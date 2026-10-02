#!/bin/bash
set -e
set -o pipefail

# ============================================================
# deploy-image.sh - Deploy Doctor-Patient-Portal to AWS EKS
# ============================================================

APP_NAME="doctor-patient-portal"
NAMESPACE="doctor-patient-portal"
K8S_DIR="kubernetes"

echo "=============================================="
echo " Doctor-Patient-Portal - EKS Deploy Script"
echo "=============================================="
echo ""

# Prompt for AWS and EKS details
read -p "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "ERROR: AWS Region is required."
  exit 1
fi

read -p "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "ERROR: EKS Cluster Name is required."
  exit 1
fi

read -p "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "ERROR: Docker image URI is required."
  exit 1
fi

echo ""
echo "--- Application Configuration ---"
echo "Provide values for environment variables (press Enter to use default):"
echo ""

read -p "Enter DB_HOST (MySQL host, e.g. mydb.cluster.us-east-1.rds.amazonaws.com): " DB_HOST
DB_HOST="${DB_HOST:-localhost}"

read -p "Enter DB_PORT [3306]: " DB_PORT
DB_PORT="${DB_PORT:-3306}"

read -p "Enter DB_NAME [hospital]: " DB_NAME
DB_NAME="${DB_NAME:-hospital}"

read -p "Enter DB_USER [root]: " DB_USER
DB_USER="${DB_USER:-root}"

read -s -p "Enter DB_PASSWORD: " DB_PASSWORD
echo ""
DB_PASSWORD="${DB_PASSWORD:-changeme}"

read -p "Enter REDIS_HOST (ElastiCache endpoint): " REDIS_HOST
REDIS_HOST="${REDIS_HOST:-localhost}"

read -p "Enter REDIS_PORT [6379]: " REDIS_PORT
REDIS_PORT="${REDIS_PORT:-6379}"

echo ""
echo "--- Configuring kubectl for EKS ---"
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"
if [ $? -ne 0 ]; then
  echo "ERROR: Failed to configure kubectl for EKS cluster."
  exit 1
fi

echo ""
echo "--- Verifying cluster connectivity ---"
kubectl cluster-info || { echo "ERROR: Cannot connect to EKS cluster."; exit 1; }

echo ""
echo "--- Updating Kubernetes manifests ---"

# Create working copies
cp -r "${K8S_DIR}" "${K8S_DIR}_deploy_tmp"

# Replace placeholders using pipe delimiter
sed -i 's|{{IMAGE_URI}}|'"${IMAGE_URI}"'|g'       "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{DB_HOST}}|'"${DB_HOST}"'|g'           "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{DB_PORT}}|'"${DB_PORT}"'|g'           "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{DB_NAME}}|'"${DB_NAME}"'|g'           "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{DB_USER}}|'"${DB_USER}"'|g'           "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{DB_PASSWORD}}|'"${DB_PASSWORD}"'|g'   "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{REDIS_HOST}}|'"${REDIS_HOST}"'|g'     "${K8S_DIR}_deploy_tmp/deployment.yaml"
sed -i 's|{{REDIS_PORT}}|'"${REDIS_PORT}"'|g'     "${K8S_DIR}_deploy_tmp/deployment.yaml"

echo ""
echo "--- Applying Kubernetes manifests ---"

echo "Applying namespace..."
kubectl apply -f "${K8S_DIR}_deploy_tmp/namespace.yaml"

echo "Applying deployment..."
kubectl apply -f "${K8S_DIR}_deploy_tmp/deployment.yaml"

echo "Applying service..."
kubectl apply -f "${K8S_DIR}_deploy_tmp/service.yaml"

echo "Applying ingress..."
kubectl apply -f "${K8S_DIR}_deploy_tmp/ingress.yaml"

# Clean up temp copies
rm -rf "${K8S_DIR}_deploy_tmp"

echo ""
echo "--- Waiting for deployment rollout ---"
kubectl rollout status deployment/${APP_NAME} -n ${NAMESPACE} --timeout=300s
if [ $? -ne 0 ]; then
  echo "ERROR: Deployment rollout failed. Running rollback..."
  kubectl rollout undo deployment/${APP_NAME} -n ${NAMESPACE}
  echo "Rollback initiated. Check pod status with:"
  echo "  kubectl get pods -n ${NAMESPACE}"
  exit 1
fi

echo ""
echo "--- Verifying deployed resources ---"
kubectl get pods,svc,ingress -n ${NAMESPACE}

echo ""
echo "--- Application Access ---"
INGRESS_HOST=$(kubectl get ingress ${APP_NAME}-ingress -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
if [ "$INGRESS_HOST" != "pending" ] && [ -n "$INGRESS_HOST" ]; then
  echo "Application URL: http://${INGRESS_HOST}"
else
  echo "Ingress hostname is still provisioning. Check later with:"
  echo "  kubectl get ingress -n ${NAMESPACE}"
fi

echo ""
echo "=============================================="
echo " SUCCESS: Deployment complete!"
echo " Namespace : ${NAMESPACE}"
echo " Image     : ${IMAGE_URI}"
echo "=============================================="
echo ""
echo "Useful commands:"
echo "  kubectl get pods -n ${NAMESPACE}"
echo "  kubectl logs -f deployment/${APP_NAME} -n ${NAMESPACE}"
echo "  kubectl rollout undo deployment/${APP_NAME} -n ${NAMESPACE}  # rollback"
