package com.hms.doctor.servlet;

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

import com.hms.dao.DoctorDAO;
import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.Doctor;


@WebServlet("/doctorLogin")
public class DoctorLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		//get email and password which is coming from doctor_login.jsp page
		String email = req.getParameter("email");
		String password = req.getParameter("password");

		// cz-java-0069: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session state is stored in Amazon ElastiCache (Redis), enabling horizontal scaling on EKS.
		//create session
		HttpSession session = req.getSession();

		//create DB connection
		DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
		
		//call loginDoctor() method for doctor login which method declared in DoctorDAO 
		Doctor doctor = docDAO.loginDoctor(email, password);

		if (doctor != null) {
			//means doctor is valid or exist
			//then store particular logged in doctor object in session
			// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
			session.setAttribute("doctorObj", doctor);
			//and redirect the particular doctor index page which is reside doctor folder
			resp.sendRedirect("doctor/index.jsp");//doctor index means dashboard of doctors
		} else {
			// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
			session.setAttribute("errorMsg", "Invalid email or password");
			resp.sendRedirect("doctor_login.jsp");
		}

	}

}
