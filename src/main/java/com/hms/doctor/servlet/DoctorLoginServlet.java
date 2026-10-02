package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.DoctorDAO;
import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.Doctor;

/**
 * Handles doctor login requests.
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
@WebServlet("/doctorLogin")
public class DoctorLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		//get email and password which is coming from doctor_login.jsp page
		String email = req.getParameter("email");
		String password = req.getParameter("password");

		// cr-java-0065: Session is backed by Amazon ElastiCache for Redis via
		// Spring Session. The springSessionRepositoryFilter (registered in
		// SpringSessionInitializer) intercepts this call and returns a
		// Redis-backed HttpSession, enabling stateless application instances
		// with centralized, distributed session management.
		// No server affinity (sticky sessions) is required.
		HttpSession session = req.getSession();

		//create DB connection
		DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
		
		//call loginDoctor() method for doctor login which method declared in DoctorDAO 
		Doctor doctor = docDAO.loginDoctor(email, password);

		if (doctor != null) {
			//means doctor is valid or exist
			//then store particular logged in doctor object in session
			// cr-java-0065: doctorObj is stored in the Redis-backed distributed
			// session — not in server-local memory — so it survives instance
			// restarts and load-balancer failovers.
			session.setAttribute("doctorObj", doctor);
			//and redirect the particular doctor index page which is reside doctor folder
			resp.sendRedirect("doctor/index.jsp");//doctor index means dashboard of doctors
		} else {
			// cr-java-0065: Error message stored in Redis-backed session.
			session.setAttribute("errorMsg", "Invalid email or password");
			resp.sendRedirect("doctor_login.jsp");
		}

	}

}
