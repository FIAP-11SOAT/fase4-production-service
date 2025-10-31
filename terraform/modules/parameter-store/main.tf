# Application Configuration Parameters (ConfigMap equivalent)
resource "aws_ssm_parameter" "spring_profiles_active" {
  name  = "/${var.project_name}/${var.environment}/spring/profiles/active"
  type  = "String"
  value = "production"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-spring-profiles-active"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "server_port" {
  name  = "/${var.project_name}/${var.environment}/server/port"
  type  = "String"
  value = "8083"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-server-port"
    Type = "Configuration"
  })
}

# DynamoDB Configuration
resource "aws_ssm_parameter" "dynamodb_table_name" {
  name  = "/${var.project_name}/${var.environment}/aws/dynamodb/table-name"
  type  = "String"
  value = var.dynamodb_table_name

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-dynamodb-table-name"
    Type = "Configuration"
  })
}

# SQS Configuration
resource "aws_ssm_parameter" "sqs_order_queue_url" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/order-queue-url"
  type  = "String"
  value = var.order_queue_url

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-order-queue-url"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "sqs_production_completed_queue_url" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/production-completed-queue-url"
  type  = "String"
  value = var.production_completed_queue_url

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-production-completed-queue-url"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "sqs_dlq_url" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/dlq-url"
  type  = "String"
  value = var.dlq_url

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-dlq-url"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "sqs_region" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/region"
  type  = "String"
  value = "us-east-1"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-region"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "sqs_max_number_of_messages" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/max-number-of-messages"
  type  = "String"
  value = "10"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-max-messages"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "sqs_wait_time_seconds" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/wait-time-seconds"
  type  = "String"
  value = "20"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-wait-time"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "sqs_visibility_timeout_seconds" {
  name  = "/${var.project_name}/${var.environment}/aws/sqs/visibility-timeout-seconds"
  type  = "String"
  value = "30"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-sqs-visibility-timeout"
    Type = "Configuration"
  })
}

# Cognito Configuration
resource "aws_ssm_parameter" "cognito_jwk_set_uri" {
  name  = "/${var.project_name}/${var.environment}/aws/cognito/jwk-set-uri"
  type  = "String"
  value = var.cognito_jwk_set_uri

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-cognito-jwk-set-uri"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "cognito_region" {
  name  = "/${var.project_name}/${var.environment}/aws/cognito/region"
  type  = "String"
  value = "us-east-1"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-cognito-region"
    Type = "Configuration"
  })
}

# Production Configuration
resource "aws_ssm_parameter" "production_max_retries" {
  name  = "/${var.project_name}/${var.environment}/production/processing/max-retries"
  type  = "String"
  value = "3"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-max-retries"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "production_retry_delay_ms" {
  name  = "/${var.project_name}/${var.environment}/production/processing/retry-delay-ms"
  type  = "String"
  value = "5000"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-retry-delay"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "production_enable_async" {
  name  = "/${var.project_name}/${var.environment}/production/processing/enable-async"
  type  = "String"
  value = "true"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-enable-async"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "production_thread_pool_size" {
  name  = "/${var.project_name}/${var.environment}/production/processing/thread-pool-size"
  type  = "String"
  value = "10"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-thread-pool-size"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "production_enable_dlq" {
  name  = "/${var.project_name}/${var.environment}/production/messaging/enable-dead-letter-queue"
  type  = "String"
  value = "true"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-enable-dlq"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "production_max_receive_count" {
  name  = "/${var.project_name}/${var.environment}/production/messaging/max-receive-count"
  type  = "String"
  value = "3"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-max-receive-count"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "production_message_retention_seconds" {
  name  = "/${var.project_name}/${var.environment}/production/messaging/message-retention-period-seconds"
  type  = "String"
  value = "1209600"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-message-retention"
    Type = "Configuration"
  })
}

# Logging Configuration
resource "aws_ssm_parameter" "logging_level_root" {
  name  = "/${var.project_name}/${var.environment}/logging/level/root"
  type  = "String"
  value = "INFO"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-logging-level-root"
    Type = "Configuration"
  })
}

resource "aws_ssm_parameter" "logging_level_application" {
  name  = "/${var.project_name}/${var.environment}/logging/level/com.example.production"
  type  = "String"
  value = "INFO"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-logging-level-application"
    Type = "Configuration"
  })
}

# Secrets (encrypted parameters)
resource "aws_ssm_parameter" "database_encryption_key" {
  name  = "/${var.project_name}/${var.environment}/secrets/database/encryption-key"
  type  = "SecureString"
  value = "CHANGEME_GENERATE_RANDOM_KEY"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-database-encryption-key"
    Type = "Secret"
  })
}

resource "aws_ssm_parameter" "jwt_secret_key" {
  name  = "/${var.project_name}/${var.environment}/secrets/jwt/secret-key"
  type  = "SecureString"
  value = "CHANGEME_GENERATE_JWT_SECRET"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-jwt-secret-key"
    Type = "Secret"
  })
}