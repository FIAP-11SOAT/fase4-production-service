#!/bin/bash

# Script completo de teste das funcionalidades
echo "=== TESTE COMPLETO DO PRODUCTION SERVICE ==="

BASE_URL="http://localhost:8083/api/productions"

echo "1. Verificando se o serviço está rodando..."
if curl -s -f "$BASE_URL/stats/count-by-status" > /dev/null; then
    echo "✅ Serviço está rodando!"
else
    echo "❌ Serviço não está respondendo. Verifique se está rodando."
    exit 1
fi

echo -e "\n2. Verificando estatísticas iniciais..."
curl -s "$BASE_URL/stats/count-by-status" | jq '.'

echo -e "\n3. Listando produções existentes..."
curl -s "$BASE_URL?page=0&size=5" | jq '.content | length' | xargs echo "Total de produções:"

echo -e "\n4. Processando mensagens da fila..."
response=$(curl -s -X POST "$BASE_URL/process-queue?maxMessages=5")
echo "Resultado: $response"

echo -e "\n5. Verificando estatísticas após processamento..."
curl -s "$BASE_URL/stats/count-by-status" | jq '.'

echo -e "\n6. Testando mudança de status..."
# Pegar o ID da primeira produção
production_id=$(curl -s "$BASE_URL?page=0&size=1" | jq -r '.content[0].id // empty')

if [ -n "$production_id" ]; then
    echo "Alterando status da produção: $production_id"
    curl -s -X POST "$BASE_URL/$production_id/status-change" \
        -H "Content-Type: application/json" \
        -d '{"status": "IN_PROGRESS"}' | echo "Resultado: $(cat)"
else
    echo "Nenhuma produção encontrada para testar mudança de status"
fi

echo -e "\n7. Estatísticas finais..."
curl -s "$BASE_URL/stats/count-by-status" | jq '.'

echo -e "\n=== TESTE COMPLETO FINALIZADO ==="