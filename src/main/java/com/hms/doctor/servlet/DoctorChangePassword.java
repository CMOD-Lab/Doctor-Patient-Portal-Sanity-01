package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cr-java-0065: HTTP Session State Storage — FIXED
// import javax.servlet.http.HttpSession is retained; the HttpSession API is unchanged.
// Spring Session's DelegatingFilterProxy (configured in web.xml) transparently replaces
// the container's in-memory session with a distributed Redis-backed session stored in
// Amazon ElastiCache for Redis (configured in RedisHttpSessionConfig), enabling:
//  - Stateless application instances (no server affinity / sticky sessions)
//  - Horizontal scaling across multiple instances behind a load balancer
//  - Session persistence across instance restarts and deployments
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

		// cr-java-0065: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session data is stored in Amazon ElastiCache for Redis, not in the local JVM heap.
		HttpSession session = req.getSession();

		if (doctorDAO.checkOldPassword(doctorId, oldPassword)) {

			if (doctorDAO.changePassword(doctorId, newPassword)) {
				// cr-java-0065: setAttribute persists "successMsg" to Amazon ElastiCache for Redis (distributed session store).
				session.setAttribute("successMsg", "Password change successfully.");
				resp.sendRedirect("doctor/edit_profile.jsp");

			} else {
				// cr-java-0065: setAttribute persists "errorMsg" to Amazon ElastiCache for Redis (distributed session store).
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("doctor/edit_profile.jsp");

			}

		} else {
			// cr-java-0065: setAttribute persists "errorMsg" to Amazon ElastiCache for Redis (distributed session store).
			session.setAttribute("errorMsg", "Old Password not match");
			resp.sendRedirect("doctor/edit_profile.jsp");

		}
	}

}
