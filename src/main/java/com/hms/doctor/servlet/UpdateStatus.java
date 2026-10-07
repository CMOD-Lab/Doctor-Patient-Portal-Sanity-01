package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is now backed by Spring Session + Amazon ElastiCache (Redis)
// The springSessionRepositoryFilter (registered via SpringSessionInitializer) transparently
// intercepts req.getSession() calls and stores/retrieves session data from Redis, enabling
// stateless container deployments on EKS with horizontal scaling support.
// Session data survives container restarts and is shared across all scaled instances.
import javax.servlet.http.HttpSession;

import com.hms.dao.AppointmentDAO;
import com.hms.db.DBConnection;

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
		 
		 // cz-java-0069 (Line 34): Session retrieved via Spring Session Redis - stored in ElastiCache, not in-memory
		 HttpSession session = req.getSession();
		 
		 
		 if(f == true) {
			 // cz-java-0069 (Line 39): Session attribute set in Redis-backed session store via Spring Session
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
