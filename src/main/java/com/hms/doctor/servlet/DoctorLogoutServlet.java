package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is transparently backed by Amazon ElastiCache (Redis)
// via Spring Session (spring-session-data-redis + DelegatingFilterProxy in web.xml).
// Sessions survive container restarts and scale horizontally on EKS.
import javax.servlet.http.HttpSession;

@WebServlet("/doctorLogout")
public class DoctorLogoutServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// cz-java-0069: Session is backed by Amazon ElastiCache (Redis) via Spring Session
		// - safe for horizontal scaling on EKS; session survives container restarts.
		HttpSession session = req.getSession();
		session.removeAttribute("doctorObj");
		session.setAttribute("successMsg", "Doctor Logout Successfully.");
		resp.sendRedirect("doctor_login.jsp");
	}
	
	

}
