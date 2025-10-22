#!/bin/bash

# Script para monitorar as filas SQS
export AWS_ENDPOINT_URL=http://localstack:4566
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

echo "=== Status das Filas SQS ==="

echo "Fila order-confirmed:"
aws sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/order-confirmed \
    --attribute-names ApproximateNumberOfMessages,ApproximateNumberOfMessagesNotVisible

echo -e "\nFila production-completed:"
aws sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/production-completed \
    --attribute-names ApproximateNumberOfMessages,ApproximateNumberOfMessagesNotVisible

echo -e "\n=== Últimas mensagens na fila production-completed ==="
aws sqs receive-message \
    --queue-url http://localstack:4566/000000000000/production-completed \
    --max-number-of-messages 5 \
    --wait-time-seconds 1