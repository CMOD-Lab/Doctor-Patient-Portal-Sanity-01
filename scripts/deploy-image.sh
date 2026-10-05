#!/bin/bash
set -e
set -o pipefail

# ============================================================
# deploy-image.sh  -  Deploy Doctor-Patient-Portal to AWS EKS
# ============================================================

APP_NAME="doctor-patient-portal"
NAMESPACE="doctor-patient-portal"
K8S_DIR="kubernetes"

echo "=============================================="
echo "  Doctor-Patient-Portal - Deploy to AWS EKS"
echo "=============================================="

# ---------- AWS / EKS configuration ----------
read -rp "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "ERROR: AWS Region is required."
  exit 1
fi

read -rp "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "ERROR: EKS Cluster Name is required."
  exit 1
fi

read -rp "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "ERROR: Docker image URI is required."
  exit 1
fi

# ---------- Application environment variables ----------
echo ""
echo "--- Application Environment Variables ---"
echo "(Press Enter to use defaults or skip)"

read -rp "Enter DB_HOST (MySQL host, e.g. mydb.cluster.rds.amazonaws.com): " DB_HOST
read -rp "Enter DB_PORT [3306]: " DB_PORT
DB_PORT="${DB_PORT:-3306}"
read -rp "Enter DB_NAME [hospital]: " DB_NAME
DB_NAME="${DB_NAME:-hospital}"
read -rp "Enter DB_USER [root]: " DB_USER
DB_USER="${DB_USER:-root}"
read -rsp "Enter DB_PASSWORD: " DB_PASSWORD
echo ""
read -rp "Enter REDIS_HOST (ElastiCache endpoint): " REDIS_HOST
read -rp "Enter REDIS_PORT [6379]: " REDIS_PORT
REDIS_PORT="${REDIS_PORT:-6379}"
read -rsp "Enter REDIS_PASSWORD (leave blank if none): " REDIS_PASSWORD
echo ""

# ---------- Configure kubectl ----------
echo ""
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME ..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "ERROR: Cannot connect to EKS cluster."; exit 1; }

# ---------- Substitute placeholders in manifests ----------
echo ""
echo "Updating Kubernetes manifests with provided values..."

# Work on copies to avoid modifying originals
TMP_DIR="/tmp/k8s-deploy-$$"
cp -r "$K8S_DIR" "$TMP_DIR"

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g"           "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_HOST}}|${DB_HOST}|g"               "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_PORT}}|${DB_PORT}|g"               "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_NAME}}|${DB_NAME}|g"               "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_USER}}|${DB_USER}|g"               "$TMP_DIR/deployment.yaml"
sed -i "s|{{DB_PASSWORD}}|${DB_PASSWORD}|g"       "$TMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_HOST}}|${REDIS_HOST}|g"         "$TMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_PORT}}|${REDIS_PORT}|g"         "$TMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_PASSWORD}}|${REDIS_PASSWORD}|g" "$TMP_DIR/deployment.yaml"

# ---------- Apply manifests ----------
echo ""
echo "Applying Kubernetes manifests..."

echo "[1/4] Applying namespace..."
kubectl apply -f "$TMP_DIR/namespace.yaml"

echo "[2/4] Applying deployment..."
kubectl apply -f "$TMP_DIR/deployment.yaml"

echo "[3/4] Applying service..."
kubectl apply -f "$TMP_DIR/service.yaml"

echo "[4/4] Applying ingress..."
kubectl apply -f "$TMP_DIR/ingress.yaml"

# ---------- Wait for rollout ----------
echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/"$APP_NAME" -n "$NAMESPACE" --timeout=300s

# ---------- Verify resources ----------
echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n "$NAMESPACE"

# ---------- Display access URL ----------
echo ""
echo "Fetching application URL..."
INGRESS_HOST=$(kubectl get ingress "${APP_NAME}-ingress" -n "$NAMESPACE" \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
if [ "$INGRESS_HOST" != "pending" ] && [ -n "$INGRESS_HOST" ]; then
  echo "Application URL: http://${INGRESS_HOST}"
else
  echo "Ingress hostname is still provisioning. Run the following to check:"
  echo "  kubectl get ingress -n $NAMESPACE"
fi

# ---------- Cleanup temp files ----------
rm -rf "$TMP_DIR"

echo ""
echo "=============================================="
echo "  Deployment complete!"
echo "  Namespace : $NAMESPACE"
echo "  Image     : $IMAGE_URI"
echo "=============================================="
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/$APP_NAME -n $NAMESPACE"
