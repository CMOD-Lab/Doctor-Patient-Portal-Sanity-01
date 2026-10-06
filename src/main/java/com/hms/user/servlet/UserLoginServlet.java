package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is now backed by Amazon ElastiCache (Redis) via Spring Session.
// The SessionRepositoryFilter (web.xml DelegatingFilterProxy) transparently replaces
// the in-memory session store with Redis, enabling stateless containers on EKS.
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
		
		// cz-java-0069: Session is Redis-backed via Spring Session + ElastiCache.
		// req.getSession() returns a Redis-backed session transparently via
		// SessionRepositoryFilter; sessions survive container restarts on EKS.
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
