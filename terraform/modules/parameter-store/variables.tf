variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "order_queue_url" {
  description = "URL of the order queue"
  type        = string
}

variable "production_completed_queue_url" {
  description = "URL of the production completed queue"
  type        = string
}

variable "dlq_url" {
  description = "URL of the dead letter queue"
  type        = string
}

variable "cognito_jwk_set_uri" {
  description = "Cognito JWK Set URI"
  type        = string
}

variable "tags" {
  description = "A map of tags to assign to the resource"
  type        = map(string)
  default     = {}
}