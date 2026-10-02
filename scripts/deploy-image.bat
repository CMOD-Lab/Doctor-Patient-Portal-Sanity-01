@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: deploy-image.bat - Deploy Doctor-Patient-Portal to AWS EKS
::                    (Windows)
:: ============================================================

set "APP_NAME=doctor-patient-portal"
set "NAMESPACE=doctor-patient-portal"
set "K8S_DIR=kubernetes"

echo ==============================================
echo   EKS Deployment - %APP_NAME%
echo ==============================================

:: ---- Collect deployment inputs ----
set /p "AWS_REGION=Enter AWS Region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo ERROR: AWS Region is required.
    exit /b 1
)

set /p "CLUSTER_NAME=Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS Cluster Name is required.
    exit /b 1
)

set /p "IMAGE_URI=Enter full Docker image URI: "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker image URI is required.
    exit /b 1
)

echo.
echo --- Application Environment Variables ---
echo Press Enter to keep the placeholder.

set /p "DB_HOST_VAL=Enter DB_HOST (MySQL host) [blank to keep placeholder]: "
set /p "DB_PORT_VAL=Enter DB_PORT (MySQL port) [blank for 3306]: "
set /p "DB_NAME_VAL=Enter DB_NAME (MySQL database name) [blank for hospital]: "
set /p "DB_USER_VAL=Enter DB_USER (MySQL username) [blank to keep placeholder]: "
set /p "DB_PASSWORD_VAL=Enter DB_PASSWORD (MySQL password) [blank to keep placeholder]: "
set /p "REDIS_HOST_VAL=Enter REDIS_HOST (ElastiCache endpoint) [blank to keep placeholder]: "
set /p "REDIS_PORT_VAL=Enter REDIS_PORT (Redis port) [blank for 6379]: "

if "!DB_PORT_VAL!"==""    set "DB_PORT_VAL=3306"
if "!DB_NAME_VAL!"==""    set "DB_NAME_VAL=hospital"
if "!REDIS_PORT_VAL!"=="" set "REDIS_PORT_VAL=6379"

:: ---- Configure kubectl for EKS ----
echo.
echo Configuring kubectl for EKS cluster '!CLUSTER_NAME!' in '!AWS_REGION!'...
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

:: ---- Patch manifests ----
echo.
echo Updating Kubernetes manifests with deployment values...

copy /Y "!K8S_DIR!\deployment.yaml" "!K8S_DIR!\deployment.yaml.deploy" >nul

powershell -NoProfile -Command ^
  "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{IMAGE_URI}}','!IMAGE_URI!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"

if not "!DB_HOST_VAL!"=="" (
    powershell -NoProfile -Command ^
      "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_HOST}}','!DB_HOST_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"
)
if not "!DB_USER_VAL!"=="" (
    powershell -NoProfile -Command ^
      "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_USER}}','!DB_USER_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"
)
if not "!DB_PASSWORD_VAL!"=="" (
    powershell -NoProfile -Command ^
      "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_PASSWORD}}','!DB_PASSWORD_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"
)
if not "!REDIS_HOST_VAL!"=="" (
    powershell -NoProfile -Command ^
      "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{REDIS_HOST}}','!REDIS_HOST_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"
)

powershell -NoProfile -Command ^
  "(Get-Content '!K8S_DIR!\deployment.yaml.deploy') -replace '{{DB_PORT}}','!DB_PORT_VAL!' -replace '{{DB_NAME}}','!DB_NAME_VAL!' -replace '{{REDIS_PORT}}','!REDIS_PORT_VAL!' | Set-Content '!K8S_DIR!\deployment.yaml.deploy'"

:: ---- Apply manifests ----
echo.
echo Applying Kubernetes manifests...

echo   [1/4] Applying namespace...
kubectl apply -f "!K8S_DIR!\namespace.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply namespace. & exit /b 1 )

echo   [2/4] Applying deployment...
kubectl apply -f "!K8S_DIR!\deployment.yaml.deploy"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply deployment. & exit /b 1 )

echo   [3/4] Applying service...
kubectl apply -f "!K8S_DIR!\service.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply service. & exit /b 1 )

echo   [4/4] Applying ingress...
kubectl apply -f "!K8S_DIR!\ingress.yaml"
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply ingress. & exit /b 1 )

del /f /q "!K8S_DIR!\deployment.yaml.deploy" >nul 2>&1

:: ---- Wait for rollout ----
echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/!APP_NAME! -n !NAMESPACE! --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo WARNING: Rollout did not complete within timeout.
    echo Rollback command: kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!
    exit /b 1
)

:: ---- Verify resources ----
echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n !NAMESPACE!

echo.
echo ==============================================
echo   Deployment Complete!
echo   Application: !APP_NAME!
echo   Namespace:   !NAMESPACE!
echo   Image:       !IMAGE_URI!
echo ==============================================
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/!APP_NAME! -n !NAMESPACE!

endlocal
exit /b 0
