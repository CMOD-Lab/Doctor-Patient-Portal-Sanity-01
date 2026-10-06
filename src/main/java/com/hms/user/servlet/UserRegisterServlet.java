package com.hms.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cr-java-0065: HttpSession is now backed by Amazon ElastiCache for Redis via
// Spring Session. The existing HttpSession API is preserved unchanged; the
// springSessionRepositoryFilter (DelegatingFilterProxy registered in web.xml)
// transparently replaces the server-local session store with a centralised,
// distributed Redis store. This eliminates server affinity and enables
// stateless, horizontally scalable application instances.
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

			// cr-java-0065: Spring Session intercepts getSession() and returns a
			// Redis-backed session stored in Amazon ElastiCache. All setAttribute /
			// getAttribute calls below are transparently delegated to the distributed
			// Redis store, enabling stateless instances with no server affinity.
			HttpSession session = req.getSession();

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// Success message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("successMsg", "Register Successfully");
				resp.sendRedirect("signup.jsp"); // which page you want to show this msg
				// System.out.println("register successfull");
				// out.println("success");

			} else {

				// Error message stored in Redis-backed distributed session (cr-java-0065)
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
