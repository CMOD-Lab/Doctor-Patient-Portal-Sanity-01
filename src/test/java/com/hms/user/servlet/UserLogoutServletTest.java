package com.hms.user.servlet;

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
class UserLogoutServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private UserLogoutServlet userLogoutServlet;

    @BeforeEach
    void setUp() {
        userLogoutServlet = new UserLogoutServlet();
    }

    @Test
    void userLogoutServlet_shouldBeInstantiable() {
        assertNotNull(userLogoutServlet);
    }

    @Test
    void doGet_shouldRemoveUserObjFromSession() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        userLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockSession).removeAttribute("userObj");
    }

    @Test
    void doGet_shouldSetSuccessMessage() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        userLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockSession).setAttribute("successMsg", "User Logout Successfully.");
    }

    @Test
    void doGet_shouldRedirectToLoginPage() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        userLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).sendRedirect("user_login.jsp");
    }

    @Test
    void doGet_shouldCallGetSession() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        userLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockRequest).getSession();
    }
}
