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

@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// cz-java-0069: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session state is stored in Amazon ElastiCache (Redis), enabling horizontal scaling on EKS.
		HttpSession session = req.getSession();
		session.removeAttribute("userObj");
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");
		
	}

	
	
}
