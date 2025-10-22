# Production Service - Guia de Teste Local

## 🚀 Início Rápido

### 1. Construir e Subir o Ambiente
```bash
# Compilar o projeto
mvn clean package -DskipTests

# Subir todos os serviços
docker-compose up -d

# Verificar se todos os serviços estão rodando
docker-compose ps
```

### 2. Aguardar Inicialização (2-3 minutos)
```bash
# Acompanhar logs da aplicação
docker-compose logs -f production-service

# Verificar saúde dos serviços
curl http://localhost:8083/actuator/health
```

### 3. Testar Funcionalidades

#### 📊 **Ver Estatísticas Iniciais**
```bash
curl http://localhost:8083/api/productions/stats/count-by-status
```

#### 📨 **Enviar Mensagens de Teste**
```bash
docker exec message-sender /scripts/send-test-messages.sh
```

#### ⚡ **Processar Fila**
```bash
curl -X POST "http://localhost:8083/api/productions/process-queue?maxMessages=5"
```

#### 📋 **Listar Produções**
```bash
curl "http://localhost:8083/api/productions?page=0&size=10"
```

#### 🔄 **Alterar Status**
```bash
# Primeiro, pegar ID de uma produção
PRODUCTION_ID=$(curl -s "http://localhost:8083/api/productions?size=1" | jq -r '.content[0].id')

# Alterar status
curl -X POST "http://localhost:8083/api/productions/$PRODUCTION_ID/status-change" \
     -H "Content-Type: application/json" \
     -d '{"status": "IN_PROGRESS"}'
```

### 4. Teste Automático Completo
```bash
# Dar permissão de execução
chmod +x test-scripts/test-complete.sh

# Executar teste completo
./test-scripts/test-complete.sh
```

## 🔍 Monitoramento

### Logs dos Serviços
```bash
# Todos os logs
docker-compose logs -f

# Apenas da aplicação
docker-compose logs -f production-service

# LocalStack
docker-compose logs -f localstack
```

### Status das Filas
```bash
docker exec message-sender /scripts/monitor-queues.sh
```

### Acessos Diretos
- **API Base:** http://localhost:8083/api/productions
- **Health Check:** http://localhost:8083/actuator/health
- **LocalStack:** http://localhost:4566

## 🛠️ Troubleshooting

### Problema: Serviço não inicia
```bash
# Verificar logs
docker-compose logs production-service

# Reiniciar serviço específico
docker-compose restart production-service
```

### Problema: Filas não criadas
```bash
# Reiniciar LocalStack
docker-compose restart localstack

# Verificar se filas foram criadas
docker exec message-sender aws --endpoint-url=http://localstack:4566 sqs list-queues
```

### Problema: MongoDB não conecta
```bash
# Verificar se MongoDB está rodando
docker-compose logs mongo

# Testar conexão
docker exec mongo-db mongosh --eval "db.runCommand('ping')"
```

## 📝 Endpoints Disponíveis

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/productions` | Lista produções (paginado) |
| GET | `/api/productions/stats/count-by-status` | Estatísticas por status |
| POST | `/api/productions/process-queue` | Processa mensagens da fila |
| POST | `/api/productions/{id}/status-change` | Altera status e publica na fila |

## 🔄 Fluxo de Teste Completo

1. **Dados Iniciais:** MongoDB já tem 3 produções de exemplo
2. **Enviar Pedidos:** Script adiciona 4 novos pedidos na fila
3. **Processar Fila:** API lê fila e cria produções no banco
4. **Alterar Status:** API atualiza status e envia para outra fila
5. **Verificar Resultado:** Conferir estatísticas e filas

## 🧹 Limpeza

```bash
# Parar e remover containers
docker-compose down

# Remover volumes (dados serão perdidos)
docker-compose down -v

# Limpeza completa
docker-compose down -v --rmi all
```