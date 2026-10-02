@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: build-push.bat - Build and push Docker image for
:: Doctor-Patient-Portal to AWS ECR or Docker Hub
:: ============================================================

set "PROJECT_NAME=doctor-patient-portal"
set "DOCKERFILE_PATH=Dockerfile"

echo ==============================================
echo  Doctor-Patient-Portal - Build ^& Push Script
echo ==============================================
echo.

set /p IMAGE_TAG="Enter image tag [latest]: "
if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

echo.
echo Select container registry:
echo   1. AWS ECR
echo   2. Docker Hub
set /p REGISTRY_CHOICE="Enter choice [1 or 2]: "

echo.

if "!REGISTRY_CHOICE!"=="1" (
    set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
    set /p AWS_ACCOUNT_ID="Enter AWS Account ID: "
    set "ECR_REPO=!PROJECT_NAME!"
    set "REGISTRY_URL=!AWS_ACCOUNT_ID!.dkr.ecr.!AWS_REGION!.amazonaws.com"
    set "FULL_IMAGE_NAME=!REGISTRY_URL!/!ECR_REPO!:!IMAGE_TAG!"

    echo.
    echo Logging in to AWS ECR...
    aws ecr get-login-password --region !AWS_REGION! | docker login --username AWS --password-stdin !REGISTRY_URL!
    if !ERRORLEVEL! neq 0 (
        echo ERROR: ECR login failed.
        exit /b 1
    )

    echo Ensuring ECR repository exists...
    aws ecr describe-repositories --repository-names !ECR_REPO! --region !AWS_REGION! >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Creating ECR repository...
        aws ecr create-repository --repository-name !ECR_REPO! --region !AWS_REGION!
    )

) else if "!REGISTRY_CHOICE!"=="2" (
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    set /p DOCKER_PASSWORD="Enter Docker Hub password/token: "
    set "REGISTRY_URL=docker.io"
    set "FULL_IMAGE_NAME=!DOCKER_USERNAME!/!PROJECT_NAME!:!IMAGE_TAG!"

    echo.
    echo Logging in to Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo ERROR: Docker Hub login failed.
        exit /b 1
    )

) else (
    echo ERROR: Invalid choice. Please enter 1 or 2.
    exit /b 1
)

echo.
echo Building Docker image: !FULL_IMAGE_NAME!
docker build -f "!DOCKERFILE_PATH!" -t "!FULL_IMAGE_NAME!" .
if !ERRORLEVEL! neq 0 (
    echo ERROR: Docker build failed.
    exit /b 1
)

echo.
echo Pushing Docker image: !FULL_IMAGE_NAME!
docker push "!FULL_IMAGE_NAME!"
if !ERRORLEVEL! neq 0 (
    echo ERROR: Docker push failed.
    exit /b 1
)

echo.
echo ==============================================
echo  SUCCESS: Image pushed successfully!
echo  Image: !FULL_IMAGE_NAME!
echo ==============================================

endlocal
