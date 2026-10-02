package com.hms;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.net.URISyntaxException;

/**
 * EmbeddedTomcatLauncher — cr-java-0107: WAR Packaging → Executable JAR
 *
 * <p>This class replaces the external application server requirement by embedding
 * Apache Tomcat directly inside the executable JAR. The application can now be
 * launched with {@code java -jar Doctor-Patient-Portal.jar} without any external
 * servlet container, enabling direct container deployment on Amazon ECS, EKS, or
 * AWS Fargate.</p>
 *
 * <p>The HTTP port is read from the {@code PORT} environment variable (default 8080),
 * following the 12-factor app principle of storing configuration in the environment.</p>
 */
public class EmbeddedTomcatLauncher {

    /** Environment variable name for the HTTP port (12-factor app principle). */
    private static final String PORT_ENV_VAR = "PORT";

    /** Default HTTP port when PORT environment variable is not set. */
    private static final int DEFAULT_PORT = 8080;

    /**
     * Application entry point. Starts an embedded Tomcat instance and blocks
     * until the server is stopped.
     *
     * @param args command-line arguments (not used; configuration via env vars)
     * @throws Exception if Tomcat fails to start
     */
    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        System.out.println("[EmbeddedTomcatLauncher] Starting Doctor-Patient-Portal on port " + port);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        // Configure the HTTP connector explicitly so Tomcat listens on the resolved port
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.getService().addConnector(connector);

        // Locate the webapp directory — works both from IDE and from the fat JAR
        String webappDir = resolveWebappDir();
        System.out.println("[EmbeddedTomcatLauncher] Webapp directory: " + webappDir);

        // Add the web application context at the root path "/"
        Context ctx = tomcat.addWebapp("", new File(webappDir).getAbsolutePath());

        // When running from a fat JAR, compiled classes are on the classpath.
        // Add them as an additional web resource so Tomcat can find servlet classes.
        WebResourceRoot resources = new StandardRoot(ctx);
        File classesDir = resolveClassesDir();
        if (classesDir != null && classesDir.exists()) {
            resources.addPreResources(
                new DirResourceSet(resources, "/WEB-INF/classes",
                    classesDir.getAbsolutePath(), "/"));
            System.out.println("[EmbeddedTomcatLauncher] Classes directory: " + classesDir.getAbsolutePath());
        }
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("[EmbeddedTomcatLauncher] Server started. Listening on port " + port);
        tomcat.getServer().await();
    }

    /**
     * Resolves the HTTP port from the {@code PORT} environment variable.
     * Falls back to {@value #DEFAULT_PORT} if the variable is absent or invalid.
     *
     * @return the resolved port number
     */
    private static int resolvePort() {
        String portEnv = System.getenv(PORT_ENV_VAR);
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                int port = Integer.parseInt(portEnv.trim());
                if (port > 0 && port <= 65535) {
                    return port;
                }
                System.err.println("[EmbeddedTomcatLauncher] Invalid PORT value '" + portEnv
                    + "'; falling back to " + DEFAULT_PORT);
            } catch (NumberFormatException e) {
                System.err.println("[EmbeddedTomcatLauncher] Non-numeric PORT value '" + portEnv
                    + "'; falling back to " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }

    /**
     * Resolves the path to the {@code src/main/webapp} directory.
     * When running from a fat JAR the webapp resources are embedded on the
     * classpath; this method returns the path extracted from the JAR location.
     *
     * @return absolute path to the webapp directory
     */
    private static String resolveWebappDir() {
        // Try to locate webapp relative to the JAR / class location
        try {
            File jarFile = new File(
                EmbeddedTomcatLauncher.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            // When running from IDE: target/classes → go up to project root
            if (jarFile.isDirectory()) {
                // Running from IDE (target/classes)
                File webappDir = new File(jarFile.getParentFile().getParentFile(),
                    "src/main/webapp");
                if (webappDir.exists()) {
                    return webappDir.getAbsolutePath();
                }
                // Fallback: target/Doctor-Patient-Portal (exploded WAR)
                File explodedDir = new File(jarFile.getParentFile(), "Doctor-Patient-Portal");
                if (explodedDir.exists()) {
                    return explodedDir.getAbsolutePath();
                }
            } else {
                // Running from fat JAR: webapp is next to the JAR or embedded
                File webappDir = new File(jarFile.getParentFile(), "webapp");
                if (webappDir.exists()) {
                    return webappDir.getAbsolutePath();
                }
            }
        } catch (URISyntaxException e) {
            System.err.println("[EmbeddedTomcatLauncher] Could not resolve JAR location: " + e.getMessage());
        }

        // Last resort: use current working directory / webapp
        String cwd = System.getProperty("user.dir");
        File fallback = new File(cwd, "src/main/webapp");
        if (fallback.exists()) {
            return fallback.getAbsolutePath();
        }
        return new File(cwd, "webapp").getAbsolutePath();
    }

    /**
     * Resolves the compiled classes directory so Tomcat can load servlet classes
     * when running from the fat JAR (where classes are on the classpath root).
     *
     * @return the classes directory, or {@code null} if it cannot be determined
     */
    private static File resolveClassesDir() {
        try {
            File jarFile = new File(
                EmbeddedTomcatLauncher.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (jarFile.isDirectory()) {
                // Running from IDE: the directory itself is the classes dir
                return jarFile;
            }
            // Running from fat JAR: classes are embedded; return null (already on classpath)
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
