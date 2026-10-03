#!/bin/bash
set -e

# ============================================================
# build-push.sh  -  Build and push Docker image
# Doctor-Patient-Portal  |  AWS EKS deployment
# ============================================================

PROJECT_NAME="doctor-patient-portal"
DOCKERFILE="Dockerfile"

echo "=============================================="
echo "  Doctor-Patient-Portal - Build & Push"
echo "=============================================="

# ---------- Registry selection ----------
echo ""
echo "Select container registry:"
echo "  1) AWS ECR"
echo "  2) Docker Hub"
read -rp "Enter choice [1 or 2]: " REGISTRY_CHOICE

# ---------- Image tag ----------
read -rp "Enter image tag (default: latest): " IMAGE_TAG_INPUT
IMAGE_TAG=$(echo "${IMAGE_TAG_INPUT}" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9._-' '-' | sed 's/^-*//;s/-*$//')
if [ -z "$IMAGE_TAG" ]; then
  IMAGE_TAG="latest"
fi
echo "Using tag: $IMAGE_TAG"

# ---------- Sanitise image name ----------
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

# ============================================================
# AWS ECR
# ============================================================
if [ "$REGISTRY_CHOICE" = "1" ]; then
  read -rp "Enter AWS region (e.g. us-east-1): " AWS_REGION
  read -rp "Enter AWS account ID: " AWS_ACCOUNT_ID
  read -rp "Enter ECR repository name (default: ${IMAGE_NAME}): " ECR_REPO_INPUT
  ECR_REPO="${ECR_REPO_INPUT:-$IMAGE_NAME}"

  REGISTRY_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
  FULL_IMAGE_NAME="${REGISTRY_URL}/${ECR_REPO}:${IMAGE_TAG}"

  echo ""
  echo "Logging in to ECR..."
  aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$REGISTRY_URL"

  echo "Ensuring ECR repository exists..."
  aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION"

# ============================================================
# Docker Hub
# ============================================================
elif [ "$REGISTRY_CHOICE" = "2" ]; then
  read -rp "Enter Docker Hub username: " DOCKER_USERNAME
  read -rsp "Enter Docker Hub password/token: " DOCKER_PASSWORD
  echo ""
  read -rp "Enter Docker Hub repository (default: ${DOCKER_USERNAME}/${IMAGE_NAME}): " DOCKER_REPO_INPUT
  DOCKER_REPO="${DOCKER_REPO_INPUT:-${DOCKER_USERNAME}/${IMAGE_NAME}}"

  FULL_IMAGE_NAME="${DOCKER_REPO}:${IMAGE_TAG}"

  echo ""
  echo "Logging in to Docker Hub..."
  echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin

else
  echo "Invalid choice. Exiting."
  exit 1
fi

# ============================================================
# Build
# ============================================================
echo ""
echo "Building Docker image: ${FULL_IMAGE_NAME}"
docker build -f "$DOCKERFILE" -t "$FULL_IMAGE_NAME" .

echo ""
echo "Pushing image: ${FULL_IMAGE_NAME}"
docker push "$FULL_IMAGE_NAME"

echo ""
echo "=============================================="
echo "  Build & Push complete!"
echo "  Image: ${FULL_IMAGE_NAME}"
echo "=============================================="
