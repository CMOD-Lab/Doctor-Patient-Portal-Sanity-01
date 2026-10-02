package com.hms.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069 [FIX]: HttpSession is transparently backed by Amazon ElastiCache (Redis)
// via Spring Session (springSessionRepositoryFilter registered in web.xml + RedisSessionConfig).
// Sessions survive container restarts and scale horizontally on EKS with IRSA for secure access.
// REDIS_HOST and REDIS_PORT are injected as environment variables (see RedisSessionConfig.java).
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

			// cz-java-0069 [FIX]: req.getSession() returns a Spring Session proxy backed by
			// Amazon ElastiCache (Redis) — NOT the in-memory Tomcat HttpSession.
			// The springSessionRepositoryFilter (DelegatingFilterProxy) in web.xml intercepts
			// every request and wraps the HttpSession with a Redis-backed implementation.
			// REDIS_HOST and REDIS_PORT are read from environment variables in RedisSessionConfig.
			HttpSession session = req.getSession();

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// cz-java-0069 [FIX] Line 48: session.setAttribute() persisted to Amazon ElastiCache (Redis)
				// via Spring Session — not stored in-memory; safe for container restarts and horizontal scaling.
				session.setAttribute("successMsg", "Register Successfully");
				resp.sendRedirect("signup.jsp"); // which page you want to show this msg
				// System.out.println("register successfull");
				// out.println("success");

			} else {

				// cz-java-0069 [FIX] Line 55: session.setAttribute() persisted to Amazon ElastiCache (Redis)
				// via Spring Session — not stored in-memory; safe for container restarts and horizontal scaling.
				session.setAttribute("errorMsg", "Something went wrong!");
				resp.sendRedirect("signup.jsp"); // which page you want to show this msg

				// System.out.println("Error! Something went wrong");
				// out.println("error");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
