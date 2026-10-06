# Doctor-Patient-Portal – Deployment Guide

## Overview

This guide covers building, containerising, and deploying the **Doctor-Patient-Portal** Java web application to **AWS EKS** (Elastic Kubernetes Service).

| Property | Value |
|---|---|
| Application | Doctor-Patient-Portal |
| Group ID | com.hms |
| Artifact ID | Doctor-Patient-Portal |
| Build Tool | Maven 3.x |
| Java Version | 8 |
| Packaging | WAR (deployed on Tomcat 9) |
| Application Port | 8080 |
| Health Endpoint | `/health` |
| Base Image (Builder) | `maven:3.8.6-openjdk-8-slim` |
| Base Image (Runtime) | `openjdk:8-jdk` |
| Target Platform | AWS EKS |

---

## Prerequisites

### Local Development
- Docker Desktop 24+
- Java 8 JDK
- Apache Maven 3.8+

### AWS EKS Deployment
- AWS CLI v2 (`aws --version`)
- `kubectl` 1.27+ (`kubectl version --client`)
- `eksctl` (optional, for cluster creation)
- IAM permissions:
  - `ecr:*` (push images)
  - `eks:DescribeCluster`, `eks:UpdateKubeconfig`
  - `elasticloadbalancing:*` (ALB Ingress Controller)

---

## Project Structure

```
Portal-Monolith/
├── Dockerfile                  # Multi-stage build (Maven builder + openjdk:8-jdk runtime)
├── .dockerignore               # Excludes target/, .git/, wrapper files
├── docker-compose.yml          # Local single-service compose (app only)
├── kubernetes/
│   ├── namespace.yaml          # Namespace: doctor-patient-portal
│   ├── deployment.yaml         # Deployment with 2 replicas, health probes
│   ├── service.yaml            # ClusterIP service (port 80 → 8080)
│   └── ingress.yaml            # AWS ALB Ingress (internet-facing)
├── scripts/
│   ├── build-push.sh           # Linux/macOS build & push
│   ├── build-push.bat          # Windows build & push
│   ├── deploy-image.sh         # Linux/macOS EKS deploy
│   └── deploy-image.bat        # Windows EKS deploy
└── src/
    └── main/
        ├── java/               # Servlet-based Java source
        └── webapp/             # JSP pages, WEB-INF/web.xml
```

---

## Local Development with Docker Compose

### 1. Configure environment variables

Create a `.env` file in the project root (never commit this file):

```env
DB_HOST=<your-mysql-host>
DB_PORT=3306
DB_NAME=hospital
DB_USER=root
DB_PASSWORD=<your-password>
REDIS_HOST=<your-redis-host>
REDIS_PORT=6379
REDIS_PASSWORD=<your-redis-password>
```

> **Note:** The application requires an external MySQL database and a Redis instance (for Spring Session). These are **not** included in `docker-compose.yml`. Provide connection details via environment variables.

### 2. Build and start

```bash
docker-compose up --build
```

### 3. Access the application

```
http://localhost:8080
```

### 4. Health check

```
http://localhost:8080/health
```

Expected response:
```json
{"status":"UP","application":"Doctor-Patient-Portal"}
```

### 5. Stop

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

### Windows

```cmd
scripts\build-push.bat
```

The script will prompt you to:
1. Choose a registry (AWS ECR or Docker Hub)
2. Enter registry credentials / AWS account details
3. Enter an image tag (defaults to `latest`)

The script automatically:
- Sanitises the image name to lowercase with hyphens
- Creates the ECR repository if it does not exist
- Builds the Docker image from the project root
- Pushes the image to the selected registry

---

## AWS EKS Deployment

### Step 1 – Set up AWS CLI

```bash
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region, Output format
```

### Step 2 – Create or connect to an EKS cluster

**Create a new cluster (eksctl):**
```bash
eksctl create cluster \
  --name doctor-patient-portal-cluster \
  --region us-east-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4
```

**Connect to an existing cluster:**
```bash
aws eks update-kubeconfig --region us-east-1 --name <cluster-name>
kubectl cluster-info
```

### Step 3 – Install AWS Load Balancer Controller

The Ingress manifest uses the AWS ALB Ingress Controller. Install it if not already present:

```bash
# Add the EKS chart repository
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install the controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=<cluster-name> \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

Refer to the [official docs](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html) for IAM setup.

### Step 4 – Build and push the image

```bash
./scripts/build-push.sh
# Note the full image URI output, e.g.:
# 123456789012.dkr.ecr.us-east-1.amazonaws.com/doctor-patient-portal:latest
```

### Step 5 – Deploy to EKS

```bash
chmod +x scripts/deploy-image.sh
./scripts/deploy-image.sh
```

The script will prompt for:
- AWS region and EKS cluster name
- Full Docker image URI
- MySQL connection details (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
- Redis / ElastiCache connection details (REDIS_HOST, REDIS_PORT, REDIS_PASSWORD)

It then:
1. Configures `kubectl`
2. Patches all `{{PLACEHOLDER}}` values in the manifests
3. Applies manifests in order: namespace → deployment → service → ingress
4. Waits for the rollout to complete
5. Displays the ALB hostname

### Step 6 – Verify deployment

```bash
kubectl get pods,svc,ingress -n doctor-patient-portal
kubectl logs -l app=doctor-patient-portal -n doctor-patient-portal --tail=50
```

---

## Kubernetes Manifest Reference

### namespace.yaml
Creates the `doctor-patient-portal` namespace to isolate all resources.

### deployment.yaml
- **Replicas:** 2 (horizontal scaling)
- **Image:** `{{IMAGE_URI}}` – replaced at deploy time by `deploy-image.sh`
- **Resources:** requests 250m CPU / 512Mi RAM; limits 500m CPU / 1Gi RAM
- **Liveness probe:** `GET /health` on port 8080, initial delay 60s, period 15s
- **Readiness probe:** `GET /health` on port 8080, initial delay 30s, period 10s
- **Environment variables:** DB_*, REDIS_* injected at deploy time

### service.yaml
ClusterIP service exposing port 80 → container port 8080.

### ingress.yaml
AWS ALB Ingress (internet-facing) routing HTTP traffic to the service.
Update `host: doctor-patient-portal.example.com` to your actual domain.

---

## Scaling

### Manual scaling
```bash
kubectl scale deployment doctor-patient-portal --replicas=4 -n doctor-patient-portal
```

### Horizontal Pod Autoscaler
```bash
kubectl autoscale deployment doctor-patient-portal \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n doctor-patient-portal
```

---

## Rolling Updates and Rollbacks

### Update image
```bash
kubectl set image deployment/doctor-patient-portal \
  doctor-patient-portal=<new-image-uri> \
  -n doctor-patient-portal
