terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# VPC Module
module "vpc" {
  source = "./modules/vpc"
  
  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region
  
  vpc_cidr             = var.vpc_cidr
  availability_zones   = data.aws_availability_zones.available.names
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  
  tags = local.common_tags
}

# Security Groups Module
module "security_groups" {
  source = "./modules/security-groups"
  
  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.vpc.vpc_id
  
  tags = local.common_tags
}

# IAM Module
module "iam" {
  source = "./modules/iam"
  
  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region
  account_id   = data.aws_caller_identity.current.account_id
  
  tags = local.common_tags
}

# DynamoDB Module
module "dynamodb" {
  source = "./modules/dynamodb"
  
  project_name = var.project_name
  environment  = var.environment
  
  tags = local.common_tags
}

# SQS Module
module "sqs" {
  source = "./modules/sqs"
  
  project_name = var.project_name
  environment  = var.environment
  
  tags = local.common_tags
}

# Parameter Store Module (ConfigMap e Secrets)
module "parameter_store" {
  source = "./modules/parameter-store"
  
  project_name = var.project_name
  environment  = var.environment
  
  # DynamoDB
  dynamodb_table_name = module.dynamodb.table_name
  
  # SQS
  order_queue_url               = module.sqs.order_queue_url
  production_completed_queue_url = module.sqs.production_completed_queue_url
  dlq_url                       = module.sqs.dlq_url
  
  # Application settings
  cognito_jwk_set_uri = var.cognito_jwk_set_uri
  
  tags = local.common_tags
}

# ECR Module
module "ecr" {
  source = "./modules/ecr"
  
  project_name = var.project_name
  environment  = var.environment
  
  tags = local.common_tags
}

# ALB Module
module "alb" {
  source = "./modules/alb"
  
  project_name = var.project_name
  environment  = var.environment
  
  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  security_group_id  = module.security_groups.alb_security_group_id
  
  tags = local.common_tags
}

# ECS Module
module "ecs" {
  source = "./modules/ecs"
  
  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region
  
  # Networking
  vpc_id            = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  security_group_id  = module.security_groups.ecs_security_group_id
  
  # Load Balancer
  target_group_arn = module.alb.target_group_arn
  
  # IAM
  ecs_task_execution_role_arn = module.iam.ecs_task_execution_role_arn
  ecs_task_role_arn          = module.iam.ecs_task_role_arn
  
  # ECR
  ecr_repository_url = module.ecr.repository_url
  
  # Application Configuration
  container_port = 8083
  desired_count  = var.ecs_desired_count
  
  tags = local.common_tags
}

# Locals
locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
    Owner       = "FIAP-11SOAT"
  }
}