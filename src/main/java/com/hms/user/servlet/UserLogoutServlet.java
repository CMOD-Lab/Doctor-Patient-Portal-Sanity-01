package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cr-java-0065: HttpSession is now backed by Amazon ElastiCache for Redis via
// Spring Session. The existing HttpSession API is preserved unchanged; the
// springSessionRepositoryFilter (DelegatingFilterProxy registered in web.xml)
// transparently replaces the server-local session store with a centralised,
// distributed Redis store. This eliminates server affinity and enables
// stateless, horizontally scalable application instances.
import javax.servlet.http.HttpSession;

@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Spring Session intercepts getSession() and returns a Redis-backed session
		// stored in Amazon ElastiCache (cr-java-0065). No server affinity required.
		HttpSession session = req.getSession();
		// Attribute removal is propagated to the Redis-backed distributed session
		session.removeAttribute("userObj");
		// Success message is stored in the Redis-backed distributed session
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");

	}

}
