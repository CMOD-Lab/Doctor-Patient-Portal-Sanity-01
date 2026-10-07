package com.hms.admin.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void adminLoginServlet_shouldBeInstantiable() {
        assertNotNull(adminLoginServlet);
    }

    @Test
    void doPost_withValidAdminCredentials_shouldSetAdminObjAndRedirect() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("admin@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("admin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute(eq("adminObj"), any());
        verify(mockResponse).sendRedirect("admin/index.jsp");
    }

    @Test
    void doPost_withInvalidEmail_shouldSetErrorMsgAndRedirect() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("wrong@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("admin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    @Test
    void doPost_withInvalidPassword_shouldSetErrorMsgAndRedirect() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("admin@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("wrongpassword");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    @Test
    void doPost_withBothInvalidCredentials_shouldSetErrorMsgAndRedirect() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("notadmin@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("notadmin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    @Test
    void doPost_withEmptyCredentials_shouldSetErrorMsgAndRedirect() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("");
        when(mockRequest.getParameter("password")).thenReturn("");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockSession).setAttribute("errorMsg", "Invalid Username or Password.");
        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    @Test
    void doPost_withValidCredentials_shouldNotRedirectToLoginPage() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("admin@gmail.com");
        when(mockRequest.getParameter("password")).thenReturn("admin");
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLoginServlet.doPost(mockRequest, mockResponse);

        verify(mockResponse, never()).sendRedirect("admin_login.jsp");
    }
}
