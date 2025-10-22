#!/bin/bash

echo "Inicializando LocalStack..."

# Aguardar o LocalStack estar pronto
until curl -s http://localhost:4566/health | grep -q "running"; do
  echo "Aguardando LocalStack..."
  sleep 2
done

echo "LocalStack está pronto, criando recursos..."

# Criar filas SQS
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name order-confirmed \
    --region us-east-1

aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name production-completed \
    --region us-east-1

# Criar DLQ (Dead Letter Queue)
aws --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name order-confirmed-dlq \
    --region us-east-1

echo "Filas SQS criadas:"
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1

echo "LocalStack inicializado com sucesso!"