kubectl rollout status deployment/doctor-patient-portal -n doctor-patient-portal
```

### Rollback
```bash
kubectl rollout undo deployment/doctor-patient-portal -n doctor-patient-portal
```

---

## Troubleshooting

### Pods not starting
```bash
kubectl describe pod -l app=doctor-patient-portal -n doctor-patient-portal
kubectl logs -l app=doctor-patient-portal -n doctor-patient-portal
```

Common causes:
- **ImagePullBackOff** – ECR credentials or image URI incorrect
- **CrashLoopBackOff** – DB_HOST / REDIS_HOST unreachable; check env vars
- **OOMKilled** – Increase memory limit in `kubernetes/deployment.yaml`

### Health check failing
```bash
# Port-forward to test locally
kubectl port-forward deployment/doctor-patient-portal 8080:8080 -n doctor-patient-portal
curl http://localhost:8080/health
```

Expected response: `{"status":"UP","application":"Doctor-Patient-Portal"}`

### Ingress not getting an address
- Verify AWS Load Balancer Controller is installed and running
- Check IAM permissions for the controller service account
- Inspect controller logs:
  ```bash
  kubectl logs -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller
  ```

### Redis connection errors
- Confirm ElastiCache security group allows inbound port 6379 from EKS node security group
- Verify REDIS_HOST is the primary endpoint (not the reader endpoint)
- Check Spring Session logs for connection refused errors

### MySQL connection errors
- Confirm RDS security group allows inbound port 3306 from EKS node security group
- Verify DB_HOST, DB_USER, DB_PASSWORD, DB_NAME are correct
- Test connectivity: `kubectl exec -it <pod> -n doctor-patient-portal -- nc -zv $DB_HOST 3306`

---

## Configuration Management

### Environment variables reference

| Variable | Description | Default |
|---|---|---|
| `DB_HOST` | MySQL hostname | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `hospital` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `changeme` |
| `REDIS_HOST` | Redis / ElastiCache hostname | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis AUTH password | *(empty)* |
| `JAVA_OPTS` | JVM flags | See Dockerfile |
| `TZ` | Timezone | `UTC` |

### Using Kubernetes Secrets (recommended for production)

```bash
kubectl create secret generic doctor-patient-portal-secrets \
  --from-literal=DB_PASSWORD=<password> \
  --from-literal=REDIS_PASSWORD=<password> \
  -n doctor-patient-portal
```

Then reference in `kubernetes/deployment.yaml`:
```yaml
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: doctor-patient-portal-secrets
      key: DB_PASSWORD
```

---

## Security Considerations

1. **Non-root container** – The application runs as the `tomcat` user (non-root).
2. **Secrets management** – Use Kubernetes Secrets or AWS Secrets Manager; never hardcode credentials.
3. **Network policies** – Restrict pod-to-pod traffic with Kubernetes NetworkPolicy.
4. **Image scanning** – Enable ECR image scanning on push.
5. **HTTPS** – Add TLS termination at the ALB level using ACM certificates:
   ```yaml
   alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
   alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:<region>:<account>:certificate/<id>
   ```
6. **Resource limits** – Always set CPU/memory limits to prevent noisy-neighbour issues.
7. **Read-only filesystem** – Consider adding `readOnlyRootFilesystem: true` with appropriate volume mounts for Tomcat work directories.

---

## Java-Specific Notes

- **JVM container awareness** – `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage=75.0` ensure the JVM respects container memory limits rather than host memory.
- **Spring Session Redis** – Sessions are stored in Redis/ElastiCache, enabling stateless horizontal scaling across multiple pods. Session TTL is 1800 seconds (30 minutes).
- **Graceful shutdown** – `terminationGracePeriodSeconds: 30` gives Tomcat time to drain in-flight requests before the pod is terminated.
- **Entropy** – `-Djava.security.egd=file:/dev/./urandom` prevents slow startup due to entropy starvation in containers.
- **Tomcat version** – Tomcat 9.0.x is used to match the Servlet 4.0 API (`javax.servlet-api:4.0.1`) declared in `pom.xml`.
- **Base image** – Runtime uses `openjdk:8-jdk` as explicitly specified; builder uses `maven:3.8.6-openjdk-8-slim` for a lean build environment.
- **WAR deployment** – The WAR is deployed as `ROOT.war` so the application is accessible at the context root `/` (no context path prefix).
