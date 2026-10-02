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
echo "  EKS Deployment - ${APP_NAME}"
echo "=============================================="

# ---- Collect deployment inputs ----
read -rp "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "${AWS_REGION}" ]; then
  echo "ERROR: AWS Region is required."
  exit 1
fi

read -rp "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "${CLUSTER_NAME}" ]; then
  echo "ERROR: EKS Cluster Name is required."
  exit 1
fi

read -rp "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "${IMAGE_URI}" ]; then
  echo "ERROR: Docker image URI is required."
  exit 1
fi

echo ""
echo "--- Application Environment Variables ---"
echo "Press Enter to keep the placeholder (you can update the manifest manually)."

read -rp "Enter DB_HOST (MySQL host) [leave blank to keep placeholder]: " DB_HOST_VAL
read -rp "Enter DB_PORT (MySQL port) [leave blank for 3306]: " DB_PORT_VAL
read -rp "Enter DB_NAME (MySQL database name) [leave blank for hospital]: " DB_NAME_VAL
read -rp "Enter DB_USER (MySQL username) [leave blank to keep placeholder]: " DB_USER_VAL
read -rp "Enter DB_PASSWORD (MySQL password) [leave blank to keep placeholder]: " DB_PASSWORD_VAL
read -rp "Enter REDIS_HOST (ElastiCache endpoint) [leave blank to keep placeholder]: " REDIS_HOST_VAL
read -rp "Enter REDIS_PORT (Redis port) [leave blank for 6379]: " REDIS_PORT_VAL

# Apply defaults
[ -z "${DB_PORT_VAL}" ]    && DB_PORT_VAL="3306"
[ -z "${DB_NAME_VAL}" ]    && DB_NAME_VAL="hospital"
[ -z "${REDIS_PORT_VAL}" ] && REDIS_PORT_VAL="6379"

# ---- Configure kubectl for EKS ----
echo ""
echo "Configuring kubectl for EKS cluster '${CLUSTER_NAME}' in '${AWS_REGION}'..."
aws eks update-kubeconfig --region "${AWS_REGION}" --name "${CLUSTER_NAME}"

echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "ERROR: Cannot connect to EKS cluster."; exit 1; }

# ---- Patch manifests with actual values ----
echo ""
echo "Updating Kubernetes manifests with deployment values..."

# Work on copies to avoid modifying originals permanently
cp "${K8S_DIR}/deployment.yaml" "${K8S_DIR}/deployment.yaml.deploy"

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g"           "${K8S_DIR}/deployment.yaml.deploy"

if [ -n "${DB_HOST_VAL}" ]; then
  sed -i "s|{{DB_HOST}}|${DB_HOST_VAL}|g"         "${K8S_DIR}/deployment.yaml.deploy"
fi
if [ -n "${DB_USER_VAL}" ]; then
  sed -i "s|{{DB_USER}}|${DB_USER_VAL}|g"         "${K8S_DIR}/deployment.yaml.deploy"
fi
if [ -n "${DB_PASSWORD_VAL}" ]; then
  sed -i "s|{{DB_PASSWORD}}|${DB_PASSWORD_VAL}|g" "${K8S_DIR}/deployment.yaml.deploy"
fi
if [ -n "${REDIS_HOST_VAL}" ]; then
  sed -i "s|{{REDIS_HOST}}|${REDIS_HOST_VAL}|g"   "${K8S_DIR}/deployment.yaml.deploy"
fi

sed -i "s|{{DB_PORT}}|${DB_PORT_VAL}|g"           "${K8S_DIR}/deployment.yaml.deploy"
sed -i "s|{{DB_NAME}}|${DB_NAME_VAL}|g"           "${K8S_DIR}/deployment.yaml.deploy"
sed -i "s|{{REDIS_PORT}}|${REDIS_PORT_VAL}|g"     "${K8S_DIR}/deployment.yaml.deploy"

# ---- Apply manifests in order ----
echo ""
echo "Applying Kubernetes manifests..."

echo "  [1/4] Applying namespace..."
kubectl apply -f "${K8S_DIR}/namespace.yaml"

echo "  [2/4] Applying deployment..."
kubectl apply -f "${K8S_DIR}/deployment.yaml.deploy"

echo "  [3/4] Applying service..."
kubectl apply -f "${K8S_DIR}/service.yaml"

echo "  [4/4] Applying ingress..."
kubectl apply -f "${K8S_DIR}/ingress.yaml"

# Clean up temp file
rm -f "${K8S_DIR}/deployment.yaml.deploy"

# ---- Wait for rollout ----
echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/${APP_NAME} -n ${NAMESPACE} --timeout=300s

# ---- Verify resources ----
echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n ${NAMESPACE}

# ---- Display access URL ----
echo ""
echo "Fetching application ingress URL..."
INGRESS_HOST=$(kubectl get ingress ${APP_NAME}-ingress -n ${NAMESPACE} \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")

echo ""
echo "=============================================="
echo "  Deployment Complete!"
echo "  Application: ${APP_NAME}"
echo "  Namespace:   ${NAMESPACE}"
echo "  Image:       ${IMAGE_URI}"
if [ "${INGRESS_HOST}" != "pending" ] && [ -n "${INGRESS_HOST}" ]; then
  echo "  URL:         http://${INGRESS_HOST}"
else
  echo "  URL:         (Ingress hostname pending - check 'kubectl get ingress -n ${NAMESPACE}')"
fi
echo "=============================================="
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/${APP_NAME} -n ${NAMESPACE}"
