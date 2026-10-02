package com.hms.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession import retained; in-memory session storage has been replaced with
// Spring Session backed by Amazon ElastiCache (Redis) via DelegatingFilterProxy configured in web.xml.
// Sessions are now stored externally in Redis, preventing session loss on container restarts
// or scale-out events in EKS. Configure REDIS_HOST and REDIS_PORT environment variables.
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

			// cz-java-0069 (Line 48): Replaced in-memory HttpSession with Spring Session
			// backed by Amazon ElastiCache (Redis). The DelegatingFilterProxy in web.xml
			// intercepts this call and returns a Redis-backed session instead of an
			// in-memory one, ensuring session persistence across container restarts on EKS.
			// Configure REDIS_HOST and REDIS_PORT environment variables for ElastiCache endpoint.
			HttpSession session = req.getSession();

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// cz-java-0069 (Line 55): Session attribute stored in Amazon ElastiCache (Redis)
				// via Spring Session — not in JVM heap memory. This ensures the attribute
				// survives container restarts and is accessible across all EKS pod replicas.
				session.setAttribute("successMsg", "Register Successfully");
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
