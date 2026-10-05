@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM deploy-image.bat  -  Deploy Doctor-Patient-Portal to AWS EKS
REM ============================================================

set "APP_NAME=doctor-patient-portal"
set "NAMESPACE=doctor-patient-portal"
set "K8S_DIR=kubernetes"
set "TMP_DIR=%TEMP%\k8s-deploy-%RANDOM%"

echo ==============================================
echo   Doctor-Patient-Portal - Deploy to AWS EKS
echo ==============================================

REM ---------- AWS / EKS configuration ----------
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

REM ---------- Application environment variables ----------
echo.
echo --- Application Environment Variables ---
echo (Press Enter to use defaults or skip)

set /p DB_HOST="Enter DB_HOST (MySQL host): "
set /p DB_PORT="Enter DB_PORT [3306]: "
if "!DB_PORT!"=="" set "DB_PORT=3306"
set /p DB_NAME="Enter DB_NAME [hospital]: "
if "!DB_NAME!"=="" set "DB_NAME=hospital"
set /p DB_USER="Enter DB_USER [root]: "
if "!DB_USER!"=="" set "DB_USER=root"
set /p DB_PASSWORD="Enter DB_PASSWORD: "
set /p REDIS_HOST="Enter REDIS_HOST (ElastiCache endpoint): "
set /p REDIS_PORT="Enter REDIS_PORT [6379]: "
if "!REDIS_PORT!"=="" set "REDIS_PORT=6379"
set /p REDIS_PASSWORD="Enter REDIS_PASSWORD (leave blank if none): "

REM ---------- Configure kubectl ----------
echo.
echo Configuring kubectl for EKS cluster: !CLUSTER_NAME! ...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl.
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to EKS cluster.
    exit /b 1
)

REM ---------- Copy manifests to temp dir ----------
echo.
echo Preparing Kubernetes manifests...
xcopy /E /I /Q "!K8S_DIR!" "!TMP_DIR!" >nul
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to copy manifests.
    exit /b 1
)

REM ---------- Substitute placeholders using PowerShell ----------
echo Updating manifests with provided values...
powershell -Command "(Get-Content '!TMP_DIR!\deployment.yaml') -replace '\{\{IMAGE_URI\}\}','!IMAGE_URI!' -replace '\{\{DB_HOST\}\}','!DB_HOST!' -replace '\{\{DB_PORT\}\}','!DB_PORT!' -replace '\{\{DB_NAME\}\}','!DB_NAME!' -replace '\{\{DB_USER\}\}','!DB_USER!' -replace '\{\{DB_PASSWORD\}\}','!DB_PASSWORD!' -replace '\{\{REDIS_HOST\}\}','!REDIS_HOST!' -replace '\{\{REDIS_PORT\}\}','!REDIS_PORT!' -replace '\{\{REDIS_PASSWORD\}\}','!REDIS_PASSWORD!' | Set-Content '!TMP_DIR!\deployment.yaml'"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to update deployment manifest.
    exit /b 1
)

REM ---------- Apply manifests ----------
echo.
echo Applying Kubernetes manifests...

echo [1/4] Applying namespace...
kubectl apply -f "!TMP_DIR!\namespace.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply namespace. & exit /b 1 )

echo [2/4] Applying deployment...
kubectl apply -f "!TMP_DIR!\deployment.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply deployment. & exit /b 1 )

echo [3/4] Applying service...
kubectl apply -f "!TMP_DIR!\service.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply service. & exit /b 1 )

echo [4/4] Applying ingress...
kubectl apply -f "!TMP_DIR!\ingress.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply ingress. & exit /b 1 )

REM ---------- Wait for rollout ----------
echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo WARNING: Rollout did not complete within timeout.
)

REM ---------- Verify resources ----------
echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n !NAMESPACE!

REM ---------- Cleanup ----------
rmdir /S /Q "!TMP_DIR!" >nul 2>&1

echo.
echo ==============================================
echo   Deployment complete!
echo   Namespace : !NAMESPACE!
echo   Image     : !IMAGE_URI!
echo ==============================================
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!

endlocal
