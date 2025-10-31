output "order_queue_url" {
  description = "URL of the order confirmed queue"
  value       = aws_sqs_queue.order_confirmed.id
}

output "order_queue_arn" {
  description = "ARN of the order confirmed queue"
  value       = aws_sqs_queue.order_confirmed.arn
}

output "production_completed_queue_url" {
  description = "URL of the production completed queue"
  value       = aws_sqs_queue.production_completed.id
}

output "production_completed_queue_arn" {
  description = "ARN of the production completed queue"
  value       = aws_sqs_queue.production_completed.arn
}

output "dlq_url" {
  description = "URL of the dead letter queue"
  value       = aws_sqs_queue.dlq.id
}

output "dlq_arn" {
  description = "ARN of the dead letter queue"
  value       = aws_sqs_queue.dlq.arn
}