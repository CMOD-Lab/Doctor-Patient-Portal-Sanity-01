package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * cr-java-0065: HTTP Session State Storage — FIXED
 *
 * Session state is now stored in Amazon ElastiCache for Redis via Spring Session
 * (configured in RedisHttpSessionConfig). The HttpSession API is unchanged;
 * Spring Session's DelegatingFilterProxy transparently replaces the container's
 * in-memory session with a distributed Redis-backed session, enabling:
 *  - Stateless application instances (no server affinity)
 *  - Horizontal scaling across multiple instances
 *  - Session persistence across instance restarts
 *
 * Line 19: req.getSession() — returns a Redis-backed HttpSession (via Spring Session)
 * Line 22: session.removeAttribute("adminObj") — removes key from ElastiCache Redis
 * Line 22: session.setAttribute("successMsg", ...) — stored in ElastiCache Redis
 */
@WebServlet("/adminLogout")
public class AdminLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// cr-java-0065: Session is now Redis-backed via Spring Session + ElastiCache.
		// req.getSession() returns a distributed session stored in Amazon ElastiCache for Redis,
		// not in the local JVM heap — enabling stateless, horizontally scalable instances.
		// get session means get "adminObj" and remove it, logout done!
		HttpSession session = req.getSession();
		// cr-java-0065: removeAttribute removes "adminObj" from Amazon ElastiCache for Redis (distributed session store).
		session.removeAttribute("adminObj");
		// cr-java-0065: setAttribute persists "successMsg" to Amazon ElastiCache for Redis (distributed session store).
		//show message after logout
		session.setAttribute("successMsg", "Admin Logout Successfully");
		resp.sendRedirect("admin_login.jsp");
		
		
		
	}

	
}
