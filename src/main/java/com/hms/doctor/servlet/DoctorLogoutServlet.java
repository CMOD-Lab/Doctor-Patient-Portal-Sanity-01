package com.hms.doctor.servlet;

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

@WebServlet("/doctorLogout")
public class DoctorLogoutServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Spring Session intercepts getSession() and returns a Redis-backed session
		HttpSession session = req.getSession();
		// Removes attribute from Redis-backed distributed session (cr-java-0065)
		session.removeAttribute("doctorObj");
		// Success message stored in Redis-backed distributed session (cr-java-0065)
		session.setAttribute("successMsg", "Doctor Logout Successfully.");
		resp.sendRedirect("doctor_login.jsp");
	}
	
	

}
