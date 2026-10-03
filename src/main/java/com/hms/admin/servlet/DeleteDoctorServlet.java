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

@WebServlet("/deleteDoctor")
public class DeleteDoctorServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//get id(which is coming as string value) and convert into int	
		int id = Integer.parseInt(req.getParameter("id"));
		
		DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());
		// cz-java-0069: req.getSession() returns a Redis-backed HttpSession via Spring Session.
		// Session state is stored in Amazon ElastiCache (Redis), enabling horizontal scaling on EKS.
		HttpSession session = req.getSession();
		
		boolean f = docDAO.deleteDoctorById(id);
		
		if(f==true) {
			// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
			session.setAttribute("successMsg", "Doctor Deleted Successfully.");
			resp.sendRedirect("admin/view_doctor.jsp");
		}
		else {
			// cz-java-0069: session.setAttribute stores data in Redis (ElastiCache), not in-memory.
			session.setAttribute("errorMsg", "Something went wrong on server!");
			resp.sendRedirect("admin/view_doctor.jsp");
		}
	}
	
	

}
