package com.hms.health;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HealthCheckServlet
 *
 * Provides a simple HTTP GET /health endpoint for Kubernetes liveness and
 * readiness probes on EKS. Returns HTTP 200 with a JSON status payload when
 * the application is running.
 *
 * Example Kubernetes probe configuration:
 *   livenessProbe:
 *     httpGet:
 *       path: /health
 *       port: 8080
 *     initialDelaySeconds: 30
 *     periodSeconds: 10
 *   readinessProbe:
 *     httpGet:
 *       path: /health
 *       port: 8080
 *     initialDelaySeconds: 15
 *     periodSeconds: 5
 */
@WebServlet("/health")
public class HealthCheckServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        out.print("{\"status\":\"UP\",\"application\":\"Doctor-Patient-Portal\"}");
        out.flush();
    }
}
