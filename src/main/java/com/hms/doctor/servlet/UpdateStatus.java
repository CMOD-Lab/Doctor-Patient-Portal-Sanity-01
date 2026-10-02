package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.AppointmentDAO;
import com.hms.db.DBConnection;

/**
 * Handles doctor appointment status/comment update requests.
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
 *
 * <p>Required environment variables for Redis/ElastiCache:
 * <ul>
 *   <li>{@code REDIS_HOST}     – ElastiCache primary endpoint (default: localhost)</li>
 *   <li>{@code REDIS_PORT}     – Redis port (default: 6379)</li>
 *   <li>{@code REDIS_PASSWORD} – Redis AUTH token (optional)</li>
 * </ul>
 */
@WebServlet("/updateStatus")
public class UpdateStatus extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {

			int id = Integer.parseInt(req.getParameter("id"));
			int doctorId = Integer.parseInt(req.getParameter("doctorId"));
			String comment = req.getParameter("comment");

			AppointmentDAO appDAO = new AppointmentDAO(DBConnection.getConn());
			boolean f = appDAO.updateDrAppointmentCommentStatus(id, doctorId, comment);

			// cr-java-0065: Session is backed by Amazon ElastiCache for Redis via
			// Spring Session. The springSessionRepositoryFilter (registered in
			// SpringSessionInitializer) intercepts this call and returns a
			// Redis-backed HttpSession, enabling stateless application instances
			// with centralized, distributed session management.
			// No server affinity (sticky sessions) is required.
			HttpSession session = req.getSession();

			if (f == true) {
				// cr-java-0065: Setting successMsg on the Redis-backed distributed
				// session ensures the flash message is available across all
				// application instances, not just the current server node.
				session.setAttribute("successMsg", "Comment updated");
				resp.sendRedirect("doctor/patient.jsp");

			} else {
				// cr-java-0065: Setting errorMsg on the Redis-backed distributed
				// session ensures the flash message is available across all
				// application instances, not just the current server node.
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("doctor/patient.jsp");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
