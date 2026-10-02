package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.SpecialistDAO;
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
 * Line 10: import javax.servlet.http.HttpSession — used with Redis-backed session
 * Line 26: req.getSession() — returns a Redis-backed HttpSession (via Spring Session)
 * Line 29: session.setAttribute("successMsg", ...) — stored in ElastiCache Redis
 * Line 33: session.setAttribute("errorMsg", ...) — stored in ElastiCache Redis
 */
@WebServlet("/addSpecialist")
public class SpecialistServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String specialistName = req.getParameter("specialistName");
		
		SpecialistDAO specialistDAO = new SpecialistDAO(DBConnection.getConn());
		boolean f = specialistDAO.addSpecialist(specialistName);
		
		// cr-java-0065: Session is now Redis-backed via Spring Session + ElastiCache.
		// req.getSession() returns a distributed session stored in Amazon ElastiCache for Redis,
		// not in the local JVM heap — enabling stateless, horizontally scalable instances.
		HttpSession session = req.getSession();
		
		if (f==true) {
			// cr-java-0065: setAttribute persists "successMsg" to Amazon ElastiCache for Redis (distributed session store).
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");
			
		} else {
			// cr-java-0065: setAttribute persists "errorMsg" to Amazon ElastiCache for Redis (distributed session store).
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}
	
	

}
