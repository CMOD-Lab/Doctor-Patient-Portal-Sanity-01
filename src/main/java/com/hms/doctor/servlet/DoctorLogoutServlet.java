package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069 [In-Memory Session Storage]: HttpSession is transparently backed by
// Amazon ElastiCache (Redis) via Spring Session (springSessionRepositoryFilter).
// Sessions survive container restarts and are shared across all horizontal
// replicas on EKS. No in-memory session storage is used.
import javax.servlet.http.HttpSession;

@WebServlet("/doctorLogout")
public class DoctorLogoutServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// cz-java-0069 [In-Memory Session Storage]: req.getSession() returns a Redis-backed
		// session managed by Spring Session + Amazon ElastiCache (Redis) on EKS.
		// The DelegatingFilterProxy (springSessionRepositoryFilter) in web.xml
		// intercepts this call and delegates to the Redis session repository,
		// ensuring session data is externalized and container-restart resilient.
		HttpSession session = req.getSession();
		session.removeAttribute("doctorObj");
		session.setAttribute("successMsg", "Doctor Logout Successfully.");
		resp.sendRedirect("doctor_login.jsp");
	}
	
	

}
