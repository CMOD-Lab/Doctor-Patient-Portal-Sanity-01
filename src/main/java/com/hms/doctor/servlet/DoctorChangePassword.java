package com.hms.doctor.servlet;

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

import com.hms.dao.DoctorDAO;
import com.hms.db.DBConnection;

@WebServlet("/doctorChangePassword")
public class DoctorChangePassword extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int doctorId = Integer.parseInt(req.getParameter("doctorId"));
		String newPassword = req.getParameter("newPassword");
		String oldPassword = req.getParameter("oldPassword");

		DoctorDAO doctorDAO = new DoctorDAO(DBConnection.getConn());

		// Spring Session intercepts getSession() and returns a Redis-backed session
		HttpSession session = req.getSession();

		if (doctorDAO.checkOldPassword(doctorId, oldPassword)) {

			if (doctorDAO.changePassword(doctorId, newPassword)) {
				
				// Success message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("successMsg", "Password change successfully.");
				resp.sendRedirect("doctor/edit_profile.jsp");

			} else {
				
				// Error message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("doctor/edit_profile.jsp");

			}

		} else {
			// Error message stored in Redis-backed distributed session (cr-java-0065)
			session.setAttribute("errorMsg", "Old Password not match");
			resp.sendRedirect("doctor/edit_profile.jsp");

		}
	}

}
