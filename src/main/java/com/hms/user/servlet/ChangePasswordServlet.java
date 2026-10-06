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

@WebServlet("/userChangePassword")
public class ChangePasswordServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		int userId = Integer.parseInt(req.getParameter("userId"));
		String oldPassword = req.getParameter("oldPassword");
		String newPassword = req.getParameter("newPassword");
		
		UserDAO uDAO = new UserDAO(DBConnection.getConn());
		//boolean f = uDAO.checkOldPassword(userId, oldPassword);
		
		// Spring Session intercepts getSession() and returns a Redis-backed session
		HttpSession session = req.getSession();
		
		if(uDAO.checkOldPassword(userId, oldPassword)) {
			
			if(uDAO.changePassword(userId, newPassword)) {
				
				// Success message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("successMsg", "Password Change Successfully.");
				resp.sendRedirect("change_password.jsp");
				
			}else {
				
				// Error message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("errorMsg", "Something wrong on server!");
				resp.sendRedirect("change_password.jsp");
				
			}
			
		}else {
			// Error message stored in Redis-backed distributed session (cr-java-0065)
			session.setAttribute("errorMsg", "Old password incorrect");
			resp.sendRedirect("change_password.jsp");
		}
		
		
		
	}
	
	

}
