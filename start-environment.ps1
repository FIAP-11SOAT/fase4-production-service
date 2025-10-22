Corrigir configuração do Maven Spring Boot Plugin no pom.xml
Recompilar e reiniciar o production-service
Testar os endpoints REST da sua controller# Scripts para Windows PowerShell

Write-Host "Iniciando ambiente Docker..." -ForegroundColor Green

# Configurar Java 21 para Maven
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + ($env:PATH -replace "C:\\Program Files\\Java\\jdk1\.8\.0_202\\bin;", "")

Write-Host "✅ Java 21 configurado para Maven" -ForegroundColor Green

# Verificar se Docker está rodando
try {
    docker version | Out-Null
    Write-Host "✅ Docker está rodando" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker não está rodando. Inicie o Docker Desktop primeiro." -ForegroundColor Red
    exit 1
}

# Compilar o projeto
Write-Host "`nCompilando o projeto..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Falha na compilação" -ForegroundColor Red
    exit 1
}

# Subir os containers
Write-Host "`nSubindo containers..." -ForegroundColor Yellow
docker-compose up -d

# Aguardar inicialização
Write-Host "`nAguardando inicialização dos serviços..." -ForegroundColor Yellow
Start-Sleep 30

# Verificar status dos containers
Write-Host "`nStatus dos containers:" -ForegroundColor Yellow
docker-compose ps

# Aguardar aplicação estar pronta
Write-Host "`nAguardando aplicação estar pronta..." -ForegroundColor Yellow
$timeout = 120 # 2 minutos
$elapsed = 0

do {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8083/actuator/health" -Method Get -TimeoutSec 5
        if ($health.status -eq "UP") {
            Write-Host "✅ Aplicação está pronta!" -ForegroundColor Green
            break
        }
    } catch {
        # Continuar tentando
    }
    
    Start-Sleep 5
    $elapsed += 5
    Write-Host "." -NoNewline
    
} while ($elapsed -lt $timeout)

if ($elapsed -ge $timeout) {
    Write-Host "`n❌ Timeout: Aplicação não ficou pronta em 2 minutos" -ForegroundColor Red
    Write-Host "Verificar logs: docker-compose logs -f production-service" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n`n🎉 Ambiente pronto para testes!" -ForegroundColor Green
Write-Host "Executar: .\test-windows.ps1" -ForegroundColor Cyan