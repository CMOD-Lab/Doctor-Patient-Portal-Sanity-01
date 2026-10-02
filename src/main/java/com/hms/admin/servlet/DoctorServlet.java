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
import com.hms.entity.Doctor;

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
 * Line 10: import javax.servlet.http.HttpSession — used with Redis-backed session
 * Line 40: req.getSession() — returns a Redis-backed HttpSession (via Spring Session)
 * Line 43: session.setAttribute("successMsg", ...) — stored in ElastiCache Redis
 * Line 48: session.setAttribute("errorMsg", ...) — stored in ElastiCache Redis
 */
@WebServlet("/addDoctor")
public class DoctorServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			//get all data which is coming from doctor.jsp doctor details
			String fullName = req.getParameter("fullName");
			String dateOfBirth = req.getParameter("dateOfBirth");
			String qualification = req.getParameter("qualification");
			String specialist = req.getParameter("specialist");
			String email = req.getParameter("email");
			String phone = req.getParameter("phone");
			String password = req.getParameter("password");
			
			
			Doctor doctor = new Doctor(fullName, dateOfBirth, qualification, specialist, email, phone, password);
			
			DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
			
			boolean f = docDAO.registerDoctor(doctor);

			// cr-java-0065: Session is now Redis-backed via Spring Session + ElastiCache.
			// req.getSession() returns a distributed session stored in Amazon ElastiCache for Redis,
			// not in the local JVM heap — enabling stateless, horizontally scalable instances.
			HttpSession session = req.getSession();
			
			if(f==true) {
				// cr-java-0065: setAttribute persists "successMsg" to Amazon ElastiCache for Redis (distributed session store).
				session.setAttribute("successMsg", "Doctor added Successfully");
				resp.sendRedirect("admin/doctor.jsp");
				
			}
			else {
				// cr-java-0065: setAttribute persists "errorMsg" to Amazon ElastiCache for Redis (distributed session store).
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("admin/doctor.jsp");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
}
