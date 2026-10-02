package com.hms.launcher;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * EmbeddedTomcatLauncher — main entry point for the self-contained executable JAR.
 *
 * <p>Replaces the external application-server (WAR) deployment model with an
 * embedded Apache Tomcat 9.x instance so the application can be started with:
 * <pre>
 *   java -jar Doctor-Patient-Portal.jar
 * </pre>
 * This enables direct deployment on Amazon ECS, EKS, or AWS Fargate without
 * any external application server, reducing container image size and
 * simplifying deployment automation.
 *
 * <p>Configuration is driven entirely by environment variables following the
 * 12-factor app methodology:
 * <ul>
 *   <li>{@code PORT}          — HTTP port to listen on (default: 8080)</li>
 *   <li>{@code CONTEXT_PATH}  — servlet context path (default: ""  i.e. root)</li>
 * </ul>
 */
public class EmbeddedTomcatLauncher {

    /** Default HTTP port; overridden by the {@code PORT} environment variable. */
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws LifecycleException {

        // ── 1. Resolve configuration from environment variables ──────────────
        int port = DEFAULT_PORT;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("[EmbeddedTomcatLauncher] Invalid PORT value '" + portEnv
                        + "'; falling back to default " + DEFAULT_PORT);
            }
        }

        String contextPath = System.getenv("CONTEXT_PATH");
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "";
        }

        // ── 2. Locate the webapp directory ───────────────────────────────────
        // When running from the exploded project (IDE / mvn exec:java) the
        // webapp resources live under src/main/webapp.  When running from the
        // assembled fat-JAR the resources are extracted to a temp directory by
        // the assembly plugin; fall back to the current working directory.
        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            webappDir = new File(".");
        }

        // ── 3. Create and configure the embedded Tomcat instance ─────────────
        Tomcat tomcat = new Tomcat();

        // Configure the HTTP connector on the resolved port
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        // Add the web application context
        Context ctx = tomcat.addWebapp(contextPath, webappDir.getAbsolutePath());

        // Allow the embedded Tomcat to locate compiled servlet classes when
        // running from the exploded project structure (target/classes).
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(
                    resources,
                    "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(),
                    "/"));
            ctx.setResources(resources);
        }

        // ── 4. Start Tomcat and block until the JVM is shut down ─────────────
        tomcat.start();
        System.out.println("[EmbeddedTomcatLauncher] Server started on port " + port
                + " with context path '" + contextPath + "'");
        tomcat.getServer().await();
    }
}
