package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069 [In-Memory Session Storage]: HttpSession is transparently backed by
// Amazon ElastiCache (Redis) via Spring Session (springSessionRepositoryFilter).
// Sessions survive container restarts and are shared across all horizontal
// replicas on EKS. No in-memory session storage is used.
import javax.servlet.http.HttpSession;

import com.hms.dao.DoctorDAO;
import com.hms.db.DBConnection;

@WebServlet("/deleteDoctor")
public class DeleteDoctorServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//get id(which is coming as string value) and convert into int	
		int id = Integer.parseInt(req.getParameter("id"));
		
		DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
		// cz-java-0069 [In-Memory Session Storage]: req.getSession() returns a Redis-backed
		// session managed by Spring Session + Amazon ElastiCache (Redis) on EKS.
		// The DelegatingFilterProxy (springSessionRepositoryFilter) in web.xml
		// intercepts this call and delegates to the Redis session repository,
		// ensuring session data is externalized and container-restart resilient.
		HttpSession session = req.getSession();
		
		boolean f = docDAO.deleteDoctorById(id);
		
		if(f==true) {
			session.setAttribute("successMsg", "Doctor Deleted Successfully.");
			resp.sendRedirect("admin/view_doctor.jsp");
		}
		else {
			session.setAttribute("errorMsg", "Something went wrong on server!");
			resp.sendRedirect("admin/view_doctor.jsp");
		}
	}
	
	

}
