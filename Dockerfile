# ============================================================
# Stage 1: Builder
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the WAR artifact
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:8-jdk

LABEL maintainer="HMS Team" \
      application="Doctor-Patient-Portal" \
      version="0.0.1-SNAPSHOT"

# Set timezone
ENV TZ=UTC

# Tomcat environment
ENV CATALINA_HOME=/usr/local/tomcat
ENV CATALINA_BASE=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

WORKDIR /usr/local/tomcat

# Copy Tomcat from the official Tomcat image (avoids curl/wget)
COPY --from=tomcat:9.0.82-jdk8-temurin /usr/local/tomcat /usr/local/tomcat

# Remove default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
           /usr/local/tomcat/webapps/examples \
           /usr/local/tomcat/webapps/docs \
           /usr/local/tomcat/webapps/host-manager \
           /usr/local/tomcat/webapps/manager

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war /usr/local/tomcat/webapps/ROOT.war

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown -R appuser:appuser /usr/local/tomcat

USER appuser

# Application port (Tomcat default)
EXPOSE 8080

# JVM options for container awareness
ENV JAVA_OPTS="-Xms256m -Xmx512m \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=UTC"

# Start Tomcat
CMD ["sh", "-c", "exec ${CATALINA_HOME}/bin/catalina.sh run"]
