package com.hms.admin.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLoginServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private AdminLoginServlet adminLoginServlet;

    @BeforeEach
    void setUp() {
        adminLoginServlet = new AdminLoginServlet();
    }

    // ── doPost – valid admin credentials ─────────────────────────────────────

    @Test
    void doPost_validAdminCredentials_setsSessionAndRedirectsToAdminIndex() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("admin@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("admin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute(eq("adminObj"), any());
        verify(mockResponse).sendRedirect("admin/index.jsp");
    }

    // ── doPost – invalid email ────────────────────────────────────────────────

    @Test
    void doPost_invalidEmail_setsErrorMsgAndRedirectsToAdminLogin() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("wrong@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("admin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    // ── doPost – invalid password ─────────────────────────────────────────────

    @Test
    void doPost_invalidPassword_setsErrorMsgAndRedirectsToAdminLogin() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("admin@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("wrongpass");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    // ── doPost – both invalid ─────────────────────────────────────────────────

    @Test
    void doPost_bothInvalid_setsErrorMsgAndRedirectsToAdminLogin() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("other@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("other");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    // ── doPost – null email ───────────────────────────────────────────────────

    @Test
    void doPost_nullEmail_setsErrorMsgAndRedirectsToAdminLogin() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn(null);
        when(mockRequest.getParameter("password")).thenReturn("admin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    // ── doPost – empty credentials ────────────────────────────────────────────

    @Test
    void doPost_emptyCredentials_setsErrorMsgAndRedirectsToAdminLogin() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("");
        when(mockRequest.getParameter("password")).thenReturn("");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }
}
