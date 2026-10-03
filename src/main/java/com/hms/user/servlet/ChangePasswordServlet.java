package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is transparently backed by Amazon ElastiCache (Redis)
// via Spring Session (RedisSessionConfig + SpringSessionInitializer).
// Spring Session's filter intercepts req.getSession() and returns a Redis-backed
// session instead of the in-memory container session, enabling stateless horizontal
// scaling on EKS. No servlet-layer code change required.
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
		
		// cz-java-0069: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session state is stored in Amazon ElastiCache (Redis), enabling horizontal scaling on EKS.
		HttpSession session = req.getSession();
		
		if(uDAO.checkOldPassword(userId, oldPassword)) {
			
			if(uDAO.changePassword(userId, newPassword)) {
				
				// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
				session.setAttribute("successMsg", "Password Change Successfully.");
				resp.sendRedirect("change_password.jsp");
				
			}else {
				
				// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
				session.setAttribute("errorMsg", "Something wrong on server!");
				resp.sendRedirect("change_password.jsp");
				
			}
			
		}else {
			// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
			session.setAttribute("errorMsg", "Old password incorrect");
			resp.sendRedirect("change_password.jsp");
		}
		
		
		
	}
	
	

}
