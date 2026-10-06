# Doctor-Patient-Portal - Deployment Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Project Analysis](#project-analysis)
4. [Local Development with Docker Compose](#local-development-with-docker-compose)
5. [Build and Push Docker Image](#build-and-push-docker-image)
6. [AWS EKS Deployment](#aws-eks-deployment)
7. [Kubernetes Manifest Reference](#kubernetes-manifest-reference)
8. [Configuration Management](#configuration-management)
9. [Scaling and Management](#scaling-and-management)
10. [Troubleshooting](#troubleshooting)
11. [Security Considerations](#security-considerations)
12. [Java-Specific Notes](#java-specific-notes)

---

## Overview

**Application**: Doctor-Patient-Portal
**Technology**: Java 8, Maven, Servlet/JSP, Spring Session, Tomcat 9
**Packaging**: WAR deployed on embedded Tomcat 9
**Target Platform**: AWS EKS (Elastic Kubernetes Service)
**Health Endpoint**: `GET /health` -> `{"status":"UP","application":"Doctor-Patient-Portal"}`
**Application Port**: 8080

The application uses:
- **MySQL / Amazon RDS** for persistent data storage
- **Redis / Amazon ElastiCache** for distributed HTTP session storage (Spring Session)

---

## Prerequisites

### Local Development
| Tool | Version | Purpose |
|------|---------|---------|
| Docker | 20.10+ | Build and run containers |
| Docker Compose | 2.x | Local multi-service orchestration |
| Java JDK | 8 | Local compilation (optional) |
| Maven | 3.8+ | Local build (optional) |

### AWS EKS Deployment
| Tool | Version | Purpose |
|------|---------|---------|
| AWS CLI | 2.x | AWS authentication and ECR access |
| kubectl | 1.27+ | Kubernetes cluster management |
| eksctl | 0.150+ | EKS cluster creation (optional) |
| Docker | 20.10+ | Image build and push |

### AWS IAM Permissions Required
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
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:CreateRepository",
        "ecr:DescribeRepositories"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "eks:DescribeCluster",
        "eks:ListClusters"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## Project Analysis

| Property | Value |
|----------|-------|
| Group ID | com.hms |
| Artifact ID | Doctor-Patient-Portal |
| Java Version | 8 |
| Build Tool | Maven 3.8.6 |
| Packaging | WAR |
| Servlet Container | Tomcat 9 |
| Session Store | Redis (Spring Session Data Redis + Lettuce) |
| Database | MySQL 8 (via JDBC) |
| Health Endpoint | `/health` (HealthCheckServlet) |
| Base Image (Runtime) | eclipse-temurin:8-jdk |
| Base Image (Builder) | maven:3.8.6-openjdk-8-slim |

---

## Local Development with Docker Compose

### 1. Configure environment variables

Create a `.env` file in the project root:

```env
# Redis (use a local Redis container or ElastiCache endpoint)
REDIS_HOST=redis
REDIS_PORT=6379

# MySQL (use a local MySQL container or RDS endpoint)
DB_HOST=mysql
DB_PORT=3306
DB_NAME=hospital
DB_USER=root
DB_PASSWORD=yourpassword
```

> **Note**: The `docker-compose.yml` contains only the application service.
> You must provide Redis and MySQL separately (local containers or cloud services).

### 2. Start the application

```bash
docker-compose up --build
```

### 3. Access the application

```
http://localhost:8080
```

### 4. Health check

```bash
curl http://localhost:8080/health
# Expected: {"status":"UP","application":"Doctor-Patient-Portal"}
```

### 5. Stop the application

```bash
docker-compose down
```

---

## Build and Push Docker Image

### Linux / macOS

```bash
chmod +x scripts/build-push.sh
./scripts/build-push.sh
```

The script will prompt for:
1. Image tag (default: `latest`)
2. Registry type: `1) AWS ECR` or `2) Docker Hub`
3. Registry-specific credentials

### Windows

```cmd
scripts\build-push.bat
```

### Manual Docker Build

```bash
# Build
docker build -t doctor-patient-portal:latest .

# Tag for ECR
docker tag doctor-patient-portal:latest \
  <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/doctor-patient-portal:latest

# Push to ECR
aws ecr get-login-password --region <REGION> | \
  docker login --username AWS --password-stdin \
  <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com

docker push <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/doctor-patient-portal:latest
```

---

## AWS EKS Deployment

### Step 1: Configure AWS CLI

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### Step 2: Create or connect to EKS cluster

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

# Or connect to an existing cluster
aws eks update-kubeconfig --region us-east-1 --name <CLUSTER_NAME>
```

### Step 3: Install AWS Load Balancer Controller (for Ingress)

```bash
# Add the EKS chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<CLUSTER_NAME> \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### Step 4: Run the deployment script

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

The script will prompt for:
- AWS region
- EKS cluster name
- Full Docker image URI
- Redis host/port (ElastiCache endpoint)
- Database host/port/name/user/password (RDS endpoint)

### Step 5: Verify deployment

```bash
kubectl get pods -n doctor-patient-portal
kubectl get svc -n doctor-patient-portal
kubectl get ingress -n doctor-patient-portal
```

### Step 6: Access the application

```bash
# Get the ALB DNS name
kubectl get ingress doctor-patient-portal-ingress -n doctor-patient-portal \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

---

## Kubernetes Manifest Reference

### namespace.yaml
Creates the `doctor-patient-portal` namespace to isolate all resources.

### deployment.yaml
- **Replicas**: 2 (for high availability)
- **Image**: `{{IMAGE_URI}}` (replaced by deploy script)
- **Resources**: requests `250m CPU / 512Mi RAM`, limits `500m CPU / 1Gi RAM`
- **Liveness Probe**: `GET /health` after 60s, every 30s
- **Readiness Probe**: `GET /health` after 45s, every 15s
- **Environment Variables**: All external service connections via env vars

### service.yaml
- **Type**: ClusterIP (internal access only)
- **Port**: 80 -> 8080 (container port)

### ingress.yaml
- **Class**: AWS ALB (Application Load Balancer)
- **Scheme**: internet-facing
- **Health Check**: `/health`
- **Host**: `doctor-patient-portal.example.com` (update to your domain)

---

## Configuration Management

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_HOST` | Amazon ElastiCache Redis endpoint | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `DB_HOST` | Amazon RDS MySQL endpoint | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `hospital` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `changeme` |
| `JAVA_OPTS` | JVM options | `-Xms256m -Xmx512m ...` |
| `TZ` | Timezone | `UTC` |

### Using Kubernetes Secrets (Recommended for Production)

```bash
kubectl create secret generic doctor-patient-portal-secrets \
  --from-literal=DB_PASSWORD=<your-db-password> \
  -n doctor-patient-portal
```

Then reference in deployment.yaml:
```yaml
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: doctor-patient-portal-secrets
      key: DB_PASSWORD
```

### Amazon RDS Setup

```bash
# Create RDS MySQL instance
aws rds create-db-instance \
  --db-instance-identifier doctor-patient-portal-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0 \
  --master-username root \
  --master-user-password <password> \
  --allocated-storage 20 \
  --db-name hospital \
  --region us-east-1
```

### Amazon ElastiCache Setup

```bash
# Create ElastiCache Redis cluster
aws elasticache create-cache-cluster \
  --cache-cluster-id doctor-patient-portal-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --region us-east-1
```

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

### Manual scaling

```bash
kubectl scale deployment doctor-patient-portal \
  --replicas=4 \
  -n doctor-patient-portal
```

### Rolling update

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

# Rollback to specific revision
kubectl rollout undo deployment/doctor-patient-portal \
  --to-revision=2 \
  -n doctor-patient-portal
```

---

## Troubleshooting

### Pod not starting

```bash
# Check pod status
kubectl describe pod -l app=doctor-patient-portal -n doctor-patient-portal

# Check logs
kubectl logs -l app=doctor-patient-portal -n doctor-patient-portal --tail=100

# Check events
kubectl get events -n doctor-patient-portal --sort-by='.lastTimestamp'
```

### Health check failing

```bash
# Test health endpoint from within the cluster
kubectl exec -it <POD_NAME> -n doctor-patient-portal -- \
  sh -c 'wget -qO- http://localhost:8080/health'
```

### Redis connection issues

```bash
# Verify REDIS_HOST env var
kubectl exec -it <POD_NAME> -n doctor-patient-portal -- \
  sh -c 'echo $REDIS_HOST'

# Check ElastiCache security group allows inbound on port 6379 from EKS node SG
```

### Database connection issues

```bash
# Verify DB_HOST env var
kubectl exec -it <POD_NAME> -n doctor-patient-portal -- \
  sh -c 'echo $DB_HOST'

# Check RDS security group allows inbound on port 3306 from EKS node SG
```

### Ingress / ALB not provisioning

```bash
# Check AWS Load Balancer Controller logs
kubectl logs -n kube-system \
  -l app.kubernetes.io/name=aws-load-balancer-controller

# Verify IAM permissions for the controller service account
```

### OOMKilled (Out of Memory)

Increase memory limits in `kubernetes/deployment.yaml`:
```yaml
resources:
  limits:
    memory: "2Gi"
```
Also increase JVM heap:
```yaml
- name: JAVA_OPTS
  value: "-Xms512m -Xmx1536m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

---

## Security Considerations

1. **Non-root container**: The application runs as `appuser` (non-root) inside the container.
2. **Secrets management**: Use Kubernetes Secrets or AWS Secrets Manager for sensitive values (DB_PASSWORD, etc.). Never hard-code credentials.
3. **Network policies**: Restrict pod-to-pod communication using Kubernetes NetworkPolicy.
4. **Image scanning**: Enable ECR image scanning to detect vulnerabilities.
5. **IRSA (IAM Roles for Service Accounts)**: Use IRSA to grant the application pod fine-grained AWS permissions without static credentials.
6. **TLS termination**: Configure HTTPS on the ALB using ACM certificates:
   ```yaml
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:<region>:<account>:certificate/<id>
   ```
7. **ElastiCache in-transit encryption**: Enable TLS on ElastiCache and update `REDIS_HOST` to use the TLS endpoint.
8. **RDS encryption**: Enable encryption at rest and in-transit for RDS.
9. **Least privilege IAM**: Grant only the minimum required IAM permissions to EKS node roles and service accounts.
10. **Pod Security Standards**: Apply `restricted` pod security standards to the namespace.

---

## Java-Specific Notes

- **JVM Container Support**: `-XX:+UseContainerSupport` ensures the JVM respects container memory limits (Java 8u191+).
- **MaxRAMPercentage**: Set to 75% so the JVM heap uses up to 75% of the container memory limit.
- **Graceful Shutdown**: `terminationGracePeriodSeconds: 60` gives Tomcat time to finish in-flight requests.
- **Session Persistence**: Spring Session + ElastiCache ensures sessions survive pod restarts and enable horizontal scaling.
- **Startup Time**: Tomcat + Spring Session initialization takes ~30-60 seconds; `initialDelaySeconds` on probes is set accordingly.
- **Base Image**: Uses `eclipse-temurin:8-jdk` as the explicit runtime base image for Java 8 compatibility.
- **WAR Deployment**: The WAR is deployed as `ROOT.war` in Tomcat so the application is accessible at the root context path `/`.
- **Spring Session**: Requires a running Redis instance. Set `REDIS_HOST` and `REDIS_PORT` environment variables to point to your Amazon ElastiCache endpoint.
