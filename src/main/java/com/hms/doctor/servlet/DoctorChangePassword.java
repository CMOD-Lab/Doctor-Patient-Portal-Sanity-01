package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is transparently backed by Amazon ElastiCache (Redis)
// via Spring Session (RedisSessionConfig + SpringSessionInitializer).
// Spring Session's filter intercepts req.getSession() and returns a Redis-backed
// session instead of the in-memory container session, enabling stateless horizontal
// scaling on EKS. No servlet-layer code change required.
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

		// cz-java-0069: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session state is stored in Amazon ElastiCache (Redis), enabling horizontal scaling on EKS.
		HttpSession session = req.getSession();

		if (doctorDAO.checkOldPassword(doctorId, oldPassword)) {

			if (doctorDAO.changePassword(doctorId, newPassword)) {
				
				// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
				session.setAttribute("successMsg", "Password change successfully.");
				resp.sendRedirect("doctor/edit_profile.jsp");

			} else {
				
				// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("doctor/edit_profile.jsp");

			}

		} else {
			// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
			session.setAttribute("errorMsg", "Old Password not match");
			resp.sendRedirect("doctor/edit_profile.jsp");

		}
	}

}
