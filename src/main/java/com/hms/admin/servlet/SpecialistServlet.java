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
 * Add specialist servlet.
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
@WebServlet("/addSpecialist")
public class SpecialistServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String specialistName = req.getParameter("specialistName");
		
		SpecialistDAO specialistDAO = new SpecialistDAO(DBConnection.getConn());
		boolean f = specialistDAO.addSpecialist(specialistName);
		
		// cz-java-0069: Spring Session intercepts getSession() and stores the session
		// in Amazon ElastiCache (Redis) instead of in-memory JVM storage.
		HttpSession session = req.getSession();
		
		if (f==true) {
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");
			
		} else {
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}
	
	

}
