output "parameter_store_path" {
  description = "Base path for parameter store parameters"
  value       = "/${var.project_name}/${var.environment}"
}

output "configuration_parameters" {
  description = "List of configuration parameter names"
  value = [
    aws_ssm_parameter.spring_profiles_active.name,
    aws_ssm_parameter.server_port.name,
    aws_ssm_parameter.dynamodb_table_name.name,
    aws_ssm_parameter.sqs_order_queue_url.name,
    aws_ssm_parameter.sqs_production_completed_queue_url.name,
    aws_ssm_parameter.sqs_dlq_url.name,
    aws_ssm_parameter.cognito_jwk_set_uri.name,
    aws_ssm_parameter.production_max_retries.name,
    aws_ssm_parameter.production_retry_delay_ms.name,
    aws_ssm_parameter.production_enable_async.name,
    aws_ssm_parameter.production_thread_pool_size.name,
    aws_ssm_parameter.production_enable_dlq.name,
    aws_ssm_parameter.production_max_receive_count.name,
    aws_ssm_parameter.production_message_retention_seconds.name,
    aws_ssm_parameter.logging_level_root.name,
    aws_ssm_parameter.logging_level_application.name,
  ]
}

output "secret_parameters" {
  description = "List of secret parameter names"
  value = [
    aws_ssm_parameter.database_encryption_key.name,
    aws_ssm_parameter.jwt_secret_key.name,
  ]
}