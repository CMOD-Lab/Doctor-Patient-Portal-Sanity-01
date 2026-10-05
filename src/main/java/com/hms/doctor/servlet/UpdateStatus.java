package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.AppointmentDAO;
import com.hms.db.DBConnection;

/**
 * Update appointment status servlet.
 *
 * cz-java-0069 (In-Memory Session Storage) remediation:
 * HttpSession is transparently backed by Amazon ElastiCache (Redis)
 * via Spring Session (SpringHttpSessionConfig + DelegatingFilterProxy in web.xml),
 * enabling container-safe session management on EKS. Sessions survive container
 * restarts and are shared across horizontally-scaled instances.
 *
 * Required environment variables:
 *   REDIS_HOST - Amazon ElastiCache Redis endpoint (default: localhost)
 *   REDIS_PORT - Amazon ElastiCache Redis port     (default: 6379)
 */
@WebServlet("/updateStatus")
public class UpdateStatus extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
		 int 	id = Integer.parseInt(req.getParameter("id"));
		 int 	doctorId = Integer.parseInt(req.getParameter("doctorId"));
		 String comment = req.getParameter("comment");
		 
		 AppointmentDAO appDAO = new AppointmentDAO(DBConnection.getConn());
		 boolean f = appDAO.updateDrAppointmentCommentStatus(id, doctorId, comment);
		 
		 // cz-java-0069: Spring Session intercepts getSession() and stores the session
		 // in Amazon ElastiCache (Redis) instead of in-memory JVM storage.
		 HttpSession session = req.getSession();
		 
		 
		 if(f == true) {
			 session.setAttribute("successMsg", "Comment updated");
			 resp.sendRedirect("doctor/patient.jsp");
			 
		 }else {
			 
			 session.setAttribute("errorMsg", "Something went wrong on server!");
			 resp.sendRedirect("doctor/patient.jsp");
			 
		 }
		 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
}
