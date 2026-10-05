# ============================================================
# Stage 1: Build
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy dependency descriptor first for layer caching
COPY pom.xml .

# Download all dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy the full source tree (wrapper files are excluded via .dockerignore)
COPY src ./src

# Build the WAR, skip tests
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM amazoncorretto:8

# Metadata
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

# Tomcat environment
ENV CATALINA_HOME=/opt/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

# Install Tomcat 9 (compatible with Servlet 4.0 / Java 8)
RUN yum install -y tar gzip && \
    curl -fsSL https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.82/bin/apache-tomcat-9.0.82.tar.gz \
         -o /tmp/tomcat.tar.gz && \
    mkdir -p $CATALINA_HOME && \
    tar -xzf /tmp/tomcat.tar.gz --strip-components=1 -C $CATALINA_HOME && \
    rm /tmp/tomcat.tar.gz && \
    rm -rf $CATALINA_HOME/webapps/ROOT \
           $CATALINA_HOME/webapps/examples \
           $CATALINA_HOME/webapps/docs \
           $CATALINA_HOME/webapps/host-manager \
           $CATALINA_HOME/webapps/manager && \
    yum clean all

# Create non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup -d /opt/tomcat appuser && \
    chown -R appuser:appgroup $CATALINA_HOME

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war $CATALINA_HOME/webapps/ROOT.war

# Expose application port
EXPOSE 8080

USER appuser

# Start Tomcat
CMD ["catalina.sh", "run"]
