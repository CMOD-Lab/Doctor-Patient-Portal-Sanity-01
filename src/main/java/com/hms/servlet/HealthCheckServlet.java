package com.hms.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Health check endpoint for containerization / Kubernetes liveness and
 * readiness probes on EKS.
 *
 * GET /health  →  HTTP 200  {"status":"UP","application":"Doctor-Patient-Portal"}
 *
 * This endpoint is intentionally lightweight and does NOT require
 * authentication. It is used by the container orchestrator (EKS) to
 * determine whether the application instance is alive and ready to serve
 * traffic.
 *
 * health-check-endpoint: Mandatory containerization requirement.
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
