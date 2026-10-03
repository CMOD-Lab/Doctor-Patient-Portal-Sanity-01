# Doctor-Patient-Portal – Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Structure](#project-structure)
4. [Local Development with Docker Compose](#local-development-with-docker-compose)
5. [Building and Pushing the Docker Image](#building-and-pushing-the-docker-image)
6. [AWS EKS Deployment](#aws-eks-deployment)
7. [Configuration Reference](#configuration-reference)
8. [Kubernetes Resource Details](#kubernetes-resource-details)
9. [Scaling and Management](#scaling-and-management)
10. [Troubleshooting](#troubleshooting)
11. [Security Considerations](#security-considerations)

---

## Overview

**Application**: Doctor-Patient-Portal  
**Technology**: Java 8, Maven, Servlet/JSP, Spring Session (Redis), MySQL  
**Packaging**: WAR deployed on Apache Tomcat 9  
**Target Platform**: AWS EKS (Elastic Kubernetes Service)  
**Health Endpoint**: `GET /health` → `{"status":"UP","application":"Doctor-Patient-Portal"}`  
**Base Image**: `amazoncorretto:8` (runtime), `maven:3.8.6-openjdk-8-slim` (builder)

---

## Prerequisites

### Local Development
| Tool | Version | Purpose |
|------|---------|---------|
| Docker | 20.10+ | Build and run containers |
| Docker Compose | 2.x | Local container orchestration |
| Java JDK 8 | 1.8+ | Local compilation (optional) |
| Maven | 3.8+ | Local build (optional) |

### AWS EKS Deployment
| Tool | Version | Purpose |
|------|---------|---------|
| AWS CLI | 2.x | AWS authentication and ECR operations |
| kubectl | 1.27+ | Kubernetes cluster management |
| eksctl | 0.150+ | EKS cluster creation (optional) |

### AWS IAM Permissions Required
```
ecr:GetAuthorizationToken
ecr:BatchCheckLayerAvailability
ecr:GetDownloadUrlForLayer
ecr:BatchGetImage
ecr:CreateRepository
ecr:DescribeRepositories
ecr:PutImage
eks:DescribeCluster
eks:ListClusters
```

---

## Project Structure

```
DoctorPatientPortalSanityCMP04/
├── Dockerfile                    # Multi-stage build (maven:3.8.6-openjdk-8-slim → amazoncorretto:8)
├── docker-compose.yml            # Local development (app only)
├── .dockerignore                 # Excludes build artifacts and IDE files
├── pom.xml                       # Maven build descriptor
├── src/
│   └── main/
│       ├── java/com/hms/         # Application source code
│       └── webapp/               # JSP pages and WEB-INF
├── kubernetes/
│   ├── namespace.yaml            # Kubernetes namespace
│   ├── deployment.yaml           # Deployment with 2 replicas
│   ├── service.yaml              # ClusterIP service (port 80 → 8080)
│   └── ingress.yaml              # AWS ALB Ingress
├── scripts/
│   ├── build-push.sh             # Linux/macOS build & push
│   ├── build-push.bat            # Windows build & push
│   ├── deploy-image.sh           # Linux/macOS EKS deploy
│   └── deploy-image.bat          # Windows EKS deploy
└── docs/
    └── DEPLOYMENT.md             # This file
```

---

## Local Development with Docker Compose

### 1. Configure Environment Variables

Create a `.env` file in the project root:

```env
# Redis (use a local Redis instance or ElastiCache endpoint)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MySQL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=hospital
DB_USER=root
DB_PASSWORD=changeme
```

> **Note**: The `docker-compose.yml` contains only the application service.  
> You must provide Redis and MySQL separately (e.g., via managed services or separate compose files).

### 2. Build and Start

```bash
# Build the image
docker compose build

# Start the application
docker compose up -d

# View logs
docker compose logs -f doctor-patient-portal

# Stop
docker compose down
```

### 3. Verify

```bash
curl http://localhost:8080/health
# Expected: {"status":"UP","application":"Doctor-Patient-Portal"}
```

---

## Building and Pushing the Docker Image

### Linux / macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

### Windows

```cmd
scripts\build-push.bat
```

### Script Prompts

| Prompt | Description |
|--------|-------------|
| Registry choice | `1` for AWS ECR, `2` for Docker Hub |
| Image tag | Docker image tag (default: `latest`) |
| AWS region | e.g., `us-east-1` (ECR only) |
| AWS account ID | 12-digit AWS account number (ECR only) |
| ECR repository name | Defaults to `doctor-patient-portal` |
| Docker Hub username/password | Docker Hub credentials (Docker Hub only) |

### Manual Build

```bash
# Build
docker build -t doctor-patient-portal:latest .

# Tag for ECR
docker tag doctor-patient-portal:latest \
  123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest

# Push to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  123456789012.dkr.ecr.us-east-1.amazonaws.com

docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest
```

---

## AWS EKS Deployment

### Step 1: Create or Connect to EKS Cluster

```bash
# Create a new cluster (if needed)
eksctl create cluster \
  --name doctor-patient-portal-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4

# Configure kubectl
aws eks update-kubeconfig \
  --region us-east-1 \
  --name doctor-patient-portal-cluster
```

### Step 2: Install AWS Load Balancer Controller

```bash
# Add the EKS chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=doctor-patient-portal-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### Step 3: Set Up Amazon ElastiCache (Redis)

The application uses Spring Session backed by Redis for distributed session management.

```bash
# Create ElastiCache Redis cluster (via AWS Console or CLI)
aws elasticache create-cache-cluster \
  --cache-cluster-id doctor-portal-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --region us-east-1
```

Note the Redis endpoint for use in the deployment script.

### Step 4: Set Up Amazon RDS (MySQL)

```bash
# Create RDS MySQL instance (via AWS Console or CLI)
aws rds create-db-instance \
  --db-instance-identifier doctor-portal-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0 \
  --master-username admin \
  --master-user-password <password> \
  --allocated-storage 20 \
  --region us-east-1
```

Note the RDS endpoint for use in the deployment script.

### Step 5: Run the Deployment Script

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

**Windows:**
```cmd
scripts\deploy-image.bat
```

### Step 6: Verify Deployment

```bash
# Check pods
kubectl get pods -n doctor-patient-portal

# Check services
kubectl get svc -n doctor-patient-portal

# Check ingress (ALB URL)
kubectl get ingress -n doctor-patient-portal

# View pod logs
kubectl logs -f deployment/doctor-patient-portal -n doctor-patient-portal

# Health check
curl http://<ALB-DNS>/health
```

### Manual Deployment (without script)

```bash
# Apply namespace
kubectl apply -f kubernetes/namespace.yaml

# Replace placeholders and apply deployment
sed 's|{{IMAGE_URI}}|123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest|g' \
    kubernetes/deployment.yaml | \
sed 's|{{REDIS_HOST}}|my-redis.abc123.ng.0001.use1.cache.amazonaws.com|g' | \
sed 's|{{REDIS_PORT}}|6379|g' | \
sed 's|{{REDIS_PASSWORD}}||g' | \
sed 's|{{DB_HOST}}|my-mysql.rds.amazonaws.com|g' | \
sed 's|{{DB_PORT}}|3306|g' | \
sed 's|{{DB_NAME}}|hospital|g' | \
sed 's|{{DB_USER}}|admin|g' | \
sed 's|{{DB_PASSWORD}}|secret|g' | \
kubectl apply -f -

kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/doctor-patient-portal -n doctor-patient-portal
```

---

## Configuration Reference

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_HOST` | `localhost` | Redis/ElastiCache endpoint |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | _(empty)_ | Redis AUTH password |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `hospital` | MySQL database name |
| `DB_USER` | `root` | MySQL username |
| `DB_PASSWORD` | `changeme` | MySQL password |
| `JAVA_OPTS` | See Dockerfile | JVM tuning flags |
| `TZ` | `UTC` | Container timezone |

### JVM Options

```
-Xms256m                                    # Initial heap size
-Xmx512m                                    # Maximum heap size
-XX:+UseContainerSupport                    # Container-aware JVM
-XX:MaxRAMPercentage=75.0                   # Use 75% of container RAM
-Djava.security.egd=file:/dev/./urandom     # Faster SecureRandom
-Dfile.encoding=UTF-8                       # UTF-8 encoding
```

---

## Kubernetes Resource Details

### Deployment (`kubernetes/deployment.yaml`)
- **Replicas**: 2 (for high availability)
- **Strategy**: RollingUpdate (maxSurge: 1, maxUnavailable: 0)
- **Resources**: requests: 250m CPU / 512Mi RAM; limits: 500m CPU / 1Gi RAM
- **Liveness Probe**: `GET /health` after 90s, every 30s
- **Readiness Probe**: `GET /health` after 60s, every 15s
- **Security Context**: runAsNonRoot: true, runAsUser: 1000

### Service (`kubernetes/service.yaml`)
- **Type**: ClusterIP
- **Port**: 80 → 8080 (Tomcat)

### Ingress (`kubernetes/ingress.yaml`)
- **Class**: AWS ALB (internet-facing)
- **Health check path**: `/health`
- **Host**: `doctor-patient-portal.example.com` _(update to your domain)_

### Namespace (`kubernetes/namespace.yaml`)
- **Name**: `doctor-patient-portal`

---

## Scaling and Management

### Horizontal Pod Autoscaler

```bash
kubectl autoscale deployment doctor-patient-portal \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n doctor-patient-portal
```

### Rolling Update

```bash
# Update image
kubectl set image deployment/doctor-patient-portal \
  doctor-patient-portal=<NEW_IMAGE_URI> \
  -n doctor-patient-portal

# Monitor rollout
kubectl rollout status deployment/doctor-patient-portal -n doctor-patient-portal
```

### Rollback

```bash
kubectl rollout undo deployment/doctor-patient-portal -n doctor-patient-portal

# Rollback to specific revision
kubectl rollout undo deployment/doctor-patient-portal \
  --to-revision=2 \
  -n doctor-patient-portal
```

### Scale Manually

```bash
kubectl scale deployment doctor-patient-portal \
  --replicas=4 \
  -n doctor-patient-portal
```

---

## Troubleshooting

### Pod Not Starting

```bash
# Describe pod for events
kubectl describe pod -l app=doctor-patient-portal -n doctor-patient-portal

# Check logs
kubectl logs -l app=doctor-patient-portal -n doctor-patient-portal --previous
```

**Common causes:**
- `ImagePullBackOff`: ECR credentials not configured or image URI incorrect
- `CrashLoopBackOff`: Application startup failure – check logs for Redis/DB connection errors
- `OOMKilled`: Increase memory limits in `deployment.yaml`

### Redis Connection Failure

Ensure the EKS node security group allows outbound traffic to the ElastiCache security group on port 6379.

```bash
# Verify REDIS_HOST is reachable from the pod
kubectl exec -it <pod-name> -n doctor-patient-portal -- \
  sh -c "nc -zv $REDIS_HOST $REDIS_PORT"
```

### Database Connection Failure

Ensure the RDS security group allows inbound traffic from the EKS node security group on port 3306.

```bash
# Check DB_HOST resolution
kubectl exec -it <pod-name> -n doctor-patient-portal -- \
  sh -c "nc -zv $DB_HOST $DB_PORT"
```

### Ingress / ALB Not Provisioned

```bash
# Check ALB controller logs
kubectl logs -n kube-system deployment/aws-load-balancer-controller

# Verify ingress annotations
kubectl describe ingress doctor-patient-portal-ingress -n doctor-patient-portal
```

### Health Check Failing

```bash
# Test health endpoint directly via port-forward
kubectl port-forward svc/doctor-patient-portal-service 8080:80 -n doctor-patient-portal
curl http://localhost:8080/health
```

### Tomcat Startup Slow

Tomcat 9 with Spring Session initialization can take 60–90 seconds on first start. The liveness probe has `initialDelaySeconds: 90` to account for this. If pods are being killed before startup completes, increase this value in `deployment.yaml`.

---

## Security Considerations

1. **Non-root container**: The application runs as `appuser` (UID 1000) inside the container.
2. **Secrets management**: Use AWS Secrets Manager or Kubernetes Secrets for `DB_PASSWORD` and `REDIS_PASSWORD` instead of plain environment variables.
3. **Network policies**: Restrict pod-to-pod communication using Kubernetes NetworkPolicy.
4. **Image scanning**: Enable ECR image scanning on push to detect vulnerabilities.
5. **IRSA**: Use IAM Roles for Service Accounts (IRSA) to grant the pod access to ElastiCache and RDS without static credentials.
6. **TLS**: Configure HTTPS on the ALB Ingress using ACM certificates:
   ```yaml
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/...
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
   ```
7. **Resource limits**: Always set CPU and memory limits to prevent noisy-neighbour issues.

---

## Java-Specific Notes

- **Tomcat 9** is used as the servlet container (supports Servlet 4.0 / Java EE 8).
- **Spring Session** externalises `HttpSession` to Redis, enabling stateless horizontal scaling across EKS pods.
- The `amazoncorretto:8` runtime image is used as specified by the explicit base image parameter.
- JVM container support (`-XX:+UseContainerSupport`) ensures the JVM respects cgroup memory limits rather than using host memory.
- The builder stage uses `maven:3.8.6-openjdk-8-slim` with system `mvn` (never wrapper scripts).
- Maven wrapper files (`mvnw`, `mvnw.cmd`, `.mvn/`) are excluded via `.dockerignore` and never referenced in the Dockerfile.
- Startup probes are not configured; the `initialDelaySeconds` on liveness/readiness probes accounts for Tomcat's warm-up time (~60–90 seconds).
- The health endpoint `GET /health` is served by `HealthCheckServlet` and returns `{"status":"UP","application":"Doctor-Patient-Portal"}`.
