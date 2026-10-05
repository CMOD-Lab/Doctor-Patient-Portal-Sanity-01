# Doctor-Patient-Portal – Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Structure](#project-structure)
4. [Local Development with Docker Compose](#local-development-with-docker-compose)
5. [Build & Push Docker Image](#build--push-docker-image)
6. [AWS EKS Deployment](#aws-eks-deployment)
7. [Configuration Reference](#configuration-reference)
8. [Health Checks](#health-checks)
9. [Scaling & Rolling Updates](#scaling--rolling-updates)
10. [Troubleshooting](#troubleshooting)
11. [Security Considerations](#security-considerations)

---

## Overview

**Application**: Doctor-Patient-Portal  
**Technology**: Java 8, Maven, Servlet/JSP, Spring Session, MySQL, Redis  
**Packaging**: WAR deployed on Apache Tomcat 9  
**Target Platform**: AWS EKS (Elastic Kubernetes Service)  
**Base Runtime Image**: `amazoncorretto:8`

The application is a Hospital Management System (HMS) web portal that supports patient registration, doctor management, and appointment scheduling. Sessions are externalised to Amazon ElastiCache (Redis) via Spring Session so the application scales horizontally on EKS without sticky sessions.

---

## Prerequisites

### Local Development
| Tool | Version | Purpose |
|------|---------|---------|
| Docker | 20.10+ | Build and run containers |
| Docker Compose | 2.x | Local multi-container orchestration |
| Java JDK | 8 | Local compilation (optional) |
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
TestComp1/
├── Dockerfile                    # Multi-stage build (builder + amazoncorretto:8 runtime)
├── docker-compose.yml            # Local development stack (app only)
├── .dockerignore                 # Excludes target/, wrapper files, IDE files
├── pom.xml                       # Maven build descriptor
├── src/
│   └── main/
│       ├── java/com/hms/         # Application source code
│       └── webapp/               # JSP views, WEB-INF/web.xml
├── kubernetes/
│   ├── namespace.yaml            # Kubernetes namespace
│   ├── deployment.yaml           # Deployment with 2 replicas
│   ├── service.yaml              # ClusterIP service (port 80 → 8080)
│   └── ingress.yaml              # AWS ALB Ingress
├── scripts/
│   ├── build-push.sh             # Linux/macOS: build & push to ECR or Docker Hub
│   ├── build-push.bat            # Windows: build & push to ECR or Docker Hub
│   ├── deploy-image.sh           # Linux/macOS: deploy to EKS
│   └── deploy-image.bat          # Windows: deploy to EKS
└── docs/
    └── DEPLOYMENT.md             # This file
```

---

## Local Development with Docker Compose

### 1. Configure environment variables

Create a `.env` file in the project root (never commit this file):

```env
# MySQL (provide your own MySQL instance)
DB_HOST=your-mysql-host
DB_PORT=3306
DB_NAME=hospital
DB_USER=root
DB_PASSWORD=your-password

# Redis (local or ElastiCache)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
SESSION_TIMEOUT_SECONDS=1800
```

### 2. Start the application

```bash
docker compose up --build
```

The application will be available at: **http://localhost:8080**

### 3. Stop the application

```bash
docker compose down
```

---

## Build & Push Docker Image

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
1. Enter an image tag (default: `latest`)
2. Select a registry: **AWS ECR** or **Docker Hub**
3. Provide registry credentials

#### AWS ECR Example
```
Enter image tag [latest]: v1.0.0
Select container registry:
  1) AWS ECR
  2) Docker Hub
Enter choice [1]: 1
Enter AWS Region (e.g. us-east-1): us-east-1
Enter AWS Account ID: 123456789012
```

The script automatically creates the ECR repository if it does not exist.

#### Docker Hub Example
```
Enter image tag [latest]: v1.0.0
Select container registry:
  1) AWS ECR
  2) Docker Hub
Enter choice [1]: 2
Enter Docker Hub username: myusername
Enter Docker Hub password/token: ********
```

---

## AWS EKS Deployment

### Step 1: Create or connect to an EKS Cluster

```bash
# Create a new cluster (optional)
eksctl create cluster \
  --name doctor-patient-portal-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4

# Or update kubeconfig for an existing cluster
aws eks update-kubeconfig --region us-east-1 --name your-cluster-name
```

### Step 2: Install AWS Load Balancer Controller

The Ingress resource uses the AWS Load Balancer Controller. Install it if not already present:

```bash
# Add the EKS chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=your-cluster-name \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### Step 3: Build and push the Docker image

```bash
./scripts/build-push.sh
# Note the full image URI output (e.g. 123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest)
```

### Step 4: Deploy to EKS

#### Linux / macOS
```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

#### Windows
```cmd
scripts\deploy-image.bat
```

The script will prompt for:
- AWS Region
- EKS Cluster Name
- Full Docker image URI
- Database connection details (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
- Redis / ElastiCache connection details (REDIS_HOST, REDIS_PORT, REDIS_PASSWORD)

### Step 5: Verify deployment

```bash
kubectl get pods,svc,ingress -n doctor-patient-portal
```

### Step 6: Access the application

```bash
kubectl get ingress doctor-patient-portal-ingress -n doctor-patient-portal \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Open the returned hostname in your browser.

---

## Configuration Reference

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `DB_HOST` | *(required)* | MySQL hostname (RDS endpoint) |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `hospital` | MySQL database name |
| `DB_USER` | `root` | MySQL username |
| `DB_PASSWORD` | *(required)* | MySQL password |
| `REDIS_HOST` | *(required)* | Redis hostname (ElastiCache endpoint) |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | *(empty)* | Redis auth token (if enabled) |
| `SESSION_TIMEOUT_SECONDS` | `1800` | HTTP session timeout (30 min) |
| `JAVA_OPTS` | `-Xms256m -Xmx512m ...` | JVM startup options |
| `TZ` | `UTC` | Container timezone |

---

## Health Checks

The application exposes a dedicated health endpoint:

```
GET /health
```

**Response (HTTP 200)**:
```json
{"status":"UP","application":"Doctor-Patient-Portal"}
```

This endpoint is used by:
- **Kubernetes liveness probe** – restarts the container if it becomes unresponsive
- **Kubernetes readiness probe** – removes the pod from load balancer rotation until ready
- **AWS ALB health check** – determines whether the target is healthy

### Probe Configuration (deployment.yaml)

```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
  timeoutSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 45
  periodSeconds: 15
  timeoutSeconds: 10
  failureThreshold: 3
```

---

## Scaling & Rolling Updates

### Manual scaling

```bash
kubectl scale deployment doctor-patient-portal \
  --replicas=4 -n doctor-patient-portal
```

### Horizontal Pod Autoscaler (HPA)

```bash
kubectl autoscale deployment doctor-patient-portal \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n doctor-patient-portal
```

### Rolling update (new image)

```bash
kubectl set image deployment/doctor-patient-portal \
  doctor-patient-portal=<NEW_IMAGE_URI> \
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
# Check pod status
kubectl get pods -n doctor-patient-portal

# Describe a failing pod
kubectl describe pod <pod-name> -n doctor-patient-portal

# View container logs
kubectl logs <pod-name> -n doctor-patient-portal
kubectl logs <pod-name> -n doctor-patient-portal --previous
```

### Common issues

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| `CrashLoopBackOff` | JVM OOM or DB connection failure | Check logs; verify DB_HOST/DB_PASSWORD |
| `ImagePullBackOff` | Wrong image URI or missing ECR permissions | Verify image URI; check IAM role |
| `Pending` pods | Insufficient cluster resources | Scale node group or reduce resource requests |
| Health check failing | App not started yet | Increase `initialDelaySeconds` |
| Redis connection error | Wrong REDIS_HOST or auth token | Verify ElastiCache endpoint and REDIS_PASSWORD |

### Ingress not getting an address

```bash
# Check AWS Load Balancer Controller logs
kubectl logs -n kube-system \
  -l app.kubernetes.io/name=aws-load-balancer-controller

# Verify ingress annotations
kubectl describe ingress doctor-patient-portal-ingress \
  -n doctor-patient-portal
```

### Database connectivity

```bash
# Exec into a running pod to test connectivity
kubectl exec -it <pod-name> -n doctor-patient-portal -- /bin/sh
# Then: curl -v telnet://<DB_HOST>:3306
```

---

## Security Considerations

1. **Non-root container**: The application runs as `appuser` (UID 1000) inside the container.
2. **Secrets management**: Use AWS Secrets Manager or Kubernetes Secrets (not plain env vars) for `DB_PASSWORD` and `REDIS_PASSWORD` in production.
3. **Network policies**: Restrict pod-to-pod traffic using Kubernetes NetworkPolicy resources.
4. **TLS termination**: Configure HTTPS on the ALB by adding an ACM certificate ARN annotation:
   ```yaml
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/...
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
   ```
5. **Image scanning**: Enable ECR image scanning on push to detect vulnerabilities.
6. **IRSA**: Use IAM Roles for Service Accounts (IRSA) to grant the pod least-privilege AWS access without static credentials.
7. **Resource limits**: CPU and memory limits are set to prevent noisy-neighbour issues on shared nodes.

---

## Java-Specific Notes

- **JVM container awareness**: `-XX:+UseContainerSupport` ensures the JVM respects cgroup memory limits rather than using host memory.
- **MaxRAMPercentage**: Set to `75.0` so the JVM heap uses at most 75% of the container memory limit (1 Gi → ~768 Mi heap).
- **Graceful shutdown**: `terminationGracePeriodSeconds: 60` gives Tomcat time to drain in-flight requests before the pod is killed.
- **Session externalisation**: Spring Session with Redis (ElastiCache) ensures sessions survive pod restarts and are shared across all replicas — no sticky sessions required.
- **Tomcat 9**: Compatible with Servlet API 4.0 and Java 8. The WAR is deployed as `ROOT.war` so the context path is `/`.
