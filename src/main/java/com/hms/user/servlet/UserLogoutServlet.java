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

@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// cz-java-0069: Session obtained here is transparently backed by Amazon ElastiCache (Redis)
		// via Spring Session — no in-memory state is used. Session survives container restarts.
		HttpSession session = req.getSession();
		session.removeAttribute("userObj");
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");

	}

}
