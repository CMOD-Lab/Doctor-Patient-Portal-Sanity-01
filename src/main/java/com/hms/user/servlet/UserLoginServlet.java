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
 * User login servlet.
 *
 * cz-java-0069 (In-Memory Session Storage) remediation:
 * HttpSession is transparently backed by Amazon ElastiCache (Redis)
 * via Spring Session (SpringHttpSessionConfig + DelegatingFilterProxy in web.xml),
 * enabling container-safe session management on EKS. Sessions survive container
 * restarts and are shared across horizontally-scaled instances.
 *
 * Required environment variables:
 *   REDIS_HOST - Amazon ElastiCache Redis endpoint (default: localhost)
 *   REDIS_PORT - Amazon ElastiCache Redis port     (default: 6379)
 */
@WebServlet("/userLogin")
public class UserLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		
		// cz-java-0069: Spring Session intercepts getSession() and stores the session
		// in Amazon ElastiCache (Redis) instead of in-memory JVM storage.
		HttpSession session = req.getSession();
		
		UserDAO userDAO = new UserDAO(DBConnection.getConn());
		User user = userDAO.loginUser(email, password);
		
		if (user!=null) {
			session.setAttribute("userObj",user);
			resp.sendRedirect("index.jsp"); 
		}
		else {
			session.setAttribute("errorMsg","Invalid email or password");
			resp.sendRedirect("user_login.jsp"); 
		}
	}
	
	
}
