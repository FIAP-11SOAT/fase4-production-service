output "table_name" {
  description = "Name of the DynamoDB productions table"
  value       = aws_dynamodb_table.productions.name
}

output "table_arn" {
  description = "ARN of the DynamoDB productions table"
  value       = aws_dynamodb_table.productions.arn
}

output "table_id" {
  description = "ID of the DynamoDB productions table"
  value       = aws_dynamodb_table.productions.id
}

output "events_table_name" {
  description = "Name of the DynamoDB production events table"
  value       = aws_dynamodb_table.production_events.name
}

output "events_table_arn" {
  description = "ARN of the DynamoDB production events table"
  value       = aws_dynamodb_table.production_events.arn
}

output "events_table_id" {
  description = "ID of the DynamoDB production events table"
  value       = aws_dynamodb_table.production_events.id
}