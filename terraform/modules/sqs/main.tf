# Dead Letter Queue
resource "aws_sqs_queue" "dlq" {
  name = "${var.project_name}-${var.environment}-dlq"

  message_retention_seconds = 1209600 # 14 days
  
  # Enable server-side encryption
  sqs_managed_sse_enabled = true

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-dlq"
    Type = "DeadLetterQueue"
  })
}

# Order Confirmed Queue
resource "aws_sqs_queue" "order_confirmed" {
  name = "${var.project_name}-${var.environment}-order-confirmed"

  # Message retention period (14 days)
  message_retention_seconds = 1209600
  
  # Visibility timeout (30 seconds)
  visibility_timeout_seconds = 30
  
  # Receive wait time (20 seconds for long polling)
  receive_wait_time_seconds = 20
  
  # Dead letter queue configuration
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })

  # Enable server-side encryption
  sqs_managed_sse_enabled = true

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-order-confirmed"
    Type = "OrderQueue"
  })
}

# Production Completed Queue
resource "aws_sqs_queue" "production_completed" {
  name = "${var.project_name}-${var.environment}-production-completed"

  # Message retention period (14 days)
  message_retention_seconds = 1209600
  
  # Visibility timeout (30 seconds)
  visibility_timeout_seconds = 30
  
  # Receive wait time (20 seconds for long polling)
  receive_wait_time_seconds = 20
  
  # Dead letter queue configuration
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = 3
  })

  # Enable server-side encryption
  sqs_managed_sse_enabled = true

  tags = merge(var.tags, {
    Name = "${var.project_name}-${var.environment}-production-completed"
    Type = "ProductionQueue"
  })
}

# Queue Policy for Order Confirmed Queue
resource "aws_sqs_queue_policy" "order_confirmed" {
  queue_url = aws_sqs_queue.order_confirmed.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action = "sqs:SendMessage"
        Resource = aws_sqs_queue.order_confirmed.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = "arn:aws:sns:*:*:*"
          }
        }
      }
    ]
  })
}

# Queue Policy for Production Completed Queue
resource "aws_sqs_queue_policy" "production_completed" {
  queue_url = aws_sqs_queue.production_completed.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Action = "sqs:SendMessage"
        Resource = aws_sqs_queue.production_completed.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = "arn:aws:sns:*:*:*"
          }
        }
      }
    ]
  })
}