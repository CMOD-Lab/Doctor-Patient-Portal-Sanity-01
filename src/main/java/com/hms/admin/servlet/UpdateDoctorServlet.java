package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// HttpSession is now backed by Amazon ElastiCache for Redis via Spring Session
// (cr-java-0065). The existing HttpSession API is preserved; Spring Session's
// springSessionRepositoryFilter (registered in web.xml) transparently replaces
// the server-local session store with a distributed Redis store, enabling
// stateless, horizontally scalable instances without server affinity.
import javax.servlet.http.HttpSession;

import com.hms.dao.DoctorDAO;
import com.hms.db.DBConnection;
import com.hms.entity.Doctor;

@WebServlet("/updateDoctor")
public class UpdateDoctorServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {

			// get all data which is coming from doctor.jsp doctor details
			String fullName = req.getParameter("fullName");
			String dateOfBirth = req.getParameter("dateOfBirth");
			String qualification = req.getParameter("qualification");
			String specialist = req.getParameter("specialist");
			String email = req.getParameter("email");
			String phone = req.getParameter("phone");
			String password = req.getParameter("password");
			
			//here need to get id also...for updating the doctor details
			//doctors will update based on respective doctor's id
			int id = Integer.parseInt(req.getParameter("id"));

			Doctor doctor = new Doctor(id, fullName, dateOfBirth, qualification, specialist, email, phone, password);

			DoctorDAO docDAO = new DoctorDAO(DBConnection.getConn());

			boolean f = docDAO.updateDoctor(doctor);

			// Spring Session intercepts getSession() and returns a Redis-backed session
			HttpSession session = req.getSession();

			if (f == true) {
				// Success message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("successMsg", "Doctor update Successfully");
				resp.sendRedirect("admin/view_doctor.jsp");

			} else {
				// Error message stored in Redis-backed distributed session (cr-java-0065)
				session.setAttribute("errorMsg", "Something went wrong on server!");
				resp.sendRedirect("admin/view_doctor.jsp");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
