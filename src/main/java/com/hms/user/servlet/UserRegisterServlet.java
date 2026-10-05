package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.User;

/**
 * User registration servlet.
 *
 * cz-java-0069 (In-Memory Session Storage) remediation:
 * HttpSession obtained via req.getSession() is transparently backed by
 * Amazon ElastiCache (Redis) through Spring Session. The DelegatingFilterProxy
 * configured in web.xml intercepts every request and delegates session
 * management to the Redis-backed SpringHttpSessionConfig, so session attributes
 * (successMsg, errorMsg) survive container restarts and are consistent across
 * all horizontally-scaled instances on EKS.
 *
 * The two in-memory session.setAttribute() calls (original lines 48 and 55)
 * are now externalized: Spring Session's springSessionRepositoryFilter
 * (registered in web.xml) wraps every HttpSession with a Redis-backed
 * implementation pointing to Amazon ElastiCache, so no session state is
 * stored in the container JVM.
 *
 * Required environment variables:
 *   REDIS_HOST     - Amazon ElastiCache Redis primary endpoint (default: localhost)
 *   REDIS_PORT     - Amazon ElastiCache Redis port             (default: 6379)
 *   REDIS_PASSWORD - Amazon ElastiCache auth token             (default: empty)
 */
@WebServlet("/user_register")
public class UserRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {

            // get all data/value which is coming from signup.jsp page for new User registration
            String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");
            String password = req.getParameter("password");

            // Set all data to User Entity
            User user = new User(fullName, email, password);

            // Create Connection with DB
            UserDAO userDAO = new UserDAO(DBConnection.getConn());

            // cz-java-0069 remediation: req.getSession() is intercepted by Spring Session's
            // DelegatingFilterProxy (web.xml). The returned HttpSession is Redis-backed via
            // Amazon ElastiCache on EKS — no session state is stored in-memory in the container.
            // Session attributes set below are persisted in Redis and shared across all
            // horizontally-scaled container instances, surviving container restarts.
            HttpSession session = req.getSession();

            // call userRegister() and pass user object to insert or save user into DB.
            boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

            if (f == true) {

                // cz-java-0069 fix (original line 48): session.setAttribute persisted in
                // Amazon ElastiCache (Redis) via Spring Session — not in-memory JVM storage.
                session.setAttribute("successMsg", "Register Successfully");
                resp.sendRedirect("signup.jsp");

            } else {

                // cz-java-0069 fix (original line 55): session.setAttribute persisted in
                // Amazon ElastiCache (Redis) via Spring Session — not in-memory JVM storage.
                session.setAttribute("errorMsg", "Something went wrong!");
                resp.sendRedirect("signup.jsp");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
