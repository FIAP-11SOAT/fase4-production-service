# Production Service (spring boot) - minimal prototype

This project is a minimal Production Service example for Java 21 + Spring Boot.
It listens to an SQS queue (`order-confirmed`) and writes production documents to MongoDB (DocumentDB compatible).
It also publishes a message to `production-completed` when production is marked complete via REST.

## Local dev with LocalStack + MongoDB

1. Build the project:
   ```bash
   mvn -B -DskipTests package
   ```

2. Start LocalStack and Mongo:
   ```bash
   docker compose up -d
   ```

3. Create SQS queues (using `awslocal` - included in LocalStack):
   ```bash
   docker exec -it $(docker ps -qf "ancestor=localstack/localstack") awslocal sqs create-queue --queue-name order-confirmed
   docker exec -it $(docker ps -qf "ancestor=localstack/localstack") awslocal sqs create-queue --queue-name production-completed
   ```

4. Run the Spring Boot app (or use Docker build/run):
   ```bash
   mvn spring-boot:run
   ```

5. Send a test message to `order-confirmed`:
   ```bash
   docker exec -it $(docker ps -qf "ancestor=localstack/localstack") awslocal sqs send-message --queue-url http://localhost:4566/000000000000/order-confirmed --message-body '{"orderId":123,"productIds":[1,2,3]}'
   ```

6. Check http://localhost:8083/api/productions to see saved production documents.

## Notes for AWS
- Replace LocalStack URLs with the real SQS queue URLs in `application.yml` when deploying to AWS.
- Configure Cognito JWKS URI in `application.yml`.
- For DocumentDB, update the `spring.data.mongodb.uri` to point to your AWS DocumentDB cluster (use TLS/SSL and proper replica set settings).

