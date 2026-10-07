@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Deploy Doctor Patient Portal to AWS EKS
echo ============================================
echo.

set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo AWS Region is required. Exiting.
    exit /b 1
)

set /p CLUSTER_NAME="Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo EKS Cluster Name is required. Exiting.
    exit /b 1
)

set /p IMAGE_URI="Enter full Docker image URI: "
if "!IMAGE_URI!"=="" (
    echo Docker image URI is required. Exiting.
    exit /b 1
)

echo.
echo --- Application Environment Variables ---
echo Provide values for the following (press Enter to skip):
echo.

set /p DB_HOST="Enter DB_HOST (MySQL host): "
if "!DB_HOST!"=="" set DB_HOST={{DB_HOST}}

set /p DB_PORT="Enter DB_PORT (default 3306): "
if "!DB_PORT!"=="" set DB_PORT=3306

set /p DB_NAME="Enter DB_NAME (default hospital): "
if "!DB_NAME!"=="" set DB_NAME=hospital

set /p DB_USER="Enter DB_USER (default root): "
if "!DB_USER!"=="" set DB_USER=root

set /p DB_PASSWORD="Enter DB_PASSWORD: "
if "!DB_PASSWORD!"=="" set DB_PASSWORD={{DB_PASSWORD}}

set /p REDIS_HOST="Enter REDIS_HOST (ElastiCache endpoint): "
if "!REDIS_HOST!"=="" set REDIS_HOST={{REDIS_HOST}}

set /p REDIS_PORT="Enter REDIS_PORT (default 6379): "
if "!REDIS_PORT!"=="" set REDIS_PORT=6379

echo.
echo Configuring kubectl for EKS cluster: !CLUSTER_NAME! in !AWS_REGION! ...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo Failed to configure kubectl. Exiting.
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Cannot connect to cluster. Exiting.
    exit /b 1
)

echo.
echo Updating Kubernetes manifests with provided values...

copy kubernetes\deployment.yaml kubernetes\deployment.yaml.bak >nul

powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_HOST}}', '!DB_HOST!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_PORT}}', '!DB_PORT!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_NAME}}', '!DB_NAME!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_USER}}', '!DB_USER!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{DB_PASSWORD}}', '!DB_PASSWORD!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_HOST}}', '!REDIS_HOST!' | Set-Content kubernetes\deployment.yaml"
powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_PORT}}', '!REDIS_PORT!' | Set-Content kubernetes\deployment.yaml"

echo.
echo Applying Kubernetes manifests...
kubectl apply -f kubernetes\namespace.yaml
if !ERRORLEVEL! neq 0 ( echo Failed to apply namespace. & exit /b 1 )

kubectl apply -f kubernetes\deployment.yaml
if !ERRORLEVEL! neq 0 ( echo Failed to apply deployment. & exit /b 1 )

kubectl apply -f kubernetes\service.yaml
if !ERRORLEVEL! neq 0 ( echo Failed to apply service. & exit /b 1 )

kubectl apply -f kubernetes\ingress.yaml
if !ERRORLEVEL! neq 0 ( echo Failed to apply ingress. & exit /b 1 )

echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/doctor-patient-portal -n doctor-patient-portal
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed.
    echo Rollback: kubectl rollout undo deployment/doctor-patient-portal -n doctor-patient-portal
    exit /b 1
)

echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n doctor-patient-portal

echo.
echo Restoring original deployment.yaml...
move /y kubernetes\deployment.yaml.bak kubernetes\deployment.yaml >nul

echo.
echo ============================================
echo   Deployment Complete!
echo ============================================
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/doctor-patient-portal -n doctor-patient-portal
endlocal
