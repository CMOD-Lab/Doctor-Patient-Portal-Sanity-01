package com.hms.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Health Check Endpoint
 *
 * Provides a simple HTTP GET /health endpoint for container liveness probes
 * on Kubernetes / EKS. Returns HTTP 200 with a JSON status payload so that
 * orchestrators (e.g. ALB Ingress, Kubernetes readiness/liveness probes) can
 * verify the application is running.
 *
 * Example response:
 *   {"status":"UP","application":"Doctor-Patient-Portal"}
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
