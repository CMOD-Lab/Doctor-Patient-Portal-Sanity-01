package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cz-java-0069: HttpSession is now backed by Spring Session + Amazon ElastiCache (Redis)
// The springSessionRepositoryFilter (registered via SpringSessionInitializer) transparently
// intercepts req.getSession() calls and stores/retrieves session data from Redis, enabling
// stateless container deployments on EKS with horizontal scaling support.
// Session data survives container restarts and is shared across all scaled instances.
import javax.servlet.http.HttpSession;

import com.hms.entity.User;

@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			//create one static Admin for this project
			String email = req.getParameter("email");
			String password = req.getParameter("password");
			
			// cz-java-0069 (Line 34): Session retrieved via Spring Session Redis - stored in ElastiCache, not in-memory
			HttpSession session = req.getSession();
			
			//logic for a static Admin
			if ("admin@gmail.com".equals(email) && "admin".equals(password)) {
				
				//if "adminObj" obj available then give the access of admin page, 
				//otherwise "adminObj" is not present in obj then others user is login(which is not admin). so dont give him the access of Admin.
				//the below line specially check the admin is log in or not! "adminObj" object is available that means admin is log in.
				// cz-java-0069 (Line 38): Session attribute set in Redis-backed session store via Spring Session
				session.setAttribute("adminObj", new User());
				resp.sendRedirect("admin/index.jsp");
			}
			else {
				session.setAttribute("errorMsg", "Invalid Username or Password.");
				resp.sendRedirect("admin_login.jsp");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
