package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.AppointmentDAO;
import com.hms.db.DBConnection;
import com.hms.entity.Appointment;

/**
 * Handles user appointment booking requests.
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
@WebServlet("/addAppointment")
public class AppointmentServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int userId = Integer.parseInt(req.getParameter("userId"));
		String fullName = req.getParameter("fullName");
		String gender = req.getParameter("gender");
		String age = req.getParameter("age");
		String appointmentDate = req.getParameter("appointmentDate");
		String email = req.getParameter("email");
		String phone = req.getParameter("phone");
		String diseases = req.getParameter("diseases");
		int doctorId = Integer.parseInt(req.getParameter("doctorNameSelect"));
		String address = req.getParameter("address");

		Appointment appointment = new Appointment(userId, fullName, gender, age, appointmentDate, email, phone, diseases, doctorId, address, "Pending");

		AppointmentDAO appointmentDAO = new AppointmentDAO(DBConnection.getConn());
		boolean f = appointmentDAO.addAppointment(appointment);

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
			session.setAttribute("successMsg", "Appointment is recorded Successfully.");
			resp.sendRedirect("user_appointment.jsp");

		} else {
			// cr-java-0065: Setting errorMsg on the Redis-backed distributed
			// session ensures the flash message is available across all
			// application instances, not just the current server node.
			session.setAttribute("errorMsg", "Something went wrong on server!");
			resp.sendRedirect("user_appointment.jsp");

		}

	}

}
