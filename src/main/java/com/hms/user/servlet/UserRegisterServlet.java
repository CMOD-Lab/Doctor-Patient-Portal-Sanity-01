package com.hms.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

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

			// cr-java-0065: req.getSession() returns a Redis-backed HttpSession via Spring Session.
			// setAttribute operations below store session data in Amazon ElastiCache for Redis,
			// ensuring session state is consistent across all horizontally-scaled instances.
			HttpSession session = req.getSession();

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// cr-java-0065: Success message stored in distributed Redis-backed session
				// via Amazon ElastiCache, accessible across all application instances.
				session.setAttribute("successMsg", "Register Successfully");
				resp.sendRedirect("signup.jsp"); // which page you want to show this msg
				// System.out.println("register successfull");
				// out.println("success");

			} else {

				// cr-java-0065: Error message stored in distributed Redis-backed session
				// via Amazon ElastiCache, accessible across all application instances.
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
