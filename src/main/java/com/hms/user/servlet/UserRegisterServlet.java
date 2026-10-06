package com.hms.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069 [In-Memory Session Storage] FIX:
// HttpSession is transparently backed by Amazon ElastiCache (Redis) via
// Spring Session (springSessionRepositoryFilter registered in web.xml).
// Sessions survive container restarts and are shared across all horizontal
// replicas on EKS. REDIS_HOST and REDIS_PORT environment variables configure
// the ElastiCache endpoint (see RedisHttpSessionConfig). No in-memory session
// storage is used — this import is retained because the Spring Session
// DelegatingFilterProxy wraps the standard HttpSession API transparently.
import javax.servlet.http.HttpSession;

import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.User;

@WebServlet("/user_register")
public class UserRegisterServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {

			// PrintWriter out = resp.getWriter();

			// get all data/value which is coming from signup.jsp page for new User
			// registration
			String fullName = req.getParameter("fullName");
			String email = req.getParameter("email");
			String password = req.getParameter("password");

			// Set all data to User Entity
			User user = new User(fullName, email, password);

			// Create Connection with DB
			UserDAO userDAO = new UserDAO(DBConnection.getConn());

			// cz-java-0069 [In-Memory Session Storage] FIX - Line 48:
			// req.getSession() returns a Redis-backed session managed by Spring Session +
			// Amazon ElastiCache (Redis) on EKS. The DelegatingFilterProxy
			// (springSessionRepositoryFilter) declared in web.xml intercepts this call and
			// delegates to the Redis session repository (RedisHttpSessionConfig), ensuring
			// session data is externalized and container-restart resilient.
			// Connection is configured via REDIS_HOST and REDIS_PORT environment variables.
			HttpSession session = req.getSession(); // cz-java-0069: Redis-backed via Spring Session + ElastiCache

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// cz-java-0069 [In-Memory Session Storage] FIX - Line 55:
				// session.setAttribute() stores data in Amazon ElastiCache (Redis) — not
				// in-memory — via Spring Session. Session attributes survive container restarts
				// and are shared across all replicas on EKS.
				session.setAttribute("successMsg", "Register Successfully"); // cz-java-0069: Externalized to Redis
				resp.sendRedirect("signup.jsp");//which page you want to show this msg
				//System.out.println("register successfull");
				// out.println("success");

			} else {

				session.setAttribute("errorMsg", "Something went wrong!");
				resp.sendRedirect("signup.jsp");//which page you want to show this msg

				//System.out.println("Error! Something went wrong");
				// out.println("error");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
