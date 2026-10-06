#!/usr/bin/env bash
# =============================================================================
# deploy-image.sh  -  Deploy Doctor-Patient-Portal to AWS EKS
# =============================================================================
set -e
set -o pipefail

APP_NAME="doctor-patient-portal"
NAMESPACE="doctor-patient-portal"

echo "=============================================="
echo "  Doctor-Patient-Portal - Deploy to AWS EKS"
echo "=============================================="
echo ""

# -- AWS / EKS configuration --------------------------------------------------
read -rp "Enter AWS region [us-east-1]: " AWS_REGION
AWS_REGION="${AWS_REGION:-us-east-1}"

read -rp "Enter EKS cluster name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "ERROR: EKS cluster name is required." >&2
  exit 1
fi

# -- Docker image URI ----------------------------------------------------------
read -rp "Enter full Docker image URI (e.g. 123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "ERROR: Docker image URI is required." >&2
  exit 1
fi

# -- Application environment variables ----------------------------------------
echo ""
echo "--- Application Configuration (press Enter to keep default) ---"

read -rp "Enter REDIS_HOST (Amazon ElastiCache endpoint) [localhost]: " REDIS_HOST
REDIS_HOST="${REDIS_HOST:-localhost}"

read -rp "Enter REDIS_PORT [6379]: " REDIS_PORT
REDIS_PORT="${REDIS_PORT:-6379}"

read -rp "Enter DB_HOST (Amazon RDS endpoint) [localhost]: " DB_HOST
DB_HOST="${DB_HOST:-localhost}"

read -rp "Enter DB_PORT [3306]: " DB_PORT
DB_PORT="${DB_PORT:-3306}"

read -rp "Enter DB_NAME [hospital]: " DB_NAME
DB_NAME="${DB_NAME:-hospital}"

read -rp "Enter DB_USER [root]: " DB_USER
DB_USER="${DB_USER:-root}"

read -rsp "Enter DB_PASSWORD: " DB_PASSWORD
echo ""
DB_PASSWORD="${DB_PASSWORD:-changeme}"

# -- Configure kubectl ---------------------------------------------------------
echo ""
echo "Configuring kubectl for cluster: $CLUSTER_NAME in $AWS_REGION ..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity ..."
kubectl cluster-info || { echo "ERROR: Cannot connect to EKS cluster." >&2; exit 1; }

# -- Patch manifests -----------------------------------------------------------
echo ""
echo "Updating Kubernetes manifests ..."

DEPLOY_YAML="kubernetes/deployment.yaml"

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g"       "$DEPLOY_YAML"
sed -i "s|{{REDIS_HOST}}|${REDIS_HOST}|g"     "$DEPLOY_YAML"
sed -i "s|{{REDIS_PORT}}|${REDIS_PORT}|g"     "$DEPLOY_YAML"
sed -i "s|{{DB_HOST}}|${DB_HOST}|g"           "$DEPLOY_YAML"
sed -i "s|{{DB_PORT}}|${DB_PORT}|g"           "$DEPLOY_YAML"
sed -i "s|{{DB_NAME}}|${DB_NAME}|g"           "$DEPLOY_YAML"
sed -i "s|{{DB_USER}}|${DB_USER}|g"           "$DEPLOY_YAML"
sed -i "s|{{DB_PASSWORD}}|${DB_PASSWORD}|g"   "$DEPLOY_YAML"

# -- Apply manifests -----------------------------------------------------------
echo ""
echo "Applying Kubernetes manifests ..."

kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# -- Wait for rollout ----------------------------------------------------------
echo ""
echo "Waiting for deployment rollout ..."
kubectl rollout status deployment/"$APP_NAME" -n "$NAMESPACE" --timeout=300s

# -- Verify --------------------------------------------------------------------
echo ""
echo "Deployment status:"
kubectl get pods,svc,ingress -n "$NAMESPACE"

# -- Application URL -----------------------------------------------------------
echo ""
echo "Fetching application URL ..."
INGRESS_HOST=$(kubectl get ingress "${APP_NAME}-ingress" -n "$NAMESPACE" \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
echo "Application URL: http://${INGRESS_HOST}"

echo ""
echo "Deployment complete!"
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/$APP_NAME -n $NAMESPACE"
