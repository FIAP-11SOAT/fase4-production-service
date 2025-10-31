# Project Configuration
variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "production-service"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

# VPC Configuration
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

# ECS Configuration
variable "ecs_desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 2
}

# Cognito Configuration
variable "cognito_jwk_set_uri" {
  description = "Cognito JWK Set URI for JWT validation"
  type        = string
  default     = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_CHANGEME/.well-known/jwks.json"
}