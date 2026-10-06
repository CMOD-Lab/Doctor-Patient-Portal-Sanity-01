# ============================================================
# Stage 1: Builder
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy dependency descriptor first for layer caching
COPY pom.xml .

# Download all dependencies (cached layer unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy full source tree
COPY src ./src

# Build the WAR, skip tests
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:8-jdk

LABEL maintainer="HMS Team" \
      application="Doctor-Patient-Portal" \
      version="0.0.1-SNAPSHOT"

# Timezone
ENV TZ=UTC

# JVM tuning for containers
ENV JAVA_OPTS="-Xms256m -Xmx512m \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

# Redis / ElastiCache connection (overridden at runtime)
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379

# MySQL / RDS connection (overridden at runtime)
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=hospital
ENV DB_USER=root
ENV DB_PASSWORD=changeme

# Application port
ENV PORT=8080

WORKDIR /opt/tomcat

# Install Tomcat 9 (supports Servlet 4.0 / Java 8)
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget ca-certificates && \
    wget -q https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.82/bin/apache-tomcat-9.0.82.tar.gz -O /tmp/tomcat.tar.gz && \
    tar -xzf /tmp/tomcat.tar.gz -C /opt/tomcat --strip-components=1 && \
    rm /tmp/tomcat.tar.gz && \
    rm -rf /opt/tomcat/webapps/ROOT \
           /opt/tomcat/webapps/examples \
           /opt/tomcat/webapps/docs \
           /opt/tomcat/webapps/host-manager \
           /opt/tomcat/webapps/manager && \
    apt-get remove -y wget && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war /opt/tomcat/webapps/ROOT.war

# Create non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup -d /opt/tomcat appuser && \
    chown -R appuser:appgroup /opt/tomcat

USER appuser

EXPOSE 8080

CMD ["sh", "-c", "JAVA_OPTS=\"${JAVA_OPTS}\" exec /opt/tomcat/bin/catalina.sh run"]
