package com.hms.admin.servlet;

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

import com.hms.dao.SpecialistDAO;
import com.hms.db.DBConnection;

@WebServlet("/addSpecialist")
public class SpecialistServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String specialistName = req.getParameter("specialistName");
		
		SpecialistDAO specialistDAO = new SpecialistDAO(DBConnection.getConn());
		boolean f = specialistDAO.addSpecialist(specialistName);
		
		// Spring Session intercepts getSession() and returns a Redis-backed session
		HttpSession session = req.getSession();
		
		if (f==true) {
			// Success message stored in Redis-backed distributed session (cr-java-0065)
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");
			
		} else {
			// Error message stored in Redis-backed distributed session (cr-java-0065)
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}
	
	

}
