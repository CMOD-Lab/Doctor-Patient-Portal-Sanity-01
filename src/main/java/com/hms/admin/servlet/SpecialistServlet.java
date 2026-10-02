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
 * Handles add-specialist requests from the admin panel.
 *
 * <p><strong>Cloud-Native Session Management (cr-java-0065):</strong>
 * Session state is stored in Amazon ElastiCache for Redis via Spring Session
 * ({@link com.hms.config.RedisSessionConfig}).  The {@code springSessionRepositoryFilter}
 * registered by {@link com.hms.config.SpringSessionInitializer} transparently
 * replaces the container-managed {@link HttpSession} with a Redis-backed
 * session, so calls to {@code req.getSession()} below operate against the
 * distributed Redis store rather than server-local memory.  This eliminates
 * server affinity and allows the application to scale horizontally across
 * multiple instances without session data loss.
 */
@WebServlet("/addSpecialist")
public class SpecialistServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String specialistName = req.getParameter("specialistName");

		SpecialistDAO specialistDAO = new SpecialistDAO(DBConnection.getConn());
		boolean f = specialistDAO.addSpecialist(specialistName);

		// Session is backed by Amazon ElastiCache for Redis via Spring Session.
		// The springSessionRepositoryFilter (registered in SpringSessionInitializer)
		// intercepts this call and returns a Redis-backed HttpSession, enabling
		// stateless application instances with centralized, distributed session
		// management. No server affinity (sticky sessions) is required.
		HttpSession session = req.getSession();

		if (f == true) {
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");

		} else {
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}

}
