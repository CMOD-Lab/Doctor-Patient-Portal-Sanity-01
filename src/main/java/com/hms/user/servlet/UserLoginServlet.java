package com.hms.user.servlet;

import java.io.IOException;

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

@WebServlet("/userLogin")
public class UserLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		
		// cr-java-0065: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session data (userObj, errorMsg) is stored in Amazon ElastiCache for Redis,
		// ensuring session state is consistent across all horizontally-scaled instances.
		HttpSession session = req.getSession();
		
		UserDAO userDAO = new UserDAO(DBConnection.getConn());
		User user = userDAO.loginUser(email, password);
		
		if (user != null) {
			// cr-java-0065: setAttribute stores the user object in the distributed Redis session,
			// not in local server memory — safe for multi-instance deployments.
			session.setAttribute("userObj", user);
			resp.sendRedirect("index.jsp"); 
		} else {
			// cr-java-0065: Error message stored in Redis-backed session for cross-instance access.
			session.setAttribute("errorMsg", "Invalid email or password");
			resp.sendRedirect("user_login.jsp"); 
		}
	}
	
	
}
