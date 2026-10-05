package com.hms.doctor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.DoctorDAO;
import com.hms.db.DBConnection;
import com.hms.entity.Doctor;

/**
 * Doctor login servlet.
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
@WebServlet("/doctorLogin")
public class DoctorLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		//get email and password which is coming from doctor_login.jsp page
		String email = req.getParameter("email");
		String password = req.getParameter("password");

		// cz-java-0069: Spring Session intercepts getSession() and stores the session
		// in Amazon ElastiCache (Redis) instead of in-memory JVM storage.
		HttpSession session = req.getSession();

		//create DB connection
		DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
		
		//call loginDoctor() method for doctor login which method declared in DoctorDAO 
		Doctor doctor = docDAO.loginDoctor(email, password);

		if (doctor != null) {
			//means doctor is valid or exist
			//then store particular logged in doctor object in session
			session.setAttribute("doctorObj", doctor);
			//and redirect the particular doctor index page which is reside doctor folder
			resp.sendRedirect("doctor/index.jsp");//doctor index means dashboard of doctors
		} else {
			session.setAttribute("errorMsg", "Invalid email or password");
			resp.sendRedirect("doctor_login.jsp");
		}

	}

}
