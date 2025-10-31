# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-${var.environment}-cluster"

  configuration {
    execute_command_configuration {
      logging = "OVERRIDE"
      log_configuration {
        cloud_watch_log_group_name = aws_cloudwatch_log_group.ecs.name
      }
    }
  }

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-cluster"
  })
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${var.project_name}-${var.environment}"
  retention_in_days = 7

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-ecs-logs"
  })
}

# ECS Task Definition
resource "aws_ecs_task_definition" "main" {
  family                   = "${var.project_name}-${var.environment}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = var.ecs_task_execution_role_arn
  task_role_arn           = var.ecs_task_role_arn

  container_definitions = jsonencode([
    {
      name  = "${var.project_name}-container"
      image = "${var.ecr_repository_url}:latest"

      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      environment = [
        {
          name  = "AWS_DEFAULT_REGION"
          value = var.aws_region
        }
      ]

      # Secrets from Parameter Store
      secrets = [
        {
          name      = "SPRING_PROFILES_ACTIVE"
          valueFrom = "/${var.project_name}/${var.environment}/spring/profiles/active"
        },
        {
          name      = "SERVER_PORT"
          valueFrom = "/${var.project_name}/${var.environment}/server/port"
        },
        {
          name      = "AWS_DYNAMODB_TABLE_NAME"
          valueFrom = "/${var.project_name}/${var.environment}/aws/dynamodb/table-name"
        },
        {
          name      = "AWS_SQS_ORDER_QUEUE_URL"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/order-queue-url"
        },
        {
          name      = "AWS_SQS_PRODUCTION_COMPLETED_QUEUE_URL"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/production-completed-queue-url"
        },
        {
          name      = "AWS_SQS_DLQ_URL"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/dlq-url"
        },
        {
          name      = "AWS_SQS_REGION"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/region"
        },
        {
          name      = "AWS_SQS_MAX_NUMBER_OF_MESSAGES"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/max-number-of-messages"
        },
        {
          name      = "AWS_SQS_WAIT_TIME_SECONDS"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/wait-time-seconds"
        },
        {
          name      = "AWS_SQS_VISIBILITY_TIMEOUT_SECONDS"
          valueFrom = "/${var.project_name}/${var.environment}/aws/sqs/visibility-timeout-seconds"
        },
        {
          name      = "AWS_COGNITO_JWK_SET_URI"
          valueFrom = "/${var.project_name}/${var.environment}/aws/cognito/jwk-set-uri"
        },
        {
          name      = "AWS_COGNITO_REGION"
          valueFrom = "/${var.project_name}/${var.environment}/aws/cognito/region"
        },
        {
          name      = "PRODUCTION_PROCESSING_MAX_RETRIES"
          valueFrom = "/${var.project_name}/${var.environment}/production/processing/max-retries"
        },
        {
          name      = "PRODUCTION_PROCESSING_RETRY_DELAY_MS"
          valueFrom = "/${var.project_name}/${var.environment}/production/processing/retry-delay-ms"
        },
        {
          name      = "PRODUCTION_PROCESSING_ENABLE_ASYNC"
          valueFrom = "/${var.project_name}/${var.environment}/production/processing/enable-async"
        },
        {
          name      = "PRODUCTION_PROCESSING_THREAD_POOL_SIZE"
          valueFrom = "/${var.project_name}/${var.environment}/production/processing/thread-pool-size"
        },
        {
          name      = "PRODUCTION_MESSAGING_ENABLE_DEAD_LETTER_QUEUE"
          valueFrom = "/${var.project_name}/${var.environment}/production/messaging/enable-dead-letter-queue"
        },
        {
          name      = "PRODUCTION_MESSAGING_MAX_RECEIVE_COUNT"
          valueFrom = "/${var.project_name}/${var.environment}/production/messaging/max-receive-count"
        },
        {
          name      = "PRODUCTION_MESSAGING_MESSAGE_RETENTION_PERIOD_SECONDS"
          valueFrom = "/${var.project_name}/${var.environment}/production/messaging/message-retention-period-seconds"
        },
        {
          name      = "LOGGING_LEVEL_ROOT"
          valueFrom = "/${var.project_name}/${var.environment}/logging/level/root"
        },
        {
          name      = "LOGGING_LEVEL_COM_EXAMPLE_PRODUCTION"
          valueFrom = "/${var.project_name}/${var.environment}/logging/level/com.example.production"
        },
        {
          name      = "DATABASE_ENCRYPTION_KEY"
          valueFrom = "/${var.project_name}/${var.environment}/secrets/database/encryption-key"
        },
        {
          name      = "JWT_SECRET_KEY"
          valueFrom = "/${var.project_name}/${var.environment}/secrets/jwt/secret-key"
        }
      ]

      healthCheck = {
        command = [
          "CMD-SHELL",
          "curl -f http://localhost:${var.container_port}/actuator/health || exit 1"
        ]
        interval    = 30
        timeout     = 10
        retries     = 5
        startPeriod = 120
      }

      essential = true
    }
  ])

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-task-definition"
  })
}

# ECS Service
resource "aws_ecs_service" "main" {
  name            = "${var.project_name}-${var.environment}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [var.security_group_id]
    subnets          = var.private_subnet_ids
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = "${var.project_name}-container"
    container_port   = var.container_port
  }

  depends_on = [var.target_group_arn]

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-service"
  })
}

# Auto Scaling Target
resource "aws_appautoscaling_target" "ecs_target" {
  max_capacity       = 10
  min_capacity       = 1
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.main.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-autoscaling-target"
  })
}

# Auto Scaling Policy - CPU
resource "aws_appautoscaling_policy" "ecs_policy_cpu" {
  name               = "${var.project_name}-${var.environment}-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 80.0
  }
}

# Auto Scaling Policy - Memory
resource "aws_appautoscaling_policy" "ecs_policy_memory" {
  name               = "${var.project_name}-${var.environment}-memory-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value = 80.0
  }
}