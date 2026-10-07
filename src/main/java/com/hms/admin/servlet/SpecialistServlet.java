package com.hms.admin.servlet;

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

import com.hms.dao.SpecialistDAO;
import com.hms.db.DBConnection;

@WebServlet("/addSpecialist")
public class SpecialistServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String specialistName = req.getParameter("specialistName");
		
		SpecialistDAO specialistDAO = new SpecialistDAO(DBConnection.getConn());
		boolean f = specialistDAO.addSpecialist(specialistName);
		
		// cz-java-0069 (Line 29): Session retrieved via Spring Session Redis - stored in ElastiCache, not in-memory
		HttpSession session = req.getSession();
		
		if (f==true) {
			// cz-java-0069 (Line 33): Session attribute set in Redis-backed session store via Spring Session
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");
			
		} else {
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}
	
	

}
