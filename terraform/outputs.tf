# VPC Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = module.vpc.private_subnet_ids
}

# Security Groups Outputs
output "alb_security_group_id" {
  description = "Security group ID for ALB"
  value       = module.security_groups.alb_security_group_id
}

output "ecs_security_group_id" {
  description = "Security group ID for ECS"
  value       = module.security_groups.ecs_security_group_id
}

# DynamoDB Outputs
output "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  value       = module.dynamodb.table_name
}

output "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table"
  value       = module.dynamodb.table_arn
}

# SQS Outputs
output "order_queue_url" {
  description = "URL of the order queue"
  value       = module.sqs.order_queue_url
}

output "production_completed_queue_url" {
  description = "URL of the production completed queue"
  value       = module.sqs.production_completed_queue_url
}

output "dlq_url" {
  description = "URL of the dead letter queue"
  value       = module.sqs.dlq_url
}

# ECR Outputs
output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = module.ecr.repository_url
}

output "ecr_repository_arn" {
  description = "ARN of the ECR repository"
  value       = module.ecr.repository_arn
}

# ALB Outputs
output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = module.alb.alb_zone_id
}

# ECS Outputs
output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs.service_name
}