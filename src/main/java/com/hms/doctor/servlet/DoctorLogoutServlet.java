package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cr-java-0065: HTTP Session State Storage — FIXED
// import javax.servlet.http.HttpSession is retained; the HttpSession API is unchanged.
// Spring Session's DelegatingFilterProxy (configured in web.xml) transparently replaces
// the container's in-memory session with a distributed Redis-backed session stored in
// Amazon ElastiCache for Redis (configured in RedisHttpSessionConfig), enabling:
//  - Stateless application instances (no server affinity / sticky sessions)
//  - Horizontal scaling across multiple instances behind a load balancer
//  - Session persistence across instance restarts and deployments
import javax.servlet.http.HttpSession;

@WebServlet("/doctorLogout")
public class DoctorLogoutServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// cr-java-0065: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// removeAttribute and setAttribute operate on the distributed Redis session in
		// Amazon ElastiCache, ensuring session state is consistent across all instances.
		HttpSession session = req.getSession();
		session.removeAttribute("doctorObj");
		session.setAttribute("successMsg", "Doctor Logout Successfully.");
		resp.sendRedirect("doctor_login.jsp");
	}
	
	

}
