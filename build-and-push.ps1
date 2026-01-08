# Script para buildar e fazer push da imagem Docker para o ECR
# Autor: GitHub Copilot
# Data: Janeiro 2026

# Configuracoes - AJUSTE ESTES VALORES
$AWS_REGION = "us-east-1"
$ECR_REPOSITORY_NAME = "fase4-production-service-ecr"
$AWS_ACCOUNT_ID = ""
$IMAGE_TAG = "latest"

# Cores para output
$ErrorColor = "Red"
$SuccessColor = "Green"
$InfoColor = "Cyan"

Write-Host "========================================" -ForegroundColor $InfoColor
Write-Host "  Build e Push para ECR - Production Service" -ForegroundColor $InfoColor
Write-Host "========================================" -ForegroundColor $InfoColor
Write-Host ""

# Verificar se Docker esta rodando
Write-Host "[1/6] Verificando Docker..." -ForegroundColor $InfoColor
try {
    docker info | Out-Null
    Write-Host "OK Docker esta rodando" -ForegroundColor $SuccessColor
}
catch {
    Write-Host "ERRO: Docker nao esta rodando ou nao esta instalado" -ForegroundColor $ErrorColor
    Write-Host "Por favor, inicie o Docker Desktop e tente novamente" -ForegroundColor $ErrorColor
    exit 1
}

# Verificar se AWS CLI esta instalado
Write-Host "`n[2/6] Verificando AWS CLI..." -ForegroundColor $InfoColor
try {
    aws --version | Out-Null
    Write-Host "OK AWS CLI esta instalado" -ForegroundColor $SuccessColor
}
catch {
    Write-Host "ERRO: AWS CLI nao esta instalado" -ForegroundColor $ErrorColor
    Write-Host "Instale via: https://aws.amazon.com/cli/" -ForegroundColor $ErrorColor
    exit 1
}

# Obter Account ID automaticamente se nao foi fornecido
if ([string]::IsNullOrEmpty($AWS_ACCOUNT_ID)) {
    Write-Host "`n[3/6] Detectando AWS Account ID..." -ForegroundColor $InfoColor
    try {
        $AWS_ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
        Write-Host "OK Account ID detectado: $AWS_ACCOUNT_ID" -ForegroundColor $SuccessColor
    }
    catch {
        Write-Host "ERRO ao detectar Account ID" -ForegroundColor $ErrorColor
        Write-Host "Configure suas credenciais AWS com 'aws configure'" -ForegroundColor $ErrorColor
        exit 1
    }
}
else {
    Write-Host "`n[3/6] Usando Account ID configurado: $AWS_ACCOUNT_ID" -ForegroundColor $SuccessColor
}

# Construir URL do ECR
$ECR_URL = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
$FULL_IMAGE_NAME = "${ECR_URL}/${ECR_REPOSITORY_NAME}:${IMAGE_TAG}"

# Login no ECR
Write-Host "`n[4/6] Fazendo login no ECR..." -ForegroundColor $InfoColor
try {
    $loginCommand = aws ecr get-login-password --region $AWS_REGION
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao obter senha do ECR"
    }
    
    $loginCommand | docker login --username AWS --password-stdin $ECR_URL 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK Login no ECR realizado com sucesso" -ForegroundColor $SuccessColor
    }
    else {
        throw "Falha no login"
    }
}
catch {
    Write-Host "ERRO ao fazer login no ECR" -ForegroundColor $ErrorColor
    Write-Host "Verifique se o repositorio existe com: aws ecr describe-repositories --region $AWS_REGION" -ForegroundColor $ErrorColor
    exit 1
}

# Buildar a imagem Docker
Write-Host "`n[5/6] Buildando imagem Docker..." -ForegroundColor $InfoColor
Write-Host "Imagem: $FULL_IMAGE_NAME" -ForegroundColor $InfoColor
try {
    docker build -t $FULL_IMAGE_NAME .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK Imagem buildada com sucesso" -ForegroundColor $SuccessColor
    }
    else {
        throw "Falha no build"
    }
}
catch {
    Write-Host "ERRO ao buildar imagem" -ForegroundColor $ErrorColor
    exit 1
}

# Fazer push da imagem para o ECR
Write-Host "`n[6/6] Fazendo push da imagem para o ECR..." -ForegroundColor $InfoColor
try {
    docker push $FULL_IMAGE_NAME
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK Push realizado com sucesso!" -ForegroundColor $SuccessColor
    }
    else {
        throw "Falha no push"
    }
}
catch {
    Write-Host "ERRO ao fazer push da imagem" -ForegroundColor $ErrorColor
    exit 1
}

# Resumo final
Write-Host "`n========================================" -ForegroundColor $InfoColor
Write-Host "  CONCLUIDO COM SUCESSO! " -ForegroundColor $SuccessColor
Write-Host "========================================" -ForegroundColor $InfoColor
Write-Host "`nImagem disponivel em:" -ForegroundColor $InfoColor
Write-Host "$FULL_IMAGE_NAME" -ForegroundColor $SuccessColor
Write-Host "`nPara criar o repositorio ECR (se nao existir):" -ForegroundColor $InfoColor
Write-Host "aws ecr create-repository --repository-name $ECR_REPOSITORY_NAME --region $AWS_REGION" -ForegroundColor $InfoColor
Write-Host ""
