# ============================================================
# Stage 1: Builder
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy dependency descriptor first for layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy full source
COPY src ./src

# Build the WAR (skip tests)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:8-jdk

LABEL maintainer="Doctor-Patient-Portal"
LABEL application="doctor-patient-portal"
LABEL version="0.0.1-SNAPSHOT"

# Timezone configuration
ENV TZ=UTC

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin appuser

WORKDIR /app

# Install Tomcat using only tools available in the base image
ENV CATALINA_HOME=/opt/tomcat
ENV TOMCAT_VERSION=9.0.82

RUN mkdir -p /opt/tomcat && \
    cd /tmp && \
    apt-get update && apt-get install -y --no-install-recommends ca-certificates && \
    java -version && \
    apt-get install -y --no-install-recommends wget && \
    wget -q https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
    tar -xzf apache-tomcat-${TOMCAT_VERSION}.tar.gz -C /opt && \
    mv /opt/apache-tomcat-${TOMCAT_VERSION} ${CATALINA_HOME} && \
    rm apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
    rm -rf ${CATALINA_HOME}/webapps/ROOT && \
    rm -rf ${CATALINA_HOME}/webapps/examples && \
    rm -rf ${CATALINA_HOME}/webapps/docs && \
    rm -rf ${CATALINA_HOME}/webapps/host-manager && \
    rm -rf ${CATALINA_HOME}/webapps/manager && \
    apt-get remove -y wget && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war ${CATALINA_HOME}/webapps/ROOT.war

# Set ownership
RUN chown -R appuser:appgroup ${CATALINA_HOME} /app

# JVM options for container awareness
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC"

# Application environment variables (override at runtime)
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=hospital
ENV DB_USER=root
ENV DB_PASSWORD=changeme
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379

# Expose application port
EXPOSE 8080

USER appuser

# Start Tomcat
CMD ["sh", "-c", "${CATALINA_HOME}/bin/catalina.sh run"]
