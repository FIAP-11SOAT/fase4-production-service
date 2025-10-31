#!/bin/bash

# Deploy script for AWS ECS using Terraform
# This script deploys the Production Service to AWS

set -e

# Configuration
PROJECT_NAME="production-service"
ENVIRONMENT="production"
AWS_REGION="us-east-1"
ECR_REPOSITORY_NAME="${PROJECT_NAME}-${ENVIRONMENT}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if required tools are installed
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v terraform &> /dev/null; then
        print_error "Terraform is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install it first."
        exit 1
    fi
    
    print_status "All prerequisites are installed."
}

# Function to build the application
build_application() {
    print_status "Building application..."
    
    cd "$(dirname "$0")/.."
    
    # Clean and build
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        print_status "Application built successfully."
    else
        print_error "Failed to build application."
        exit 1
    fi
}

# Function to get ECR login token and login
ecr_login() {
    print_status "Logging into ECR..."
    
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com
    
    if [ $? -eq 0 ]; then
        print_status "Successfully logged into ECR."
    else
        print_error "Failed to login to ECR."
        exit 1
    fi
}

# Function to build and push Docker image
build_and_push_image() {
    print_status "Building and pushing Docker image..."
    
    cd "$(dirname "$0")/.."
    
    # Get AWS account ID
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    ECR_REPOSITORY_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}"
    
    # Build image
    docker build -f Dockerfile.production -t $ECR_REPOSITORY_NAME:latest .
    
    # Tag image
    docker tag $ECR_REPOSITORY_NAME:latest $ECR_REPOSITORY_URI:latest
    docker tag $ECR_REPOSITORY_NAME:latest $ECR_REPOSITORY_URI:$(git rev-parse --short HEAD)
    
    # Push image
    docker push $ECR_REPOSITORY_URI:latest
    docker push $ECR_REPOSITORY_URI:$(git rev-parse --short HEAD)
    
    print_status "Docker image pushed successfully."
}

# Function to deploy infrastructure with Terraform
deploy_infrastructure() {
    print_status "Deploying infrastructure with Terraform..."
    
    cd "$(dirname "$0")/../terraform"
    
    # Initialize Terraform
    terraform init
    
    # Plan deployment
    terraform plan -out=tfplan
    
    # Apply deployment
    terraform apply tfplan
    
    if [ $? -eq 0 ]; then
        print_status "Infrastructure deployed successfully."
    else
        print_error "Failed to deploy infrastructure."
        exit 1
    fi
}

# Function to update ECS service
update_ecs_service() {
    print_status "Updating ECS service..."
    
    # Force new deployment to pull latest image
    aws ecs update-service \
        --cluster "${PROJECT_NAME}-${ENVIRONMENT}-cluster" \
        --service "${PROJECT_NAME}-${ENVIRONMENT}-service" \
        --force-new-deployment \
        --region $AWS_REGION
    
    if [ $? -eq 0 ]; then
        print_status "ECS service updated successfully."
    else
        print_error "Failed to update ECS service."
        exit 1
    fi
}

# Function to wait for deployment to complete
wait_for_deployment() {
    print_status "Waiting for deployment to complete..."
    
    aws ecs wait services-stable \
        --cluster "${PROJECT_NAME}-${ENVIRONMENT}-cluster" \
        --services "${PROJECT_NAME}-${ENVIRONMENT}-service" \
        --region $AWS_REGION
    
    if [ $? -eq 0 ]; then
        print_status "Deployment completed successfully."
    else
        print_error "Deployment failed or timed out."
        exit 1
    fi
}

# Function to get application URL
get_application_url() {
    print_status "Getting application URL..."
    
    cd "$(dirname "$0")/../terraform"
    
    ALB_DNS_NAME=$(terraform output -raw alb_dns_name)
    
    if [ ! -z "$ALB_DNS_NAME" ]; then
        print_status "Application is available at: http://$ALB_DNS_NAME"
        print_status "Health check: http://$ALB_DNS_NAME/actuator/health"
    else
        print_warning "Could not retrieve ALB DNS name."
    fi
}

# Main deployment function
main() {
    print_status "Starting deployment of $PROJECT_NAME to AWS..."
    
    check_prerequisites
    build_application
    ecr_login
    build_and_push_image
    deploy_infrastructure
    update_ecs_service
    wait_for_deployment
    get_application_url
    
    print_status "Deployment completed successfully!"
}

# Parse command line arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "build-only")
        check_prerequisites
        build_application
        ;;
    "infrastructure-only")
        check_prerequisites
        deploy_infrastructure
        ;;
    "update-service")
        check_prerequisites
        update_ecs_service
        wait_for_deployment
        ;;
    *)
        echo "Usage: $0 [deploy|build-only|infrastructure-only|update-service]"
        echo "  deploy              - Full deployment (default)"
        echo "  build-only          - Build application only"
        echo "  infrastructure-only - Deploy infrastructure only"
        echo "  update-service      - Update ECS service only"
        exit 1
        ;;
esac