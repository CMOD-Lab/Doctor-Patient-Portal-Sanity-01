package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.User;

/**
 * Handles user login requests.
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
@WebServlet("/userLogin")
public class UserLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String email    = req.getParameter("email");
		String password = req.getParameter("password");

		// cr-java-0065: Session is backed by Amazon ElastiCache for Redis via
		// Spring Session. The springSessionRepositoryFilter (registered in
		// SpringSessionInitializer) intercepts this call and returns a
		// Redis-backed HttpSession, enabling stateless application instances
		// with centralized, distributed session management.
		// No server affinity (sticky sessions) is required.
		HttpSession session = req.getSession();

		UserDAO userDAO = new UserDAO(DBConnection.getConn());
		User user = userDAO.loginUser(email, password);

		if (user != null) {
			// cr-java-0065: Storing the authenticated user object in the
			// Redis-backed distributed session ensures the user's login state
			// is available on any application instance that handles subsequent
			// requests, enabling true horizontal scalability.
			session.setAttribute("userObj", user);
			resp.sendRedirect("index.jsp");
		} else {
			// cr-java-0065: Setting errorMsg on the Redis-backed distributed
			// session ensures the error message is available on any instance
			// that handles the redirected request.
			session.setAttribute("errorMsg", "Invalid email or password");
			resp.sendRedirect("user_login.jsp");
		}
	}

}
