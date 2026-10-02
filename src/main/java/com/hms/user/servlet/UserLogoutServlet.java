package com.hms.user.servlet;

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

@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// cr-java-0065: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// removeAttribute and setAttribute operate on the distributed Redis session in
		// Amazon ElastiCache, ensuring session state is consistent across all instances.
		HttpSession session = req.getSession();

		// cr-java-0065: Removes the user object from the distributed Redis session,
		// effectively logging out the user across all instances.
		session.removeAttribute("userObj");

		// cr-java-0065: Success message stored in Redis-backed session for cross-instance access.
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");

	}

}
