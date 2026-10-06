package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is now backed by Amazon ElastiCache (Redis) via Spring Session.
// The SessionRepositoryFilter (web.xml DelegatingFilterProxy) transparently replaces
// the in-memory session store with Redis, enabling stateless containers on EKS.
import javax.servlet.http.HttpSession;

@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// cz-java-0069: Session is Redis-backed via Spring Session + ElastiCache.
		// req.getSession() returns a Redis-backed session transparently via
		// SessionRepositoryFilter; sessions survive container restarts and scale
		// horizontally across multiple EKS pod instances.
		HttpSession session = req.getSession();
		session.removeAttribute("userObj");
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");
		
	}

	
	
}
