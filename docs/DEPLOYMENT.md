# Doctor-Patient-Portal — Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Structure](#project-structure)
4. [Local Development with Docker Compose](#local-development-with-docker-compose)
5. [Build and Push Docker Image](#build-and-push-docker-image)
6. [AWS EKS Deployment](#aws-eks-deployment)
7. [Configuration Reference](#configuration-reference)
8. [Kubernetes Resource Details](#kubernetes-resource-details)
9. [Scaling and Management](#scaling-and-management)
10. [Troubleshooting](#troubleshooting)
11. [Security Considerations](#security-considerations)

---

## Overview

**Doctor-Patient-Portal** is a Java 8 web application (WAR) built with Maven and deployed on Apache Tomcat 9. It provides a hospital management portal for doctors, patients, and administrators. Sessions are externalized to Amazon ElastiCache (Redis) via Spring Session for horizontal scalability on EKS.

| Property | Value |
|---|---|
| Language | Java 8 |
| Build Tool | Maven |
| Packaging | WAR (deployed on Tomcat 9) |
| Application Port | 8080 |
| Health Endpoint | `/health` |
| Session Store | Redis (Spring Session) |
| Database | MySQL |
| Runtime Base Image | eclipse-temurin:8-jdk |

---

## Prerequisites

### Local Development
- Docker Desktop 24.x or later
- Docker Compose v2.x or later
- Java 8 JDK (for local builds outside Docker)
- Maven 3.8+ (for local builds outside Docker)

### AWS EKS Deployment
- AWS CLI v2 configured with appropriate IAM permissions
- `kubectl` v1.27+
- `eksctl` (optional, for cluster creation)
- An existing EKS cluster with:
  - AWS Load Balancer Controller installed
  - IAM OIDC provider configured
- Amazon ECR repository (auto-created by `build-push.sh`)
- Amazon RDS MySQL instance (or compatible)
- Amazon ElastiCache Redis cluster

### Required IAM Permissions
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:CreateRepository",
        "ecr:DescribeRepositories",
        "eks:DescribeCluster",
        "eks:UpdateKubeconfig"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## Project Structure

```
Doctor-Patient-Portal-MContMo/
├── Dockerfile                    # Multi-stage Docker build (Java 8 + Tomcat 9)
├── docker-compose.yml            # Local development compose file (app only)
├── .dockerignore                 # Docker build exclusions
├── pom.xml                       # Maven build descriptor
├── src/
│   └── main/
│       ├── java/com/hms/         # Java source code
│       └── webapp/               # JSP pages, WEB-INF config
├── kubernetes/
│   ├── namespace.yaml            # Kubernetes namespace
│   ├── deployment.yaml           # Application deployment
│   ├── service.yaml              # ClusterIP service
│   └── ingress.yaml              # ALB ingress
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
DB_HOST=your-mysql-host
DB_PORT=3306
DB_NAME=hospital
DB_USER=root
DB_PASSWORD=your-password
REDIS_HOST=your-redis-host
REDIS_PORT=6379
```

> **Note**: The `docker-compose.yml` contains only the application service. You must provide MySQL and Redis externally (e.g., local instances, Docker containers started separately, or cloud services).

### 2. Build and Start

```bash
# Build the image
docker compose build

# Start the application
docker compose up -d

# View logs
docker compose logs -f doctor-patient-portal
```

### 3. Access the Application

- Application: http://localhost:8080
- Health check: http://localhost:8080/health

### 4. Stop the Application

```bash
docker compose down
```

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
1. Enter an image tag (default: `latest`)
2. Select registry type (AWS ECR or Docker Hub)
3. Provide registry credentials

**AWS ECR Example:**
```
Enter image tag [latest]: v1.0.0
Select container registry:
  1. AWS ECR
  2. Docker Hub
Enter choice [1 or 2]: 1
Enter AWS Region (e.g. us-east-1): us-east-1
Enter AWS Account ID: 123456789012
```

The script automatically creates the ECR repository if it does not exist.

---

## AWS EKS Deployment

### Step 1: Verify EKS Cluster Access

```bash
aws eks update-kubeconfig --region us-east-1 --name my-eks-cluster
kubectl cluster-info
kubectl get nodes
```

### Step 2: Install AWS Load Balancer Controller (if not installed)

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

### Step 3: Run the Deploy Script

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

**Windows:**
```cmd
scripts\deploy-image.bat
```

The script will prompt for:
- AWS Region
- EKS Cluster Name
- Full Docker image URI (e.g., `123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:v1.0.0`)
- Database connection details (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
- Redis connection details (REDIS_HOST, REDIS_PORT)

### Step 4: Verify Deployment

```bash
# Check pods
kubectl get pods -n doctor-patient-portal

# Check services
kubectl get svc -n doctor-patient-portal

# Check ingress (ALB provisioning may take 2-3 minutes)
kubectl get ingress -n doctor-patient-portal

# View application logs
kubectl logs -f deployment/doctor-patient-portal -n doctor-patient-portal
```

### Step 5: Access the Application

```bash
# Get the ALB hostname
kubectl get ingress doctor-patient-portal-ingress -n doctor-patient-portal \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

Navigate to `http://<ALB_HOSTNAME>` in your browser.

---

## Configuration Reference

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_HOST` | MySQL database hostname | `localhost` |
| `DB_PORT` | MySQL database port | `3306` |
| `DB_NAME` | MySQL database name | `hospital` |
| `DB_USER` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `changeme` |
| `REDIS_HOST` | Redis/ElastiCache hostname | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JAVA_OPTS` | JVM options | `-Xms256m -Xmx512m -XX:+UseContainerSupport` |
| `TZ` | Timezone | `UTC` |

### JVM Configuration

The application uses the following JVM flags for container-aware memory management:

```
-Xms256m                          # Initial heap size
-Xmx512m                          # Maximum heap size
-XX:+UseContainerSupport          # Enable container memory awareness
-XX:MaxRAMPercentage=75.0         # Use 75% of container memory for heap
-Djava.security.egd=file:/dev/./urandom  # Faster random number generation
-Dfile.encoding=UTF-8             # UTF-8 encoding
-Duser.timezone=UTC               # UTC timezone
```

---

## Kubernetes Resource Details

### Namespace
- Name: `doctor-patient-portal`

### Deployment
- Replicas: 2 (for high availability)
- Strategy: RollingUpdate (zero-downtime deployments)
- Image: Configurable via `{{IMAGE_URI}}` placeholder

### Resource Limits

| Resource | Request | Limit |
|---|---|---|
| CPU | 250m | 500m |
| Memory | 512Mi | 1Gi |

### Health Probes

| Probe | Path | Port | Initial Delay | Period |
|---|---|---|---|---|
| Liveness | `/health` | 8080 | 60s | 30s |
| Readiness | `/health` | 8080 | 45s | 15s |

> The `/health` endpoint is served by `HealthCheckServlet` and returns `{"status":"UP","application":"Doctor-Patient-Portal"}`.

### Service
- Type: `ClusterIP`
- Port: 80 → 8080

### Ingress
- Class: `alb` (AWS Load Balancer Controller)
- Scheme: `internet-facing`
- Health check path: `/health`

---

## Scaling and Management

### Manual Scaling

```bash
kubectl scale deployment doctor-patient-portal --replicas=4 -n doctor-patient-portal
```

### Horizontal Pod Autoscaler (HPA)

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
# Rollback to previous version
kubectl rollout undo deployment/doctor-patient-portal -n doctor-patient-portal

# Rollback to specific revision
kubectl rollout undo deployment/doctor-patient-portal \
  --to-revision=2 \
  -n doctor-patient-portal

# View rollout history
kubectl rollout history deployment/doctor-patient-portal -n doctor-patient-portal
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
- `ImagePullBackOff`: ECR credentials not configured on nodes. Ensure the node IAM role has `ecr:GetAuthorizationToken` and `ecr:BatchGetImage` permissions.
- `CrashLoopBackOff`: Application startup failure. Check logs for database or Redis connection errors.
- `OOMKilled`: Increase memory limits in `deployment.yaml`.

### Database Connection Issues

Verify the MySQL RDS security group allows inbound traffic from the EKS node security group on port 3306.

```bash
# Test connectivity from a pod
kubectl run -it --rm debug --image=busybox --restart=Never -n doctor-patient-portal -- \
  sh -c "nc -zv $DB_HOST 3306"
```

### Redis Connection Issues

Verify the ElastiCache security group allows inbound traffic from the EKS node security group on port 6379.

```bash
kubectl run -it --rm debug --image=busybox --restart=Never -n doctor-patient-portal -- \
  sh -c "nc -zv $REDIS_HOST 6379"
```

### Ingress / ALB Not Provisioning

```bash
# Check AWS Load Balancer Controller logs
kubectl logs -n kube-system deployment/aws-load-balancer-controller

# Verify ingress annotations
kubectl describe ingress doctor-patient-portal-ingress -n doctor-patient-portal
```

Ensure the AWS Load Balancer Controller is installed and the EKS cluster has the correct IAM policies attached.

### Health Check Failures

```bash
# Test health endpoint directly
kubectl port-forward deployment/doctor-patient-portal 8080:8080 -n doctor-patient-portal
curl http://localhost:8080/health
# Expected: {"status":"UP","application":"Doctor-Patient-Portal"}
```

---

## Security Considerations

1. **Non-root container**: The application runs as a non-root user (`appuser`) inside the container.
2. **Secrets management**: Use AWS Secrets Manager or Kubernetes Secrets for sensitive values (DB_PASSWORD, etc.) rather than plain environment variables in production.
3. **Network policies**: Consider adding Kubernetes NetworkPolicies to restrict pod-to-pod communication.
4. **TLS termination**: Configure HTTPS on the ALB by adding the `alb.ingress.kubernetes.io/certificate-arn` annotation with your ACM certificate ARN.
5. **Image scanning**: Enable ECR image scanning to detect vulnerabilities in the container image.
6. **RBAC**: Apply least-privilege RBAC policies for the application's service account.

### Enable HTTPS on ALB

Add to `kubernetes/ingress.yaml` annotations:

```yaml
alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:123456789012:certificate/your-cert-id
alb.ingress.kubernetes.io/ssl-redirect: '443'
```

### Using Kubernetes Secrets

```bash
kubectl create secret generic doctor-patient-portal-secrets \
  --from-literal=DB_PASSWORD=your-password \
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

### Java-Specific Security Notes

- The JVM is configured with `-Djava.security.egd=file:/dev/./urandom` for faster and secure random number generation in containers.
- Tomcat's default webapps (examples, docs, manager, host-manager) are removed from the image to reduce attack surface.
- The `eclipse-temurin:8-jdk` base image is used as the explicit runtime image, providing a well-maintained and regularly patched JDK distribution.
- Consider upgrading to Java 11 or 17 for long-term support and improved security features when feasible.
