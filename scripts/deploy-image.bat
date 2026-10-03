@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: deploy-image.bat  -  Deploy Doctor-Patient-Portal to AWS EKS
:: ============================================================

set APP_NAME=doctor-patient-portal
set NAMESPACE=doctor-patient-portal
set K8S_DIR=kubernetes
set TEMP_DIR=%TEMP%\k8s-deploy-%RANDOM%

echo ==============================================
echo   Doctor-Patient-Portal - EKS Deployment
echo ==============================================

:: ---------- AWS / EKS configuration ----------
echo.
set /p AWS_REGION="Enter AWS region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo ERROR: AWS region is required.
    exit /b 1
)

set /p CLUSTER_NAME="Enter EKS cluster name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS cluster name is required.
    exit /b 1
)

set /p IMAGE_URI="Enter full Docker image URI: "
if "!IMAGE_URI!"=="" (
    echo ERROR: Image URI is required.
    exit /b 1
)

:: ---------- Application environment variables ----------
echo.
echo --- Application Environment Variables ---
echo (Press Enter to use defaults)

set /p REDIS_HOST_VAL="Enter REDIS_HOST (ElastiCache endpoint, default: localhost): "
if "!REDIS_HOST_VAL!"=="" set REDIS_HOST_VAL=localhost

set /p REDIS_PORT_VAL="Enter REDIS_PORT (default: 6379): "
if "!REDIS_PORT_VAL!"=="" set REDIS_PORT_VAL=6379

set /p REDIS_PASSWORD_VAL="Enter REDIS_PASSWORD (leave blank if none): "

set /p DB_HOST_VAL="Enter DB_HOST (MySQL endpoint, default: localhost): "
if "!DB_HOST_VAL!"=="" set DB_HOST_VAL=localhost

set /p DB_PORT_VAL="Enter DB_PORT (default: 3306): "
if "!DB_PORT_VAL!"=="" set DB_PORT_VAL=3306

set /p DB_NAME_VAL="Enter DB_NAME (default: hospital): "
if "!DB_NAME_VAL!"=="" set DB_NAME_VAL=hospital

set /p DB_USER_VAL="Enter DB_USER (default: root): "
if "!DB_USER_VAL!"=="" set DB_USER_VAL=root

set /p DB_PASSWORD_VAL="Enter DB_PASSWORD: "

:: ---------- Configure kubectl ----------
echo.
echo Configuring kubectl for cluster: !CLUSTER_NAME! in !AWS_REGION!...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl.
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to cluster.
    exit /b 1
)

:: ---------- Copy manifests to temp dir ----------
echo.
echo Patching Kubernetes manifests...
xcopy /E /I /Q !K8S_DIR! !TEMP_DIR! >nul

:: Use PowerShell to replace placeholders
powershell -Command "(Get-Content '!TEMP_DIR!\deployment.yaml') -replace '\{\{IMAGE_URI\}\}','!IMAGE_URI!' -replace '\{\{REDIS_HOST\}\}','!REDIS_HOST_VAL!' -replace '\{\{REDIS_PORT\}\}','!REDIS_PORT_VAL!' -replace '\{\{REDIS_PASSWORD\}\}','!REDIS_PASSWORD_VAL!' -replace '\{\{DB_HOST\}\}','!DB_HOST_VAL!' -replace '\{\{DB_PORT\}\}','!DB_PORT_VAL!' -replace '\{\{DB_NAME\}\}','!DB_NAME_VAL!' -replace '\{\{DB_USER\}\}','!DB_USER_VAL!' -replace '\{\{DB_PASSWORD\}\}','!DB_PASSWORD_VAL!' | Set-Content '!TEMP_DIR!\deployment.yaml'"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to patch deployment.yaml.
    exit /b 1
)

:: ---------- Apply manifests ----------
echo.
echo Applying Kubernetes manifests...
kubectl apply -f !TEMP_DIR!\namespace.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply namespace.yaml & exit /b 1 )

kubectl apply -f !TEMP_DIR!\deployment.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply deployment.yaml & exit /b 1 )

kubectl apply -f !TEMP_DIR!\service.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply service.yaml & exit /b 1 )

kubectl apply -f !TEMP_DIR!\ingress.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply ingress.yaml & exit /b 1 )

:: ---------- Wait for rollout ----------
echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed.
    echo Rollback command: kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
    exit /b 1
)

:: ---------- Verify ----------
echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n !NAMESPACE!

echo.
echo ==============================================
echo   Deployment complete!
echo   Check ingress for application URL.
echo   Health check path: /health
echo ==============================================
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!

:: Cleanup
rmdir /S /Q !TEMP_DIR! >nul 2>&1

endlocal
