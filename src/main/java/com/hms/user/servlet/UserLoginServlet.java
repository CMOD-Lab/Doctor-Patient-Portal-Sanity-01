package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// HttpSession is now backed by Amazon ElastiCache for Redis via Spring Session
// (cr-java-0065). The existing HttpSession API is preserved; Spring Session's
// springSessionRepositoryFilter (registered in web.xml) transparently replaces
// the server-local session store with a distributed Redis store, enabling
// stateless, horizontally scalable instances without server affinity.
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
		
		// Spring Session intercepts getSession() and returns a Redis-backed session
		HttpSession session = req.getSession();
		
		UserDAO userDAO = new UserDAO(DBConnection.getConn());
		User user = userDAO.loginUser(email, password);
		
		if (user!=null) {
			// User object stored in Redis-backed distributed session (cr-java-0065)
			session.setAttribute("userObj",user);
			resp.sendRedirect("index.jsp"); 
		}
		else {
			// Error message stored in Redis-backed distributed session (cr-java-0065)
			session.setAttribute("errorMsg","Invalid email or password");
			resp.sendRedirect("user_login.jsp"); 
		}
	}
	
	
}
