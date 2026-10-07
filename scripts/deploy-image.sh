#!/bin/bash
set -e
set -o pipefail

echo "============================================"
echo "  Deploy Doctor Patient Portal to AWS EKS"
echo "============================================"
echo ""

# Prompt for AWS region
read -rp "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "AWS Region is required. Exiting."
  exit 1
fi

# Prompt for EKS cluster name
read -rp "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "EKS Cluster Name is required. Exiting."
  exit 1
fi

# Prompt for Docker image URI
read -rp "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "Docker image URI is required. Exiting."
  exit 1
fi

echo ""
echo "--- Application Environment Variables ---"
echo "Provide values for the following (press Enter to skip / use placeholder):"
echo ""

read -rp "Enter DB_HOST (MySQL host, e.g. mydb.cluster.rds.amazonaws.com): " DB_HOST
[ -z "$DB_HOST" ] && DB_HOST="{{DB_HOST}}"

read -rp "Enter DB_PORT (default 3306): " DB_PORT
[ -z "$DB_PORT" ] && DB_PORT="3306"

read -rp "Enter DB_NAME (default hospital): " DB_NAME
[ -z "$DB_NAME" ] && DB_NAME="hospital"

read -rp "Enter DB_USER (default root): " DB_USER
[ -z "$DB_USER" ] && DB_USER="root"

read -rsp "Enter DB_PASSWORD: " DB_PASSWORD
echo ""
[ -z "$DB_PASSWORD" ] && DB_PASSWORD="{{DB_PASSWORD}}"

read -rp "Enter REDIS_HOST (ElastiCache endpoint): " REDIS_HOST
[ -z "$REDIS_HOST" ] && REDIS_HOST="{{REDIS_HOST}}"

read -rp "Enter REDIS_PORT (default 6379): " REDIS_PORT
[ -z "$REDIS_PORT" ] && REDIS_PORT="6379"

echo ""
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME in $AWS_REGION ..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "Cannot connect to cluster. Exiting."; exit 1; }

echo ""
echo "Updating Kubernetes manifests with provided values..."

# Work on copies to avoid modifying originals permanently
cp kubernetes/deployment.yaml kubernetes/deployment.yaml.bak

sed -i 's|{{IMAGE_URI}}|'"$IMAGE_URI"'|g'       kubernetes/deployment.yaml
sed -i 's|{{DB_HOST}}|'"$DB_HOST"'|g'           kubernetes/deployment.yaml
sed -i 's|{{DB_PORT}}|'"$DB_PORT"'|g'           kubernetes/deployment.yaml
sed -i 's|{{DB_NAME}}|'"$DB_NAME"'|g'           kubernetes/deployment.yaml
sed -i 's|{{DB_USER}}|'"$DB_USER"'|g'           kubernetes/deployment.yaml
sed -i 's|{{DB_PASSWORD}}|'"$DB_PASSWORD"'|g'   kubernetes/deployment.yaml
sed -i 's|{{REDIS_HOST}}|'"$REDIS_HOST"'|g'     kubernetes/deployment.yaml
sed -i 's|{{REDIS_PORT}}|'"$REDIS_PORT"'|g'     kubernetes/deployment.yaml

echo ""
echo "Applying Kubernetes manifests..."
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/doctor-patient-portal -n doctor-patient-portal

echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n doctor-patient-portal

echo ""
echo "Fetching application URL..."
INGRESS_HOST=$(kubectl get ingress doctor-patient-portal-ingress \
  -n doctor-patient-portal \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
echo "Application URL: http://$INGRESS_HOST"

# Restore original deployment.yaml
mv kubernetes/deployment.yaml.bak kubernetes/deployment.yaml

echo ""
echo "============================================"
echo "  Deployment Complete!"
echo "============================================"
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/doctor-patient-portal -n doctor-patient-portal"
