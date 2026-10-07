# Doctor Patient Portal — Deployment Guide

## Overview

This guide covers building, containerizing, and deploying the **Doctor-Patient-Portal** Java web application to **AWS EKS (Elastic Kubernetes Service)**.

- **Application**: Doctor-Patient-Portal
- **Technology**: Java 8, Maven, Servlet/JSP, Tomcat 9
- **Packaging**: WAR deployed on Tomcat 9
- **Session Store**: Spring Session + Redis (Amazon ElastiCache)
- **Database**: MySQL (Amazon RDS recommended)
- **Target Platform**: AWS EKS
- **Base Image**: eclipse-temurin:8-jdk (explicit)

---

## Prerequisites

### Local Development
| Tool | Version | Purpose |
|------|---------|---------|
| Docker | 20.10+ | Build and run containers |
| Docker Compose | 2.x | Local application orchestration |
| Java JDK | 8 | Local builds (optional) |
| Maven | 3.8+ | Local builds (optional) |

### AWS EKS Deployment
| Tool | Version | Purpose |
|------|---------|---------|
| AWS CLI | 2.x | AWS authentication and ECR |
| kubectl | 1.27+ | Kubernetes cluster management |
| eksctl | 0.150+ | EKS cluster creation (optional) |

### IAM Permissions Required
- `ecr:GetAuthorizationToken`, `ecr:BatchCheckLayerAvailability`, `ecr:PutImage`
- `eks:DescribeCluster`, `eks:ListClusters`
- `elasticloadbalancing:*` (for ALB Ingress Controller)

---

## Project Structure

```
doctor/
├── Dockerfile                  # Multi-stage build (Maven builder + eclipse-temurin:8-jdk + Tomcat 9)
├── .dockerignore               # Excludes build artifacts and wrapper files
├── docker-compose.yml          # Local development (app only)
├── kubernetes/
│   ├── namespace.yaml          # Kubernetes namespace
│   ├── deployment.yaml         # Deployment with health probes
│   ├── service.yaml            # ClusterIP service
│   └── ingress.yaml            # AWS ALB Ingress
├── scripts/
│   ├── build-push.sh           # Linux/macOS build & push
│   ├── build-push.bat          # Windows build & push
│   ├── deploy-image.sh         # Linux/macOS EKS deploy
│   └── deploy-image.bat        # Windows EKS deploy
└── docs/
    └── DEPLOYMENT.md           # This file
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | MySQL host (RDS endpoint) | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | MySQL database name | `hospital` |
| `DB_USER` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | *(required)* |
| `REDIS_HOST` | Redis/ElastiCache endpoint | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JAVA_OPTS` | JVM options | `-Xms256m -Xmx512m ...` |
| `TZ` | Timezone | `UTC` |

---

## Local Development with Docker Compose

### 1. Configure environment

Create a `.env` file in the project root:

```env
DB_HOST=your-mysql-host
DB_PORT=3306
DB_NAME=hospital
DB_USER=root
DB_PASSWORD=your-password
REDIS_HOST=your-redis-host
REDIS_PORT=6379
```

### 2. Build and start the application

```bash
docker compose up --build
```

### 3. Access the application

- Application: http://localhost:8080
- Health check: http://localhost:8080/health

### 4. Stop the application

```bash
docker compose down
```

> **Note**: The `docker-compose.yml` contains only the application service.
> You must provide MySQL and Redis separately (local instances or cloud services).

---

## Build and Push Docker Image

### Linux / macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

### Windows

```cmd
scripts\build-push.bat
```

The script will prompt you to:
1. Enter an image tag (defaults to `latest`)
2. Choose registry: **AWS ECR** or **Docker Hub**
3. Provide registry credentials

### Manual Build (ECR example)

```bash
AWS_ACCOUNT_ID=123456789012
AWS_REGION=us-east-1
IMAGE_TAG=latest

aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin \
  ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

aws ecr create-repository --repository-name doctor-patient-portal --region $AWS_REGION

docker build -t ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/doctor-patient-portal:${IMAGE_TAG} .
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/doctor-patient-portal:${IMAGE_TAG}
```

---

## AWS EKS Deployment

### Step 1: Prerequisites

```bash
# Configure AWS CLI
aws configure

# Verify EKS cluster access
aws eks list-clusters --region us-east-1

# Update kubeconfig
aws eks update-kubeconfig --region us-east-1 --name YOUR_CLUSTER_NAME

# Verify connectivity
kubectl cluster-info
kubectl get nodes
```

### Step 2: Install AWS Load Balancer Controller (if not installed)

```bash
# Add EKS chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=YOUR_CLUSTER_NAME \
  --set serviceAccountName=aws-load-balancer-controller
```

### Step 3: Deploy using the script

