#!/bin/bash

# Script para enviar mensagens de teste para a fila
echo "Enviando mensagens de teste para a fila..."

# Instalar AWS CLI se não estiver disponível
if ! command -v aws &> /dev/null; then
    echo "Instalando AWS CLI..."
    yum update -y
    yum install -y awscli curl
fi

export AWS_ENDPOINT_URL=http://localstack:4566
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# Função para enviar mensagem de pedido
send_order_message() {
    local order_id=$1
    local product_ids=$2
    
    local message=$(cat <<EOF
{
  "orderId": $order_id,
  "productIds": [$product_ids],
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "source": "order-service"
}
EOF
)
    
    echo "Enviando pedido $order_id..."
    aws sqs send-message \
        --queue-url http://localstack:4566/000000000000/order-confirmed \
        --message-body "$message"
}

# Enviar algumas mensagens de teste
echo "=== Enviando mensagens de pedidos ==="

send_order_message 2001 "601,602"
send_order_message 2002 "603"
send_order_message 2003 "604,605,606"
send_order_message 2004 "607,608"

echo "Mensagens enviadas! Verifique a fila:"
aws sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/order-confirmed \
    --attribute-names ApproximateNumberOfMessages

echo "=== Pronto para testes! ==="