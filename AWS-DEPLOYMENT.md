# AWS Deployment Guide - Production Service

Este guia explica como fazer o deploy do Production Service na AWS usando ECS, DynamoDB, SQS e Terraform.

## Pré-requisitos

### Ferramentas Necessárias
- AWS CLI (v2.0+)
- Terraform (v1.0+)
- Docker
- Maven
- Git

### Configuração AWS
1. Configure suas credenciais AWS:
```bash
aws configure
```

2. Certifique-se de que você tem as seguintes permissões:
   - EC2 (VPC, Security Groups, Load Balancer)
   - ECS (Cluster, Service, Task Definition)
   - ECR (Repository)
   - DynamoDB (Table)
   - SQS (Queue)
   - IAM (Roles, Policies)
   - Systems Manager (Parameter Store)
   - CloudWatch (Logs)

## Arquitetura da Solução

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Application   │    │      ALB        │    │      VPC        │
│  Load Balancer  │◄──►│   Target Group  │◄──►│   Public/       │
│                 │    │                 │    │  Private Subnets│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   ECS Fargate   │    │    DynamoDB     │    │      SQS        │
│    Cluster      │◄──►│     Tables      │◄──►│     Queues      │
│   Auto Scaling  │    │   Productions   │    │ Order/Completed │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Parameter     │    │   CloudWatch    │    │      ECR        │
│     Store       │    │      Logs       │    │   Repository    │
│ Config/Secrets  │    │   Monitoring    │    │  Docker Images  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Componentes da Infraestrutura

### 1. Networking (VPC)
- VPC com sub-redes públicas e privadas
- Internet Gateway e NAT Gateways
- Security Groups para ALB e ECS

### 2. Compute (ECS)
- Cluster ECS Fargate
- Task Definition com configurações otimizadas
- Service com Auto Scaling
- Application Load Balancer

### 3. Storage (DynamoDB)
- Tabela `productions` com índices secundários
- Tabela `production-events` para auditoria
- Configuração de TTL e backup

### 4. Messaging (SQS)
- Fila `order-confirmed`
- Fila `production-completed`
- Dead Letter Queue para mensagens com falha

### 5. Configuration (Parameter Store)
- Parâmetros de configuração da aplicação
- Secrets criptografados (JWT, chaves de criptografia)

## Deploy

### Deploy Completo (Recomendado)

#### Linux/macOS:
```bash
chmod +x scripts/deploy.sh
./scripts/deploy.sh deploy
```

#### Windows (PowerShell):
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
.\scripts\deploy.ps1 -Action deploy
```

### Deploy Parcial

#### Apenas Build da Aplicação:
```bash
# Linux/macOS
./scripts/deploy.sh build-only

# Windows
.\scripts\deploy.ps1 -Action build-only
```

#### Apenas Infraestrutura:
```bash
# Linux/macOS
./scripts/deploy.sh infrastructure-only

# Windows
.\scripts\deploy.ps1 -Action infrastructure-only
```

#### Apenas Atualização do Serviço:
```bash
# Linux/macOS
./scripts/deploy.sh update-service

# Windows
.\scripts\deploy.ps1 -Action update-service
```

### Deploy Manual com Terraform

1. **Navegar para o diretório terraform:**
```bash
cd terraform
```

2. **Inicializar Terraform:**
```bash
terraform init
```

3. **Planejar deployment:**
```bash
terraform plan
```

4. **Aplicar mudanças:**
```bash
terraform apply
```

## Configuração

### Variáveis de Ambiente
As seguintes variáveis são configuradas automaticamente via Parameter Store:

#### Configurações da Aplicação:
- `SPRING_PROFILES_ACTIVE`: Perfil do Spring (production)
- `SERVER_PORT`: Porta do servidor (8083)

#### AWS DynamoDB:
- `AWS_DYNAMODB_TABLE_NAME`: Nome da tabela DynamoDB
- `AWS_DEFAULT_REGION`: Região AWS

#### AWS SQS:
- `AWS_SQS_ORDER_QUEUE_URL`: URL da fila de pedidos
- `AWS_SQS_PRODUCTION_COMPLETED_QUEUE_URL`: URL da fila de produção concluída
- `AWS_SQS_DLQ_URL`: URL da Dead Letter Queue

#### AWS Cognito:
- `AWS_COGNITO_JWK_SET_URI`: URI do conjunto de chaves JWT do Cognito

### Secrets (Parâmetros Criptografados):
- `DATABASE_ENCRYPTION_KEY`: Chave de criptografia do banco
- `JWT_SECRET_KEY`: Chave secreta para JWT

## Monitoramento

### Health Checks
- **Application**: `http://<ALB_DNS>/actuator/health`
- **Detailed**: `http://<ALB_DNS>/actuator/health/readiness`

