package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// HttpSession is now backed by Amazon ElastiCache for Redis via Spring Session
// (cr-java-0065). The existing HttpSession API is preserved; Spring Session's
// springSessionRepositoryFilter (registered in web.xml) transparently replaces
// the server-local session store with a distributed Redis store, enabling
// stateless, horizontally scalable instances without server affinity.
import javax.servlet.http.HttpSession;

@WebServlet("/adminLogout")
public class AdminLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Spring Session intercepts getSession() and returns a Redis-backed session
		// get session means get "adminObj" and remove it, logout done!
		HttpSession session = req.getSession();
		// Removes attribute from Redis-backed distributed session (cr-java-0065)
		session.removeAttribute("adminObj");
		//show message after logout
		// Success message stored in Redis-backed session (cr-java-0065)
		session.setAttribute("successMsg", "Admin Logout Successfully");
		resp.sendRedirect("admin_login.jsp");
		
		
		
	}

	
}
