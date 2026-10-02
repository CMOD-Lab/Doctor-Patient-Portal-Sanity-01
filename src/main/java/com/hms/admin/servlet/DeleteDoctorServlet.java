package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.DoctorDAO;
import com.hms.db.DBConnection;

/**
 * cr-java-0065: HTTP Session State Storage — FIXED
 *
 * Session state is now stored in Amazon ElastiCache for Redis via Spring Session
 * (configured in RedisHttpSessionConfig). The HttpSession API is unchanged;
 * Spring Session's DelegatingFilterProxy transparently replaces the container's
 * in-memory session with a distributed Redis-backed session, enabling:
 *  - Stateless application instances (no server affinity)
 *  - Horizontal scaling across multiple instances
 *  - Session persistence across instance restarts
 *
 * Line 25: req.getSession() — returns a Redis-backed HttpSession (via Spring Session)
 * Line 30: session.setAttribute("successMsg", ...) — stored in ElastiCache Redis
 * Line 30: session.setAttribute("errorMsg", ...) — stored in ElastiCache Redis
 */
@WebServlet("/deleteDoctor")
public class DeleteDoctorServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//get id(which is coming as string value) and convert into int	
		int id = Integer.parseInt(req.getParameter("id"));
		
		DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
		// cr-java-0065: Session is now Redis-backed via Spring Session + ElastiCache.
		// req.getSession() returns a distributed session stored in Amazon ElastiCache for Redis,
		// not in the local JVM heap — enabling stateless, horizontally scalable instances.
		HttpSession session = req.getSession();
		
		boolean f = docDAO.deleteDoctorById(id);
		
		if(f==true) {
			// cr-java-0065: setAttribute persists "successMsg" to Amazon ElastiCache for Redis (distributed session store).
			session.setAttribute("successMsg", "Doctor Deleted Successfully.");
			resp.sendRedirect("admin/view_doctor.jsp");
		}
		else {
			// cr-java-0065: setAttribute persists "errorMsg" to Amazon ElastiCache for Redis (distributed session store).
			session.setAttribute("errorMsg", "Something went wrong on server!");
			resp.sendRedirect("admin/view_doctor.jsp");
		}
	}
	
	

}