**Linux / macOS:**
```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

**Windows:**
```cmd
scripts\deploy-image.bat
```

The script will prompt for:
- AWS Region and EKS cluster name
- Full Docker image URI
- Database connection details (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
- Redis connection details (REDIS_HOST, REDIS_PORT)

### Step 4: Manual deployment

```bash
# Replace placeholders in deployment.yaml before applying
sed -i 's|{{IMAGE_URI}}|YOUR_IMAGE_URI|g' kubernetes/deployment.yaml
sed -i 's|{{DB_HOST}}|YOUR_DB_HOST|g' kubernetes/deployment.yaml
sed -i 's|{{DB_PORT}}|3306|g' kubernetes/deployment.yaml
sed -i 's|{{DB_NAME}}|hospital|g' kubernetes/deployment.yaml
sed -i 's|{{DB_USER}}|YOUR_DB_USER|g' kubernetes/deployment.yaml
sed -i 's|{{DB_PASSWORD}}|YOUR_DB_PASSWORD|g' kubernetes/deployment.yaml
sed -i 's|{{REDIS_HOST}}|YOUR_REDIS_HOST|g' kubernetes/deployment.yaml
sed -i 's|{{REDIS_PORT}}|6379|g' kubernetes/deployment.yaml

# Apply manifests in order
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/doctor-patient-portal -n doctor-patient-portal

# Verify
kubectl get pods,svc,ingress -n doctor-patient-portal
```

---

## Kubernetes Manifest Descriptions

### namespace.yaml
Creates the `doctor-patient-portal` namespace to isolate all resources.

### deployment.yaml
- **Replicas**: 2 (high availability)
- **Image**: `{{IMAGE_URI}}` — replaced at deploy time
- **Health probes**: HTTP GET `/health` (liveness: 60s delay, readiness: 45s delay)
- **Resources**: requests 250m CPU / 512Mi RAM; limits 500m CPU / 1Gi RAM
- **JVM**: `-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

### service.yaml
ClusterIP service exposing port 80 → container port 8080.

### ingress.yaml
AWS ALB Ingress with:
- `alb.ingress.kubernetes.io/scheme: internet-facing`
- Health check path: `/health`
- Host: `doctor-patient-portal.example.com` (update to your domain)

---

## Health Check Endpoint

The application exposes a custom health endpoint at `/health`:

```
GET /health
HTTP 200 OK
Content-Type: application/json

{"status":"UP","application":"Doctor-Patient-Portal","version":"0.0.1-SNAPSHOT"}
```

This endpoint is used by:
- Kubernetes liveness probe (fails → pod restart)
- Kubernetes readiness probe (fails → removed from load balancer)
- AWS ALB health checks

---

## Scaling and Management

### Horizontal scaling

```bash
kubectl scale deployment doctor-patient-portal \
  --replicas=4 -n doctor-patient-portal
```

### Horizontal Pod Autoscaler (HPA)

```bash
kubectl autoscale deployment doctor-patient-portal \
  --cpu-percent=70 --min=2 --max=10 \
  -n doctor-patient-portal
```

### Rolling update

```bash
kubectl set image deployment/doctor-patient-portal \
  doctor-patient-portal=NEW_IMAGE_URI \
  -n doctor-patient-portal

kubectl rollout status deployment/doctor-patient-portal \
  -n doctor-patient-portal
```

### Rollback

```bash
kubectl rollout undo deployment/doctor-patient-portal \
  -n doctor-patient-portal
```

---

## Troubleshooting

### Pod not starting

```bash
kubectl describe pod -l app=doctor-patient-portal -n doctor-patient-portal
kubectl logs -l app=doctor-patient-portal -n doctor-patient-portal --previous
```

### Common issues

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `CrashLoopBackOff` | DB/Redis unreachable | Check `DB_HOST`, `REDIS_HOST` env vars |
| `ImagePullBackOff` | Wrong image URI or ECR auth | Verify image URI and ECR permissions |
| Readiness probe failing | App slow to start | Increase `initialDelaySeconds` |
| 502 from ALB | Pod not ready | Check readiness probe and pod logs |
| Session not persisting | Redis misconfigured | Verify `REDIS_HOST` and `REDIS_PORT` |

### Check service endpoints

```bash
kubectl get endpoints doctor-patient-portal-service -n doctor-patient-portal
```

### Check ingress

```bash
kubectl describe ingress doctor-patient-portal-ingress -n doctor-patient-portal
```

---

## Security Considerations

1. **Non-root container**: The application runs as `appuser` (non-root UID).
2. **Secrets management**: Use Kubernetes Secrets or AWS Secrets Manager for `DB_PASSWORD`.
3. **Network policies**: Restrict pod-to-pod traffic with Kubernetes NetworkPolicy.
4. **ECR image scanning**: Enable ECR image scanning on push.
5. **TLS**: Configure HTTPS on the ALB Ingress with an ACM certificate:
   ```yaml
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:REGION:ACCOUNT:certificate/CERT_ID
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
   ```
6. **Redis TLS**: For production ElastiCache, enable in-transit encryption and update `REDIS_HOST` accordingly.

---

## Java-Specific Notes

- **Java 8** with `eclipse-temurin:8-jdk` runtime image (explicit base image).
- **Tomcat 9** serves the WAR at the root context (`/`) — copied from `tomcat:9.0.82-jdk8-temurin` image in multi-stage build (no curl/wget required).
- **Spring Session + Redis**: All HTTP sessions are stored in ElastiCache, enabling stateless horizontal scaling.
- **JVM container support**: `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage=75.0` ensure the JVM respects container memory limits.
- **Graceful shutdown**: `terminationGracePeriodSeconds: 30` allows in-flight requests to complete.
- **Charset**: `-Dfile.encoding=UTF-8` ensures consistent character encoding.
- **Build tool**: Maven 3.8.6 (`mvn` system command — no wrapper scripts used).
