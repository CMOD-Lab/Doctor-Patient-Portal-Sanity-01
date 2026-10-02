package com.hms.health;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Health check endpoint for container liveness/readiness probes.
 *
 * Responds to GET /health with HTTP 200 and a JSON status payload.
 * This endpoint is required for Kubernetes (EKS) liveness and readiness
 * probes to verify the application is running correctly inside a container.
 *
 * Example response:
 *   {"status":"UP","application":"Doctor-Patient-Portal"}
 */
@WebServlet("/health")
public class HealthServlet extends HttpServlet {

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
