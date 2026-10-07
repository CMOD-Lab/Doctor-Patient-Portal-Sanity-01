package com.hms.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Health Check Endpoint - health-check-endpoint
 *
 * Provides a simple HTTP GET /health endpoint for container liveness and
 * readiness probes on EKS. Returns HTTP 200 with a JSON status payload
 * when the application is running correctly.
 *
 * Example response:
 * {
 *   "status": "UP",
 *   "application": "Doctor-Patient-Portal",
 *   "version": "0.0.1-SNAPSHOT"
 * }
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
        out.print("{");
        out.print("\"status\":\"UP\",");
        out.print("\"application\":\"Doctor-Patient-Portal\",");
        out.print("\"version\":\"0.0.1-SNAPSHOT\"");
        out.print("}");
        out.flush();
    }
}
