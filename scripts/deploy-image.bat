@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo   Doctor-Patient-Portal - Deploy to AWS EKS
echo ==============================================
echo.

set "APP_NAME=doctor-patient-portal"
set "NAMESPACE=doctor-patient-portal"

:: -- AWS / EKS configuration --------------------------------------------------
set /p AWS_REGION="Enter AWS region [us-east-1]: "
if "!AWS_REGION!"=="" set "AWS_REGION=us-east-1"

set /p CLUSTER_NAME="Enter EKS cluster name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS cluster name is required.
    exit /b 1
)

:: -- Docker image URI ---------------------------------------------------------
set /p IMAGE_URI="Enter full Docker image URI (e.g. 123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker image URI is required.
    exit /b 1
)

:: -- Application environment variables ----------------------------------------
echo.
echo --- Application Configuration ---

set /p REDIS_HOST="Enter REDIS_HOST (ElastiCache endpoint) [localhost]: "
if "!REDIS_HOST!"=="" set "REDIS_HOST=localhost"

set /p REDIS_PORT="Enter REDIS_PORT [6379]: "
if "!REDIS_PORT!"=="" set "REDIS_PORT=6379"

set /p DB_HOST="Enter DB_HOST (RDS endpoint) [localhost]: "
if "!DB_HOST!"=="" set "DB_HOST=localhost"

set /p DB_PORT="Enter DB_PORT [3306]: "
if "!DB_PORT!"=="" set "DB_PORT=3306"

set /p DB_NAME="Enter DB_NAME [hospital]: "
if "!DB_NAME!"=="" set "DB_NAME=hospital"

set /p DB_USER="Enter DB_USER [root]: "
if "!DB_USER!"=="" set "DB_USER=root"

set /p DB_PASSWORD="Enter DB_PASSWORD: "
if "!DB_PASSWORD!"=="" set "DB_PASSWORD=changeme"

:: -- Configure kubectl --------------------------------------------------------
echo.
echo Configuring kubectl for cluster: !CLUSTER_NAME! in !AWS_REGION! ...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl.
    exit /b 1
)

echo Verifying cluster connectivity ...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to EKS cluster.
    exit /b 1
)

:: -- Patch manifests ----------------------------------------------------------
echo.
echo Updating Kubernetes manifests ...

set "DEPLOY_YAML=kubernetes\deployment.yaml"

powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{IMAGE_URI}}','!IMAGE_URI!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{REDIS_HOST}}','!REDIS_HOST!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{REDIS_PORT}}','!REDIS_PORT!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{DB_HOST}}','!DB_HOST!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{DB_PORT}}','!DB_PORT!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{DB_NAME}}','!DB_NAME!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{DB_USER}}','!DB_USER!' | Set-Content '!DEPLOY_YAML!'"
powershell -Command "(Get-Content '!DEPLOY_YAML!') -replace '{{DB_PASSWORD}}','!DB_PASSWORD!' | Set-Content '!DEPLOY_YAML!'"

:: -- Apply manifests ----------------------------------------------------------
echo.
echo Applying Kubernetes manifests ...

kubectl apply -f kubernetes\namespace.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply namespace. & exit /b 1 )

kubectl apply -f kubernetes\deployment.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply deployment. & exit /b 1 )

kubectl apply -f kubernetes\service.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply service. & exit /b 1 )

kubectl apply -f kubernetes\ingress.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply ingress. & exit /b 1 )

:: -- Wait for rollout ---------------------------------------------------------
echo.
echo Waiting for deployment rollout ...
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed.
    echo Rollback command: kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
    exit /b 1
)

:: -- Verify -------------------------------------------------------------------
echo.
echo Deployment status:
kubectl get pods,svc,ingress -n !NAMESPACE!

echo.
echo Deployment complete!
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!

endlocal
