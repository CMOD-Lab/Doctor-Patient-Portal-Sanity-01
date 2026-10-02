# Doctor-Patient-Portal — Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Analysis](#project-analysis)
4. [Local Development with Docker Compose](#local-development-with-docker-compose)
5. [Build & Push Docker Image](#build--push-docker-image)
6. [AWS EKS Deployment](#aws-eks-deployment)
7. [Kubernetes Manifest Reference](#kubernetes-manifest-reference)
8. [Configuration & Environment Variables](#configuration--environment-variables)
9. [Health Checks](#health-checks)
10. [Scaling & Rolling Updates](#scaling--rolling-updates)
11. [Troubleshooting](#troubleshooting)
12. [Security Considerations](#security-considerations)

---

## Overview

**Doctor-Patient-Portal** is a Java 8 Servlet/JSP web application packaged as a WAR and deployed on Apache Tomcat 9. It provides a multi-role portal (Admin, Doctor, Patient) backed by MySQL and uses Spring Session with Amazon ElastiCache (Redis) for distributed HTTP session management — enabling horizontal scaling on EKS without session loss on container restart.

| Property | Value |
|---|---|
| Language | Java 8 |
| Build Tool | Maven 3.x |
| Package Type | WAR |
| Runtime | Apache Tomcat 9 |
| Application Port | 8080 |
| Health Endpoint | `GET /health` |
| Session Store | Amazon ElastiCache (Redis) |
| Database | MySQL 8 |
| Target Platform | AWS EKS |

---

## Prerequisites

### Local Development
- Docker Desktop 24+ (with Compose v2)
- Java 8 JDK (for local builds outside Docker)
- Maven 3.8+

### AWS EKS Deployment
- AWS CLI v2 configured (`aws configure`)
- `kubectl` 1.27+
- `eksctl` (optional, for cluster creation)
- IAM permissions:
  - `ecr:*` (push images)
  - `eks:DescribeCluster`, `eks:UpdateKubeconfig`
  - `elasticloadbalancing:*` (ALB Ingress Controller)
- AWS Load Balancer Controller installed on the EKS cluster
- Amazon ElastiCache Redis cluster provisioned
- Amazon RDS MySQL instance provisioned (or self-managed MySQL)

---

## Project Analysis

### Technology Stack
- **Framework**: Java Servlet/JSP (javax.servlet-api 4.0.1)
- **Session Management**: Spring Session Data Redis 2.7.4 + Lettuce 6.2.7
- **Database Driver**: MySQL Connector/J 8.0.28
- **Build**: Maven with `maven-war-plugin 3.3.1`
- **Base Image (Runtime)**: `amazoncorretto:8` (explicit)
- **Builder Image**: `maven:3.8.6-openjdk-8-slim`

### Key Source Files
| File | Purpose |
|---|---|
| `src/main/java/com/hms/health/HealthServlet.java` | Health check endpoint (`/health`) |
| `src/main/java/com/hms/config/RedisSessionConfig.java` | Spring Session + ElastiCache config |
| `src/main/java/com/hms/db/DBConnection.java` | MySQL JDBC connection |
| `src/main/webapp/WEB-INF/web.xml` | Servlet/Filter registration |

---

## Local Development with Docker Compose

> **Note**: Docker Compose is for local testing only. External services (MySQL, Redis) must be provided separately or via environment variables.

### Step 1: Configure environment variables

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

### Step 2: Build and start the application

```bash
docker compose up --build
```

### Step 3: Access the application

Open your browser at: [http://localhost:8080](http://localhost:8080)

Health check: [http://localhost:8080/health](http://localhost:8080/health)

### Step 4: Stop the application

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

The script will prompt you to:
1. Enter an image tag (default: `latest`)
2. Select a registry: **AWS ECR** or **Docker Hub**
3. Provide registry credentials

### Windows

```cmd
scripts\build-push.bat
```

### Manual Build (Docker CLI)

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

### Step 1: Install prerequisites

```bash
# AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip && sudo ./aws/install

# kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl && sudo mv kubectl /usr/local/bin/

# eksctl (optional)
curl --silent --location "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
```

### Step 2: Configure AWS credentials

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### Step 3: Create or connect to an EKS cluster

```bash
# Create a new cluster (if needed)
eksctl create cluster \
  --name my-eks-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2

# Or update kubeconfig for an existing cluster
aws eks update-kubeconfig --region us-east-1 --name my-eks-cluster
```

### Step 4: Install AWS Load Balancer Controller

```bash
# Add the EKS chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=my-eks-cluster \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### Step 5: Deploy the application

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
- Docker image URI
- Database and Redis connection details

### Step 6: Verify the deployment

```bash
kubectl get pods,svc,ingress -n doctor-patient-portal
```

### Step 7: Access the application

```bash
# Get the ALB hostname
kubectl get ingress doctor-patient-portal-ingress \
  -n doctor-patient-portal \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Open the returned hostname in your browser.

---

## Kubernetes Manifest Reference

| File | Kind | Description |
|---|---|---|
| `kubernetes/namespace.yaml` | Namespace | Isolates all resources under `doctor-patient-portal` |
| `kubernetes/deployment.yaml` | Deployment | 2 replicas, rolling update, liveness/readiness probes |
| `kubernetes/service.yaml` | Service (ClusterIP) | Internal load balancing on port 80 → 8080 |
| `kubernetes/ingress.yaml` | Ingress (ALB) | Internet-facing ALB with health check on `/health` |

### Placeholder Values in deployment.yaml

| Placeholder | Description |
|---|---|
| `{{IMAGE_URI}}` | Full Docker image URI with tag |
| `{{DB_HOST}}` | MySQL hostname |
| `{{DB_PORT}}` | MySQL port (default: 3306) |
| `{{DB_NAME}}` | MySQL database name (default: hospital) |
| `{{DB_USER}}` | MySQL username |
| `{{DB_PASSWORD}}` | MySQL password |
| `{{REDIS_HOST}}` | ElastiCache Redis endpoint |
| `{{REDIS_PORT}}` | Redis port (default: 6379) |

---

## Configuration & Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL server hostname |
| `DB_PORT` | `3306` | MySQL server port |
| `DB_NAME` | `hospital` | MySQL database name |
| `DB_USER` | `root` | MySQL username |
| `DB_PASSWORD` | `changeme` | MySQL password |
| `REDIS_HOST` | `localhost` | Amazon ElastiCache Redis endpoint |
| `REDIS_PORT` | `6379` | Redis port |
| `JAVA_OPTS` | `-Xms256m -Xmx512m ...` | JVM startup options |
| `TZ` | `UTC` | Container timezone |

### Using Kubernetes Secrets (Recommended for Production)

```bash
kubectl create secret generic doctor-patient-portal-secrets \
  --from-literal=DB_PASSWORD=your-db-password \
  -n doctor-patient-portal
```

Then reference in `deployment.yaml`:
```yaml
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: doctor-patient-portal-secrets
      key: DB_PASSWORD
```

---

## Health Checks

The application exposes a custom health endpoint:

```
GET /health
```

**Response (HTTP 200)**:
```json
{"status":"UP","application":"Doctor-Patient-Portal"}
```

This endpoint is used by:
- **Kubernetes liveness probe**: Restarts the container if unhealthy
- **Kubernetes readiness probe**: Removes the pod from load balancer rotation if not ready
- **AWS ALB health check**: Determines target group health

### Probe Configuration (in deployment.yaml)

```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 60   # Allow Tomcat + app startup time
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

### Manual Scaling

```bash
kubectl scale deployment doctor-patient-portal \
  --replicas=4 \
  -n doctor-patient-portal
```

### Horizontal Pod Autoscaler (HPA)

```bash
kubectl autoscale deployment doctor-patient-portal \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n doctor-patient-portal
```

### Rolling Update (new image)

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

# Describe pod for events
kubectl describe pod <pod-name> -n doctor-patient-portal

# View container logs
kubectl logs <pod-name> -n doctor-patient-portal

# Follow logs
kubectl logs -f <pod-name> -n doctor-patient-portal
```

### Common Issues

| Symptom | Likely Cause | Resolution |
|---|---|---|
| `CrashLoopBackOff` | App fails to start | Check logs; verify DB/Redis connectivity |
| `ImagePullBackOff` | Wrong image URI or missing ECR permissions | Verify image URI and IAM role |
| Liveness probe failing | Tomcat startup slow | Increase `initialDelaySeconds` |
| `Connection refused` to MySQL | Wrong `DB_HOST` | Verify RDS endpoint and security groups |
| Redis connection error | Wrong `REDIS_HOST` | Verify ElastiCache endpoint and VPC security groups |
| Ingress not getting hostname | ALB Controller not installed | Install AWS Load Balancer Controller |

### Exec into a running pod

```bash
kubectl exec -it <pod-name> -n doctor-patient-portal -- /bin/bash
```

### Check service endpoints

```bash
kubectl get endpoints -n doctor-patient-portal
```

### Check ingress events

```bash
kubectl describe ingress doctor-patient-portal-ingress \
  -n doctor-patient-portal
```

---

## Security Considerations

1. **Non-root container**: The application runs as `appuser` (non-root) inside the container.
2. **Secrets management**: Use Kubernetes Secrets or AWS Secrets Manager for DB passwords and Redis credentials — never hardcode them.
3. **Network policies**: Apply Kubernetes NetworkPolicies to restrict pod-to-pod communication.
4. **ECR image scanning**: Enable ECR image scanning on push to detect vulnerabilities.
5. **HTTPS**: Configure the ALB Ingress with an ACM certificate for TLS termination:
   ```yaml
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/...
   ```
6. **IAM Roles for Service Accounts (IRSA)**: Use IRSA to grant the application pod fine-grained AWS permissions without static credentials.
7. **Resource limits**: CPU and memory limits are set to prevent noisy-neighbour issues.
8. **ElastiCache in-transit encryption**: Enable TLS on the ElastiCache cluster and update `REDIS_HOST` to use the TLS endpoint.

---

## Java-Specific Notes

- **JVM Container Awareness**: `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage=75.0` ensure the JVM respects container memory limits rather than using host memory.
- **Graceful Shutdown**: A `preStop` lifecycle hook (`sleep 10`) gives in-flight requests time to complete before the pod is terminated.
- **Tomcat Startup Time**: Java 8 + Tomcat 9 typically takes 30–60 seconds to start. The liveness probe `initialDelaySeconds: 60` accounts for this.
- **Session Persistence**: Spring Session + ElastiCache ensures HTTP sessions survive pod restarts and are shared across all replicas — critical for horizontal scaling on EKS.
- **Explicit Base Image**: `amazoncorretto:8` is used as the runtime base image per project requirements, providing Amazon's optimized OpenJDK 8 distribution.
- **WAR Deployment**: The WAR is deployed as `ROOT.war` in Tomcat so the application is accessible at the root context path (`/`).
