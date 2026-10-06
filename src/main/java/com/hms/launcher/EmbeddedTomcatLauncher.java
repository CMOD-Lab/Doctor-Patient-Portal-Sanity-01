package com.hms.launcher;

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
 * cr-java-0107: Embedded Tomcat Launcher
 *
 * <p>Replaces the external application-server requirement introduced by WAR packaging.
 * This class starts an embedded Apache Tomcat instance, registers the existing web
 * application (servlets, filters, JSPs) and blocks until the JVM is terminated.
 *
 * <p>The resulting executable JAR can be deployed directly on Amazon ECS, EKS, or
 * AWS Fargate without any external servlet container, satisfying the cloud-native
 * self-contained executable JAR pattern.
 *
 * <p>Configuration is driven by environment variables following 12-factor app
 * principles:
 * <ul>
 *   <li>{@code PORT} – HTTP port to listen on (default: {@code 8080})</li>
 *   <li>{@code WEBAPP_DIR} – path to the exploded webapp directory; when running
 *       from the fat JAR the webapp resources are served from the classpath
 *       (default: {@code src/main/webapp} for IDE / exploded runs)</li>
 * </ul>
 */
public class EmbeddedTomcatLauncher {

    /** Environment variable that controls the HTTP listening port. */
    private static final String ENV_PORT = "PORT";

    /** Default HTTP port used when {@code PORT} is not set. */
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws LifecycleException, URISyntaxException {

        // ----------------------------------------------------------------
        // 1. Resolve HTTP port from environment (12-factor: config in env)
        // ----------------------------------------------------------------
        int port = DEFAULT_PORT;
        String portEnv = System.getenv(ENV_PORT);
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException e) {
                System.err.println("[EmbeddedTomcatLauncher] Invalid PORT value '" + portEnv
                        + "', falling back to " + DEFAULT_PORT);
            }
        }

        // ----------------------------------------------------------------
        // 2. Locate the webapp root directory
        //    When running from the fat JAR the webapp resources (JSPs, static
        //    assets) are embedded on the classpath under /webapp/.  For local
        //    IDE / exploded-WAR runs we fall back to src/main/webapp.
        // ----------------------------------------------------------------
        String webappDirEnv = System.getenv("WEBAPP_DIR");
        String webappDirLocation;
        if (webappDirEnv != null && !webappDirEnv.isEmpty()) {
            webappDirLocation = webappDirEnv;
        } else {
            // Resolve relative to the JAR location so the launcher works from
            // any working directory.
            File jarDir = new File(
                    EmbeddedTomcatLauncher.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()).getParentFile();
            File candidate = new File(jarDir, "src/main/webapp");
            webappDirLocation = candidate.exists() ? candidate.getAbsolutePath()
                    : "src/main/webapp";
        }

        // ----------------------------------------------------------------
        // 3. Configure and start embedded Tomcat
        // ----------------------------------------------------------------
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));

        // HTTP/1.1 connector
        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        // Register the web application context at the root path "/"
        Context ctx = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

        // Make compiled classes available to the web application so that
        // servlets registered in web.xml are found at runtime.
        WebResourceRoot resources = new StandardRoot(ctx);
        File classesDir = new File("target/classes");
        if (classesDir.exists()) {
            resources.addPreResources(new DirResourceSet(
                    resources, "/WEB-INF/classes",
                    classesDir.getAbsolutePath(), "/"));
        }
        ctx.setResources(resources);

        // ----------------------------------------------------------------
        // 4. Start and await
        // ----------------------------------------------------------------
        tomcat.start();
        System.out.println("[EmbeddedTomcatLauncher] Doctor-Patient-Portal started on port " + port);
        tomcat.getServer().await();
    }
}
