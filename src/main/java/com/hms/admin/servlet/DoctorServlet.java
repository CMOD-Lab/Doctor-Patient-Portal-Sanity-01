package com.hms.admin.servlet;

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
import com.hms.db.DBConnection;
import com.hms.entity.Doctor;

@WebServlet("/addDoctor")
public class DoctorServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			//get all data which is coming from doctor.jsp doctor details
			String fullName = req.getParameter("fullName");
			String dateOfBirth = req.getParameter("dateOfBirth");
			String qualification = req.getParameter("qualification");
			String specialist = req.getParameter("specialist");
			String email = req.getParameter("email");
			String phone = req.getParameter("phone");
			String password = req.getParameter("password");
			
			
			Doctor doctor = new Doctor(fullName, dateOfBirth, qualification, specialist, email, phone, password);
			
			DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
			
			boolean f = docDAO.registerDoctor(doctor);

			// cz-java-0069: req.getSession() returns a Redis-backed HttpSession via Spring Session.
			// Session state is stored in Amazon ElastiCache (Redis), enabling horizontal scaling on EKS.
			HttpSession session = req.getSession();
			
			if(f==true) {
				// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
				session.setAttribute("successMsg", "Doctor added Successfully");
				resp.sendRedirect("admin/doctor.jsp");
				
			}
			else {
				// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("admin/doctor.jsp");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
}
