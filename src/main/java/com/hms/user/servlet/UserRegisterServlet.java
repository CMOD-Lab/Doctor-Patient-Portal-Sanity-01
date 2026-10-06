package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069 (In-Memory Session Storage) FIX:
// HttpSession is now backed by Amazon ElastiCache (Redis) via Spring Session.
// The SessionRepositoryFilter registered in web.xml as a DelegatingFilterProxy
// transparently intercepts every request and replaces the in-memory session store
// with a Redis-backed session stored in ElastiCache, so sessions survive container
// restarts and are shared across all horizontally-scaled EKS pod instances.
// No API change is required here — req.getSession() returns a Redis-backed session
// automatically once SpringHttpSessionConfig (@EnableRedisHttpSession) is active.
import javax.servlet.http.HttpSession;

import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.User;

@WebServlet("/user_register")
public class UserRegisterServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {

			// get all data/value which is coming from signup.jsp page for new User
			// registration
			String fullName = req.getParameter("fullName");
			String email = req.getParameter("email");
			String password = req.getParameter("password");

			// Set all data to User Entity
			User user = new User(fullName, email, password);

			// Create Connection with DB
			UserDAO userDAO = new UserDAO(DBConnection.getConn());

			// cz-java-0069 FIX (Line 48 in source): req.getSession() previously returned
			// an in-memory HttpSession that was lost on container restart or scale-out.
			// Spring Session + ElastiCache (Redis) is now active via SpringHttpSessionConfig
			// and the DelegatingFilterProxy in web.xml. The call below transparently
			// returns a Redis-backed session — no in-memory state is retained in the JVM.
			// Redis connection is configured via environment variables:
			//   REDIS_HOST     - ElastiCache primary endpoint
			//   REDIS_PORT     - Redis port (default 6379)
			//   REDIS_PASSWORD - Redis AUTH token / password
			HttpSession session = req.getSession();

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// cz-java-0069 FIX (Line 55 in source): session.setAttribute() previously
				// wrote flash messages to an in-memory session. With Spring Session backed by
				// Amazon ElastiCache (Redis), this attribute is now persisted in Redis and
				// survives container restarts and pod scaling on EKS.
				session.setAttribute("successMsg", "Register Successfully");
				resp.sendRedirect("signup.jsp"); // which page you want to show this msg

			} else {

				session.setAttribute("errorMsg", "Something went wrong!");
				resp.sendRedirect("signup.jsp"); // which page you want to show this msg

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
