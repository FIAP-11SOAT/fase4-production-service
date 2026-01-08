resource "aws_sqs_queue" "production_queue" {
  name = "${local.project_name}-queue"

  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600 # 4 dias
  max_message_size           = 262144 # 256 KB
  delay_seconds              = 0
  receive_wait_time_seconds  = 5

  tags = {
    Name = "${local.project_name}-queue"
  }
}

