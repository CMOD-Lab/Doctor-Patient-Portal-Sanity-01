package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is now backed by Spring Session + Amazon ElastiCache (Redis) via DelegatingFilterProxy.
// The in-memory session storage is replaced with a distributed Redis-backed session, preventing session loss
// on container restarts or scale-out events in EKS. Configure REDIS_HOST and REDIS_PORT env vars.
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
		
		// cz-java-0069: Session obtained here is transparently backed by Amazon ElastiCache (Redis)
		// via Spring Session — no in-memory state is used. Session survives container restarts.
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
