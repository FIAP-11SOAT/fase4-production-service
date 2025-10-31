# PowerShell Deploy Script for AWS ECS using Terraform
# This script deploys the Production Service to AWS

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("deploy", "build-only", "infrastructure-only", "update-service")]
    [string]$Action = "deploy"
)

# Configuration
$PROJECT_NAME = "production-service"
$ENVIRONMENT = "production"
$AWS_REGION = "us-east-1"
$ECR_REPOSITORY_NAME = "$PROJECT_NAME-$ENVIRONMENT"

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Function to check if required tools are installed
function Test-Prerequisites {
    Write-Status "Checking prerequisites..."
    
    $tools = @("aws", "terraform", "docker", "mvn")
    $missing = @()
    
    foreach ($tool in $tools) {
        if (!(Get-Command $tool -ErrorAction SilentlyContinue)) {
            $missing += $tool
        }
    }
    
    if ($missing.Count -gt 0) {
        Write-Error "The following tools are not installed: $($missing -join ', ')"
        Write-Error "Please install them first."
        exit 1
    }
    
    Write-Status "All prerequisites are installed."
}

# Function to build the application
function Build-Application {
    Write-Status "Building application..."
    
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $projectRoot = Split-Path -Parent $scriptDir
    Set-Location $projectRoot
    
    # Clean and build
    & mvn clean package -DskipTests
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Application built successfully."
    } else {
        Write-Error "Failed to build application."
        exit 1
    }
}

# Function to get ECR login token and login
function Connect-ECR {
    Write-Status "Logging into ECR..."
    
    $awsAccountId = (aws sts get-caller-identity --query Account --output text)
    $ecrEndpoint = "$awsAccountId.dkr.ecr.$AWS_REGION.amazonaws.com"
    
    $loginCommand = aws ecr get-login-password --region $AWS_REGION
    if ($LASTEXITCODE -eq 0) {
        $loginCommand | docker login --username AWS --password-stdin $ecrEndpoint
        
        if ($LASTEXITCODE -eq 0) {
            Write-Status "Successfully logged into ECR."
        } else {
            Write-Error "Failed to login to ECR."
            exit 1
        }
    } else {
        Write-Error "Failed to get ECR login token."
        exit 1
    }
}

# Function to build and push Docker image
function Build-AndPushImage {
    Write-Status "Building and pushing Docker image..."
    
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $projectRoot = Split-Path -Parent $scriptDir
    Set-Location $projectRoot
    
    # Get AWS account ID
    $awsAccountId = (aws sts get-caller-identity --query Account --output text)
    $ecrRepositoryUri = "$awsAccountId.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_NAME"
    
    # Get current git commit hash
    $gitCommit = (git rev-parse --short HEAD)
    
    # Build image
    & docker build -f Dockerfile.production -t "$ECR_REPOSITORY_NAME`:latest" .
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build Docker image."
        exit 1
    }
    
    # Tag image
    & docker tag "$ECR_REPOSITORY_NAME`:latest" "$ecrRepositoryUri`:latest"
    & docker tag "$ECR_REPOSITORY_NAME`:latest" "$ecrRepositoryUri`:$gitCommit"
    
    # Push image
    & docker push "$ecrRepositoryUri`:latest"
    & docker push "$ecrRepositoryUri`:$gitCommit"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Docker image pushed successfully."
    } else {
        Write-Error "Failed to push Docker image."
        exit 1
    }
}

# Function to deploy infrastructure with Terraform
function Deploy-Infrastructure {
    Write-Status "Deploying infrastructure with Terraform..."
    
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $terraformDir = Join-Path (Split-Path -Parent $scriptDir) "terraform"
    Set-Location $terraformDir
    
    # Initialize Terraform
    & terraform init
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to initialize Terraform."
        exit 1
    }
    
    # Plan deployment
    & terraform plan -out=tfplan
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to plan Terraform deployment."
        exit 1
    }
    
    # Apply deployment
    & terraform apply tfplan
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Infrastructure deployed successfully."
    } else {
        Write-Error "Failed to deploy infrastructure."
        exit 1
    }
}

# Function to update ECS service
function Update-ECSService {
    Write-Status "Updating ECS service..."
    
    # Force new deployment to pull latest image
    & aws ecs update-service `
        --cluster "$PROJECT_NAME-$ENVIRONMENT-cluster" `
        --service "$PROJECT_NAME-$ENVIRONMENT-service" `
        --force-new-deployment `
        --region $AWS_REGION
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "ECS service updated successfully."
    } else {
        Write-Error "Failed to update ECS service."
        exit 1
    }
}

# Function to wait for deployment to complete
function Wait-ForDeployment {
    Write-Status "Waiting for deployment to complete..."
    
    & aws ecs wait services-stable `
        --cluster "$PROJECT_NAME-$ENVIRONMENT-cluster" `
        --services "$PROJECT_NAME-$ENVIRONMENT-service" `
        --region $AWS_REGION
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "Deployment completed successfully."
    } else {
        Write-Error "Deployment failed or timed out."
        exit 1
    }
}

# Function to get application URL
function Get-ApplicationUrl {
    Write-Status "Getting application URL..."
    
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $terraformDir = Join-Path (Split-Path -Parent $scriptDir) "terraform"
    Set-Location $terraformDir
    
    $albDnsName = (terraform output -raw alb_dns_name)
    
    if ($albDnsName) {
        Write-Status "Application is available at: http://$albDnsName"
        Write-Status "Health check: http://$albDnsName/actuator/health"
    } else {
        Write-Warning "Could not retrieve ALB DNS name."
    }
}

# Main deployment function
function Start-FullDeployment {
    Write-Status "Starting deployment of $PROJECT_NAME to AWS..."
    
    Test-Prerequisites
    Build-Application
    Connect-ECR
    Build-AndPushImage
    Deploy-Infrastructure
    Update-ECSService
    Wait-ForDeployment
    Get-ApplicationUrl
    
    Write-Status "Deployment completed successfully!"
}

# Main script execution
switch ($Action) {
    "deploy" {
        Start-FullDeployment
    }
    "build-only" {
        Test-Prerequisites
        Build-Application
    }
    "infrastructure-only" {
        Test-Prerequisites
        Deploy-Infrastructure
    }
    "update-service" {
        Test-Prerequisites
        Update-ECSService
        Wait-ForDeployment
    }
    default {
        Write-Host "Usage: .\deploy.ps1 [-Action <deploy|build-only|infrastructure-only|update-service>]"
        Write-Host "  deploy              - Full deployment (default)"
        Write-Host "  build-only          - Build application only"
        Write-Host "  infrastructure-only - Deploy infrastructure only"
        Write-Host "  update-service      - Update ECS service only"
        exit 1
    }
}