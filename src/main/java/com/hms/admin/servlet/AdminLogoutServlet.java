package com.hms.admin.servlet;

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

@WebServlet("/adminLogout")
public class AdminLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// cz-java-0069: Session is Redis-backed via Spring Session + ElastiCache.
		// req.getSession() returns a Redis-backed session transparently via
		// SessionRepositoryFilter; sessions survive container restarts on EKS.
		//get session means get "adminObj" and remove it, logout done!
		HttpSession session = req.getSession();
		session.removeAttribute("adminObj");
		//show message after logout
		session.setAttribute("successMsg", "Admin Logout Successfully");
		resp.sendRedirect("admin_login.jsp");
		
		
		
	}

	
}
