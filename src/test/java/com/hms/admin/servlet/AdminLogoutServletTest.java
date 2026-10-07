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
class AdminLogoutServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private AdminLogoutServlet adminLogoutServlet;

    @BeforeEach
    void setUp() {
        adminLogoutServlet = new AdminLogoutServlet();
    }

    @Test
    void adminLogoutServlet_shouldBeInstantiable() {
        assertNotNull(adminLogoutServlet);
    }

    @Test
    void doGet_shouldRemoveAdminObjFromSession() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockSession).removeAttribute("adminObj");
    }

    @Test
    void doGet_shouldSetSuccessMessage() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockSession).setAttribute("successMsg", "Admin Logout Successfully");
    }

    @Test
    void doGet_shouldRedirectToAdminLoginPage() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).sendRedirect("admin_login.jsp");
    }

    @Test
    void doGet_shouldCallGetSession() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        adminLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockRequest).getSession();
    }
}