### CloudWatch Logs
Os logs da aplicação são enviados para:
- Log Group: `/ecs/production-service-production`
- Stream: `ecs/production-service-container/<task-id>`

### Métricas
- **ECS**: CPU, Memory, Task Count
- **ALB**: Request Count, Response Time, Error Rate
- **DynamoDB**: Read/Write Capacity, Throttles
- **SQS**: Messages Sent, Received, Visible

## Scaling

### Auto Scaling ECS
- **CPU Target**: 80%
- **Memory Target**: 80%
- **Min Tasks**: 1
- **Max Tasks**: 10

### DynamoDB
- **Billing Mode**: Pay-per-request (automático)

## Segurança

### Network Security
- ALB em sub-redes públicas
- ECS tasks em sub-redes privadas
- Security Groups restritivos

### IAM Roles
- **ECS Task Execution Role**: Permissões para ECR e CloudWatch
- **ECS Task Role**: Permissões para DynamoDB, SQS, Parameter Store

### Encryption
- **DynamoDB**: Server-side encryption habilitada
- **SQS**: Server-side encryption habilitada
- **Parameter Store**: SecureString para secrets
- **ALB**: HTTPS disponível (requer certificado SSL)

## Troubleshooting

### Logs de Deploy
```bash
# Ver logs do Terraform
terraform show

# Ver logs do ECS
aws logs get-log-events --log-group-name "/ecs/production-service-production" --log-stream-name <stream-name>
```

### Problemas Comuns

#### 1. Falha no Build do Docker
```bash
# Verificar logs do build
docker build -f Dockerfile.production -t production-service:latest .
```

#### 2. Falha no Push para ECR
```bash
# Re-fazer login no ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
```

#### 3. ECS Tasks Não Iniciam
```bash
# Verificar events do service
aws ecs describe-services --cluster production-service-production-cluster --services production-service-production-service
```

#### 4. Health Check Falha
- Verificar se a porta 8083 está exposta
- Verificar se o endpoint `/actuator/health` está respondendo
- Verificar security groups

## Cleanup

Para remover toda a infraestrutura:

```bash
cd terraform
terraform destroy
```

**⚠️ Atenção**: Isso irá remover todos os recursos criados, incluindo dados no DynamoDB.

## Custos Estimados (us-east-1)

### Recursos Base (24/7):
- **ECS Fargate** (2 tasks, 0.5 vCPU, 1GB): ~$30/mês
- **Application Load Balancer**: ~$22/mês
- **NAT Gateway** (2 AZs): ~$64/mês
- **VPC**: Grátis

### Recursos Variáveis:
- **DynamoDB**: Pay-per-request (depende do uso)
- **SQS**: $0.40 por milhão de requests
- **CloudWatch Logs**: $0.50 por GB ingerido
- **Parameter Store**: Grátis (padrão), $0.05 por parâmetro avançado

**Custo Total Estimado**: ~$120-150/mês (sem tráfego alto)

## Próximos Passos

1. **SSL/TLS**: Configurar certificado SSL para HTTPS
2. **Domain**: Configurar Route 53 para domínio personalizado
3. **Backup**: Configurar backup automatizado do DynamoDB
4. **Monitoring**: Configurar alertas no CloudWatch
5. **CI/CD**: Integrar com GitHub Actions ou CodePipeline