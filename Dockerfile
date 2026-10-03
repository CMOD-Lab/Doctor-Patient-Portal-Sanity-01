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
FROM amazoncorretto:8

LABEL maintainer="hms-team" \
      application="doctor-patient-portal" \
      version="0.0.1-SNAPSHOT"

# Timezone
ENV TZ=UTC

# JVM tuning
ENV JAVA_OPTS="-Xms256m -Xmx512m \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dfile.encoding=UTF-8"

# Redis / session environment variables (overridden at runtime)
ENV REDIS_HOST=localhost \
    REDIS_PORT=6379 \
    REDIS_PASSWORD=""

# Database environment variables (overridden at runtime)
ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=hospital \
    DB_USER=root \
    DB_PASSWORD=changeme

# Install Tomcat 9 (supports Servlet 4.0 / Java 8)
ENV CATALINA_HOME=/opt/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

RUN set -eux; \
    TOMCAT_VERSION=9.0.82; \
    TOMCAT_URL="https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz"; \
    yum install -y tar gzip; \
    curl -fsSL "$TOMCAT_URL" -o /tmp/tomcat.tar.gz; \
    mkdir -p "$CATALINA_HOME"; \
    tar -xzf /tmp/tomcat.tar.gz --strip-components=1 -C "$CATALINA_HOME"; \
    rm /tmp/tomcat.tar.gz; \
    rm -rf "$CATALINA_HOME/webapps/ROOT" \
           "$CATALINA_HOME/webapps/examples" \
           "$CATALINA_HOME/webapps/docs" \
           "$CATALINA_HOME/webapps/host-manager" \
           "$CATALINA_HOME/webapps/manager"; \
    yum remove -y tar gzip && yum clean all; \
    # Create non-root user \
    groupadd -r appuser && useradd -r -g appuser -d /opt/tomcat -s /sbin/nologin appuser; \
    chown -R appuser:appuser "$CATALINA_HOME"

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war $CATALINA_HOME/webapps/ROOT.war

# Expose application port
EXPOSE 8080

USER appuser

# Graceful shutdown support
STOPSIGNAL SIGTERM

CMD ["catalina.sh", "run"]
