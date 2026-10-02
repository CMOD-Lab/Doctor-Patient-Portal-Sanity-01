package com.hms.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * health-check-endpoint: Health check servlet for container orchestration (Kubernetes/EKS).
 * Responds to GET /health with HTTP 200 and a JSON status payload.
 * Used by Kubernetes liveness and readiness probes to determine container health.
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
