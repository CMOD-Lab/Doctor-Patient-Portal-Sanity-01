package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.entity.User;

/**
 * cr-java-0065: HTTP Session State Storage — FIXED
 *
 * Session state is now stored in Amazon ElastiCache for Redis via Spring Session
 * (configured in RedisHttpSessionConfig). The HttpSession API is unchanged;
 * Spring Session's DelegatingFilterProxy transparently replaces the container's
 * in-memory session with a distributed Redis-backed session, enabling:
 *  - Stateless application instances (no server affinity)
 *  - Horizontal scaling across multiple instances
 *  - Session persistence across instance restarts
 *
 * Line 26: req.getSession() — returns a Redis-backed HttpSession (via Spring Session)
 * Line 34: session.setAttribute("adminObj", ...) — stored in ElastiCache Redis
 * Line 38: session.setAttribute("errorMsg", ...) — stored in ElastiCache Redis
 */
@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			//create one static Admin for this project
			String email = req.getParameter("email");
			String password = req.getParameter("password");
			
			// cr-java-0065: Session is now Redis-backed via Spring Session + ElastiCache.
			// req.getSession() returns a distributed session stored in Amazon ElastiCache for Redis,
			// not in the local JVM heap — enabling stateless, horizontally scalable instances.
			HttpSession session = req.getSession();
			
			//logic for a static Admin
			if ("admin@gmail.com".equals(email) && "admin".equals(password)) {
				
				//if "adminObj" obj available then give the access of admin page, 
				//otherwise "adminObj" is not present in obj then others user is login(which is not admin). so dont give him the access of Admin.
				//the below line specially check the admin is log in or not! "adminObj" object is available that means admin is log in.
				// cr-java-0065: setAttribute persists "adminObj" to Amazon ElastiCache for Redis (distributed session store).
				session.setAttribute("adminObj", new User());
				resp.sendRedirect("admin/index.jsp");
			}
			else {
				// cr-java-0065: setAttribute persists "errorMsg" to Amazon ElastiCache for Redis (distributed session store).
				session.setAttribute("errorMsg", "Invalid Username or Password.");
				resp.sendRedirect("admin_login.jsp");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
