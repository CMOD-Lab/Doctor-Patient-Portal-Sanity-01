#!/bin/bash
set -e
set -o pipefail

# ============================================================
# deploy-image.sh – Deploy Doctor-Patient-Portal to AWS EKS
# ============================================================

APP_NAME="doctor-patient-portal"
NAMESPACE="doctor-patient-portal"
K8S_DIR="kubernetes"

echo "============================================"
echo "  Doctor-Patient-Portal – EKS Deploy Script"
echo "============================================"

# ---------- AWS Region ----------
read -rp "Enter AWS region [us-east-1]: " AWS_REGION
AWS_REGION="${AWS_REGION:-us-east-1}"

# ---------- EKS Cluster name ----------
read -rp "Enter EKS cluster name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
    echo "ERROR: EKS cluster name is required." >&2
    exit 1
fi

# ---------- Docker image URI ----------
read -rp "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
    echo "ERROR: Docker image URI is required." >&2
    exit 1
fi

# ---------- Application environment variables ----------
echo ""
echo "--- Application Configuration (press Enter to use defaults) ---"

read -rp "Enter DB_HOST (MySQL host) [localhost]: " DB_HOST
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

read -rp "Enter REDIS_HOST (ElastiCache endpoint) [localhost]: " REDIS_HOST
REDIS_HOST="${REDIS_HOST:-localhost}"

read -rp "Enter REDIS_PORT [6379]: " REDIS_PORT
REDIS_PORT="${REDIS_PORT:-6379}"

read -rsp "Enter REDIS_PASSWORD (leave blank if none): " REDIS_PASSWORD
echo ""

# ---------- Configure kubectl ----------
echo ""
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME ..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "ERROR: Cannot connect to cluster." >&2; exit 1; }

# ---------- Patch manifests ----------
echo ""
echo "Patching Kubernetes manifests..."

# Work on copies to avoid modifying originals
TEMP_DIR="/tmp/k8s-deploy-$$"
cp -r "$K8S_DIR" "$TEMP_DIR"

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g"           "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_HOST}}|${DB_HOST}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_PORT}}|${DB_PORT}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_NAME}}|${DB_NAME}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_USER}}|${DB_USER}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_PASSWORD}}|${DB_PASSWORD}|g"       "$TEMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_HOST}}|${REDIS_HOST}|g"         "$TEMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_PORT}}|${REDIS_PORT}|g"         "$TEMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_PASSWORD}}|${REDIS_PASSWORD}|g" "$TEMP_DIR/deployment.yaml"

# ---------- Apply manifests ----------
echo ""
echo "Applying Kubernetes manifests..."

echo "  [1/4] Applying namespace..."
kubectl apply -f "$TEMP_DIR/namespace.yaml"

echo "  [2/4] Applying deployment..."
kubectl apply -f "$TEMP_DIR/deployment.yaml"

echo "  [3/4] Applying service..."
kubectl apply -f "$TEMP_DIR/service.yaml"

echo "  [4/4] Applying ingress..."
kubectl apply -f "$TEMP_DIR/ingress.yaml"

# ---------- Wait for rollout ----------
echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/"$APP_NAME" -n "$NAMESPACE" --timeout=300s || {
    echo "ERROR: Deployment rollout failed. Running rollback..."
    kubectl rollout undo deployment/"$APP_NAME" -n "$NAMESPACE"
    rm -rf "$TEMP_DIR"
    exit 1
}

# ---------- Verify ----------
echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n "$NAMESPACE"

# ---------- Display URL ----------
echo ""
INGRESS_HOST=$(kubectl get ingress doctor-patient-portal-ingress -n "$NAMESPACE" \
    -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
echo "============================================"
echo "  Deployment complete!"
echo "  Application URL: http://${INGRESS_HOST}"
echo "  Health check:    http://${INGRESS_HOST}/health"
echo "  (If URL shows 'pending', wait for ALB provisioning)"
echo "============================================"

# Cleanup temp files
rm -rf "$TEMP_DIR"
