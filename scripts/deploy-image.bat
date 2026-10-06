@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: deploy-image.bat – Deploy Doctor-Patient-Portal to AWS EKS
:: ============================================================

set APP_NAME=doctor-patient-portal
set NAMESPACE=doctor-patient-portal
set K8S_DIR=kubernetes

echo ============================================
echo   Doctor-Patient-Portal – EKS Deploy Script
echo ============================================

:: ---------- AWS Region ----------
set /p AWS_REGION="Enter AWS region [us-east-1]: "
if "!AWS_REGION!"=="" set AWS_REGION=us-east-1

:: ---------- EKS Cluster name ----------
set /p CLUSTER_NAME="Enter EKS cluster name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS cluster name is required.
    exit /b 1
)

:: ---------- Docker image URI ----------
set /p IMAGE_URI="Enter full Docker image URI: "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker image URI is required.
    exit /b 1
)

:: ---------- Application environment variables ----------
echo.
echo --- Application Configuration ---

set /p DB_HOST="Enter DB_HOST (MySQL host) [localhost]: "
if "!DB_HOST!"=="" set DB_HOST=localhost

set /p DB_PORT="Enter DB_PORT [3306]: "
if "!DB_PORT!"=="" set DB_PORT=3306

set /p DB_NAME="Enter DB_NAME [hospital]: "
if "!DB_NAME!"=="" set DB_NAME=hospital

set /p DB_USER="Enter DB_USER [root]: "
if "!DB_USER!"=="" set DB_USER=root

set /p DB_PASSWORD="Enter DB_PASSWORD: "
if "!DB_PASSWORD!"=="" set DB_PASSWORD=changeme

set /p REDIS_HOST="Enter REDIS_HOST (ElastiCache endpoint) [localhost]: "
if "!REDIS_HOST!"=="" set REDIS_HOST=localhost

set /p REDIS_PORT="Enter REDIS_PORT [6379]: "
if "!REDIS_PORT!"=="" set REDIS_PORT=6379

set /p REDIS_PASSWORD="Enter REDIS_PASSWORD (leave blank if none): "

:: ---------- Configure kubectl ----------
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
    echo ERROR: Cannot connect to cluster.
    exit /b 1
)

:: ---------- Copy manifests to temp dir ----------
set TEMP_DIR=%TEMP%\k8s-deploy-%RANDOM%
xcopy /E /I /Q !K8S_DIR! !TEMP_DIR! >nul

:: ---------- Patch manifests using PowerShell ----------
echo.
echo Patching Kubernetes manifests...

powershell -NoProfile -Command ^
    "$content = Get-Content '!TEMP_DIR!\deployment.yaml' -Raw; ^
     $content = $content -replace '{{IMAGE_URI}}','!IMAGE_URI!'; ^
     $content = $content -replace '{{DB_HOST}}','!DB_HOST!'; ^
     $content = $content -replace '{{DB_PORT}}','!DB_PORT!'; ^
     $content = $content -replace '{{DB_NAME}}','!DB_NAME!'; ^
     $content = $content -replace '{{DB_USER}}','!DB_USER!'; ^
     $content = $content -replace '{{DB_PASSWORD}}','!DB_PASSWORD!'; ^
     $content = $content -replace '{{REDIS_HOST}}','!REDIS_HOST!'; ^
     $content = $content -replace '{{REDIS_PORT}}','!REDIS_PORT!'; ^
     $content = $content -replace '{{REDIS_PASSWORD}}','!REDIS_PASSWORD!'; ^
     Set-Content '!TEMP_DIR!\deployment.yaml' $content"

if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to patch deployment manifest.
    exit /b 1
)

:: ---------- Apply manifests ----------
echo.
echo Applying Kubernetes manifests...

echo   [1/4] Applying namespace...
kubectl apply -f !TEMP_DIR!\namespace.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply namespace. & exit /b 1 )

echo   [2/4] Applying deployment...
kubectl apply -f !TEMP_DIR!\deployment.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply deployment. & exit /b 1 )

echo   [3/4] Applying service...
kubectl apply -f !TEMP_DIR!\service.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply service. & exit /b 1 )

echo   [4/4] Applying ingress...
kubectl apply -f !TEMP_DIR!\ingress.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply ingress. & exit /b 1 )

:: ---------- Wait for rollout ----------
echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed. Running rollback...
    kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
    rmdir /S /Q !TEMP_DIR!
    exit /b 1
)

:: ---------- Verify ----------
echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n !NAMESPACE!

echo.
echo ============================================
echo   Deployment complete!
echo   Check ingress for the application URL.
echo   Health endpoint: /health
echo ============================================

:: Cleanup
rmdir /S /Q !TEMP_DIR!

endlocal
