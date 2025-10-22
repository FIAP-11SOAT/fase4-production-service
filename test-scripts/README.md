# Scripts de Teste - Production Service

Este diretório contém scripts para testar o Production Service localmente.

## Scripts Disponíveis

### 1. `send-test-messages.sh`
Envia mensagens de teste para a fila SQS `order-confirmed`.
```bash
docker exec message-sender /scripts/send-test-messages.sh
```

### 2. `monitor-queues.sh`
Monitora o status das filas SQS e mostra mensagens.
```bash
docker exec message-sender /scripts/monitor-queues.sh
```

### 3. `test-complete.sh`
Executa um teste completo das funcionalidades da API.
```bash
# Execute do host (requer curl e jq)
./test-scripts/test-complete.sh
```

## Como Testar

1. **Subir o ambiente:**
```bash
docker-compose up -d
```

2. **Aguardar inicialização (2-3 minutos):**
```bash
docker-compose logs -f production-service
```

3. **Enviar mensagens de teste:**
```bash
docker exec message-sender /scripts/send-test-messages.sh
```

4. **Testar API manualmente:**
```bash
# Processar fila
curl -X POST "http://localhost:8083/api/productions/process-queue"

# Ver estatísticas
curl "http://localhost:8083/api/productions/stats/count-by-status"

# Listar produções
curl "http://localhost:8083/api/productions"
```

5. **Executar teste completo:**
```bash
chmod +x test-scripts/test-complete.sh
./test-scripts/test-complete.sh
```

## Monitoramento

- **LocalStack Web UI:** http://localhost:4566
- **API Health:** http://localhost:8083/actuator/health
- **MongoDB:** localhost:27017 (admin/password)
- **Logs:** `docker-compose logs -f [service-name]`