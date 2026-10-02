@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: deploy-image.bat - Deploy Doctor-Patient-Portal to AWS EKS
:: ============================================================

set "APP_NAME=doctor-patient-portal"
set "NAMESPACE=doctor-patient-portal"
set "K8S_DIR=kubernetes"

echo ==============================================
echo  Doctor-Patient-Portal - EKS Deploy Script
echo ==============================================
echo.

set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo ERROR: AWS Region is required.
    exit /b 1
)

set /p CLUSTER_NAME="Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS Cluster Name is required.
    exit /b 1
)

set /p IMAGE_URI="Enter full Docker image URI: "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker image URI is required.
    exit /b 1
)

echo.
echo --- Application Configuration ---
echo Provide values for environment variables (press Enter to use default):
echo.

set /p DB_HOST="Enter DB_HOST [localhost]: "
if "!DB_HOST!"=="" set "DB_HOST=localhost"

set /p DB_PORT="Enter DB_PORT [3306]: "
if "!DB_PORT!"=="" set "DB_PORT=3306"

set /p DB_NAME="Enter DB_NAME [hospital]: "
if "!DB_NAME!"=="" set "DB_NAME=hospital"

set /p DB_USER="Enter DB_USER [root]: "
if "!DB_USER!"=="" set "DB_USER=root"

set /p DB_PASSWORD="Enter DB_PASSWORD: "
if "!DB_PASSWORD!"=="" set "DB_PASSWORD=changeme"

set /p REDIS_HOST="Enter REDIS_HOST [localhost]: "
if "!REDIS_HOST!"=="" set "REDIS_HOST=localhost"

set /p REDIS_PORT="Enter REDIS_PORT [6379]: "
if "!REDIS_PORT!"=="" set "REDIS_PORT=6379"

echo.
echo --- Configuring kubectl for EKS ---
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl for EKS cluster.
    exit /b 1
)

echo.
echo --- Verifying cluster connectivity ---
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to EKS cluster.
    exit /b 1
)

echo.
echo --- Updating Kubernetes manifests ---

:: Copy kubernetes directory to temp
xcopy /E /I /Y "!K8S_DIR!" "!K8S_DIR!_deploy_tmp" >nul

:: Replace placeholders using PowerShell
powershell -Command "(Get-Content '!K8S_DIR!_deploy_tmp\deployment.yaml') -replace '\{\{IMAGE_URI\}\}', '!IMAGE_URI!' -replace '\{\{DB_HOST\}\}', '!DB_HOST!' -replace '\{\{DB_PORT\}\}', '!DB_PORT!' -replace '\{\{DB_NAME\}\}', '!DB_NAME!' -replace '\{\{DB_USER\}\}', '!DB_USER!' -replace '\{\{DB_PASSWORD\}\}', '!DB_PASSWORD!' -replace '\{\{REDIS_HOST\}\}', '!REDIS_HOST!' -replace '\{\{REDIS_PORT\}\}', '!REDIS_PORT!' | Set-Content '!K8S_DIR!_deploy_tmp\deployment.yaml'"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to update deployment manifest.
    exit /b 1
)

echo.
echo --- Applying Kubernetes manifests ---

echo Applying namespace...
kubectl apply -f "!K8S_DIR!_deploy_tmp\namespace.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply namespace.
    exit /b 1
)

echo Applying deployment...
kubectl apply -f "!K8S_DIR!_deploy_tmp\deployment.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply deployment.
    exit /b 1
)

echo Applying service...
kubectl apply -f "!K8S_DIR!_deploy_tmp\service.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply service.
    exit /b 1
)

echo Applying ingress...
kubectl apply -f "!K8S_DIR!_deploy_tmp\ingress.yaml"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to apply ingress.
    exit /b 1
)

:: Clean up temp directory
rmdir /S /Q "!K8S_DIR!_deploy_tmp"

echo.
echo --- Waiting for deployment rollout ---
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed. Initiating rollback...
    kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
    echo Rollback initiated. Check pod status with:
    echo   kubectl get pods -n !NAMESPACE!
    exit /b 1
)

echo.
echo --- Verifying deployed resources ---
kubectl get pods,svc,ingress -n !NAMESPACE!

echo.
echo ==============================================
echo  SUCCESS: Deployment complete!
echo  Namespace : !NAMESPACE!
echo  Image     : !IMAGE_URI!
echo ==============================================
echo.
echo Useful commands:
echo   kubectl get pods -n !NAMESPACE!
echo   kubectl logs -f deployment/!APP_NAME! -n !NAMESPACE!
echo   kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!

endlocal
