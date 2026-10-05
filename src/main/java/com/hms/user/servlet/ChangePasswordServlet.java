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

/**
 * User change password servlet.
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
@WebServlet("/userChangePassword")
public class ChangePasswordServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		int userId = Integer.parseInt(req.getParameter("userId"));
		String oldPassword = req.getParameter("oldPassword");
		String newPassword = req.getParameter("newPassword");
		
		UserDAO uDAO = new UserDAO(DBConnection.getConn());
		//boolean f = uDAO.checkOldPassword(userId, oldPassword);
		
		// cz-java-0069: Spring Session intercepts getSession() and stores the session
		// in Amazon ElastiCache (Redis) instead of in-memory JVM storage.
		HttpSession session = req.getSession();
		
		if(uDAO.checkOldPassword(userId, oldPassword)) {
			
			if(uDAO.changePassword(userId, newPassword)) {
				
				session.setAttribute("successMsg", "Password Change Successfully.");
				resp.sendRedirect("change_password.jsp");
				
			}else {
				
				session.setAttribute("errorMsg", "Something wrong on server!");
				resp.sendRedirect("change_password.jsp");
				
			}
			
		}else {
			session.setAttribute("errorMsg", "Old password incorrect");
			resp.sendRedirect("change_password.jsp");
		}
		
		
		
	}
	
	

}
