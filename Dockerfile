# ============================================================
# Stage 1: Builder
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy dependency descriptor first for layer caching
COPY pom.xml .

# Download all dependencies (cached layer)
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

# Install Tomcat 9
ENV CATALINA_HOME=/opt/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH
ENV TOMCAT_VERSION=9.0.82

RUN curl -fsSL "https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
    | tar -xz -C /opt \
    && mv /opt/apache-tomcat-${TOMCAT_VERSION} ${CATALINA_HOME} \
    && rm -rf ${CATALINA_HOME}/webapps/ROOT \
    && rm -rf ${CATALINA_HOME}/webapps/examples \
    && rm -rf ${CATALINA_HOME}/webapps/docs \
    && rm -rf ${CATALINA_HOME}/webapps/host-manager \
    && rm -rf ${CATALINA_HOME}/webapps/manager

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser -d /opt/tomcat -s /sbin/nologin appuser

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war ${CATALINA_HOME}/webapps/ROOT.war

# Set ownership
RUN chown -R appuser:appuser ${CATALINA_HOME}

# JVM options for container awareness
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=UTC"

# Application environment variables (override at runtime)
ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=hospital \
    DB_USER=root \
    DB_PASSWORD=changeme \
    REDIS_HOST=localhost \
    REDIS_PORT=6379 \
    TZ=UTC

EXPOSE 8080

USER appuser

CMD ["catalina.sh", "run"]
