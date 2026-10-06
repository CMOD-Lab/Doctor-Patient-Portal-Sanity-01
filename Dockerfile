# ============================================================
# Stage 1: Builder
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /workspace

# Copy dependency descriptor first for layer caching
COPY pom.xml .

# Download all dependencies (cached layer unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy full source tree (wrapper files are excluded via .dockerignore)
COPY src ./src

# Build the WAR, skip tests
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM openjdk:8-jdk

# Metadata
LABEL maintainer="HMS Team" \
      application="Doctor-Patient-Portal" \
      version="0.0.1-SNAPSHOT"

# Timezone
ENV TZ=UTC

# Install Tomcat 9 (supports Servlet 4.0 / Java 8)
ENV CATALINA_HOME=/opt/tomcat
ENV CATALINA_BASE=/opt/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH
ENV TOMCAT_VERSION=9.0.82

RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r tomcat && useradd -r -g tomcat -d $CATALINA_HOME -s /sbin/nologin tomcat \
    && mkdir -p $CATALINA_HOME \
    && cd /tmp \
    && apt-get update && apt-get install -y --no-install-recommends wget \
    && wget -q "https://archive.apache.org/dist/tomcat/tomcat-9/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
    && tar -xzf "apache-tomcat-${TOMCAT_VERSION}.tar.gz" --strip-components=1 -C $CATALINA_HOME \
    && rm "apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
    && apt-get remove -y wget && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf $CATALINA_HOME/webapps/ROOT \
    && rm -rf $CATALINA_HOME/webapps/examples \
    && rm -rf $CATALINA_HOME/webapps/docs \
    && rm -rf $CATALINA_HOME/webapps/host-manager \
    && rm -rf $CATALINA_HOME/webapps/manager \
    && chown -R tomcat:tomcat $CATALINA_HOME

# Copy WAR from builder stage
COPY --from=builder /workspace/target/Doctor-Patient-Portal.war $CATALINA_HOME/webapps/ROOT.war

# Adjust ownership
RUN chown tomcat:tomcat $CATALINA_HOME/webapps/ROOT.war

# JVM tuning for containers
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
    REDIS_PASSWORD=""

# Expose HTTP port
EXPOSE 8080

# Run as non-root
USER tomcat

# Start Tomcat
CMD ["catalina.sh", "run"]
