package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cr-java-0065: HTTP Session State Storage — FIXED
// import javax.servlet.http.HttpSession is retained; the HttpSession API is unchanged.
// Spring Session's DelegatingFilterProxy (configured in web.xml) transparently replaces
// the container's in-memory session with a distributed Redis-backed session stored in
// Amazon ElastiCache for Redis (configured in RedisHttpSessionConfig), enabling:
//  - Stateless application instances (no server affinity / sticky sessions)
//  - Horizontal scaling across multiple instances behind a load balancer
//  - Session persistence across instance restarts and deployments
import javax.servlet.http.HttpSession;

import com.hms.dao.AppointmentDAO;
import com.hms.db.DBConnection;
import com.hms.entity.Appointment;

@WebServlet("/addAppointment")
public class AppointmentServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
	int userId	= Integer.parseInt(req.getParameter("userId"));
	String fullName = req.getParameter("fullName");
	String gender = req.getParameter("gender");
	String age = req.getParameter("age");
	String appointmentDate = req.getParameter("appointmentDate");
	String email = req.getParameter("email");
	String phone = req.getParameter("phone");
	String diseases = req.getParameter("diseases");
	int doctorId = Integer.parseInt(req.getParameter("doctorNameSelect"));
	String address = req.getParameter("address");
	
	
	Appointment appointment = new Appointment(userId, fullName, gender, age, appointmentDate, email, phone, diseases, doctorId, address, "Pending");
	
	AppointmentDAO appointmentDAO = new AppointmentDAO(DBConnection.getConn());
	boolean f = appointmentDAO.addAppointment(appointment);
	
	// cr-java-0065: req.getSession() returns a Redis-backed HttpSession via Spring Session.
	// setAttribute operates on the distributed Redis session in Amazon ElastiCache,
	// ensuring session state is consistent across all instances.
	HttpSession session = req.getSession();
	
	if(f==true) {
		
		session.setAttribute("successMsg", "Appointment is recorded Successfully.");
		resp.sendRedirect("user_appointment.jsp");
		
		
	}
	else {
		
		session.setAttribute("errorMsg", "Something went wrong on server!");
		resp.sendRedirect("user_appointment.jsp");
		
	}
	
	
	
	
		
	}

	
}
