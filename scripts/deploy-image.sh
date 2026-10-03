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
echo "  Doctor-Patient-Portal - EKS Deployment"
echo "=============================================="

# ---------- AWS / EKS configuration ----------
read -rp "Enter AWS region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "ERROR: AWS region is required." && exit 1
fi

read -rp "Enter EKS cluster name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "ERROR: EKS cluster name is required." && exit 1
fi

read -rp "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "ERROR: Image URI is required." && exit 1
fi

# ---------- Application environment variables ----------
echo ""
echo "--- Application Environment Variables ---"
echo "(Press Enter to use defaults)"

read -rp "Enter REDIS_HOST (ElastiCache endpoint, default: localhost): " REDIS_HOST_VAL
read -rp "Enter REDIS_PORT (default: 6379): " REDIS_PORT_VAL
read -rsp "Enter REDIS_PASSWORD (leave blank if none): " REDIS_PASSWORD_VAL
echo ""
read -rp "Enter DB_HOST (MySQL endpoint, default: localhost): " DB_HOST_VAL
read -rp "Enter DB_PORT (default: 3306): " DB_PORT_VAL
read -rp "Enter DB_NAME (default: hospital): " DB_NAME_VAL
read -rp "Enter DB_USER (default: root): " DB_USER_VAL
read -rsp "Enter DB_PASSWORD: " DB_PASSWORD_VAL
echo ""

# Apply defaults
REDIS_HOST_VAL="${REDIS_HOST_VAL:-localhost}"
REDIS_PORT_VAL="${REDIS_PORT_VAL:-6379}"
DB_HOST_VAL="${DB_HOST_VAL:-localhost}"
DB_PORT_VAL="${DB_PORT_VAL:-3306}"
DB_NAME_VAL="${DB_NAME_VAL:-hospital}"
DB_USER_VAL="${DB_USER_VAL:-root}"

# ---------- Configure kubectl ----------
echo ""
echo "Configuring kubectl for cluster: ${CLUSTER_NAME} in ${AWS_REGION}..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "ERROR: Cannot connect to cluster."; exit 1; }

# ---------- Patch manifests ----------
echo ""
echo "Patching Kubernetes manifests..."

# Work on copies to avoid modifying originals
TEMP_DIR="/tmp/k8s-deploy-$$"
cp -r "$K8S_DIR" "$TEMP_DIR"

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_HOST}}|${REDIS_HOST_VAL}|g"         "$TEMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_PORT}}|${REDIS_PORT_VAL}|g"         "$TEMP_DIR/deployment.yaml"
sed -i "s|{{REDIS_PASSWORD}}|${REDIS_PASSWORD_VAL}|g" "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_HOST}}|${DB_HOST_VAL}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_PORT}}|${DB_PORT_VAL}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_NAME}}|${DB_NAME_VAL}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_USER}}|${DB_USER_VAL}|g"               "$TEMP_DIR/deployment.yaml"
sed -i "s|{{DB_PASSWORD}}|${DB_PASSWORD_VAL}|g"       "$TEMP_DIR/deployment.yaml"

# ---------- Apply manifests ----------
echo ""
echo "Applying Kubernetes manifests..."
kubectl apply -f "$TEMP_DIR/namespace.yaml"
kubectl apply -f "$TEMP_DIR/deployment.yaml"
kubectl apply -f "$TEMP_DIR/service.yaml"
kubectl apply -f "$TEMP_DIR/ingress.yaml"

# ---------- Wait for rollout ----------
echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/"${APP_NAME}" -n "${NAMESPACE}" --timeout=300s

# ---------- Verify ----------
echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n "${NAMESPACE}"

# ---------- Display URL ----------
echo ""
INGRESS_HOST=$(kubectl get ingress "${APP_NAME}-ingress" -n "${NAMESPACE}" \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "<pending>")
echo "=============================================="
echo "  Deployment complete!"
echo "  Application URL: http://${INGRESS_HOST}"
echo "  Health check:    http://${INGRESS_HOST}/health"
echo "=============================================="
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/${APP_NAME} -n ${NAMESPACE}"

# Cleanup temp files
rm -rf "$TEMP_DIR"
