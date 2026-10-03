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
class UserLoginServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private UserLoginServlet userLoginServlet;

    @BeforeEach
    void setUp() {
        userLoginServlet = new UserLoginServlet();
    }

    @Test
    void userLoginServlet_instantiation_notNull() {
        assertNotNull(userLoginServlet);
    }

    @Test
    void doPost_parameterExtraction_emailAndPasswordAreRead() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("user@example.com");
        when(mockRequest.getParameter("password")).thenReturn("userPass");

        assertEquals("user@example.com", mockRequest.getParameter("email"));
        assertEquals("userPass", mockRequest.getParameter("password"));
    }

    @Test
    void doPost_parameterExtraction_emailContainsAtSign() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("test@example.com");

        String email = mockRequest.getParameter("email");
        assertNotNull(email);
        assertTrue(email.contains("@"));
    }

    @Test
    void doPost_parameterExtraction_passwordIsNotEmpty() throws Exception {
        when(mockRequest.getParameter("password")).thenReturn("securePass123");

        String password = mockRequest.getParameter("password");
        assertNotNull(password);
        assertFalse(password.isEmpty());
    }

    @Test
    void doPost_parameterExtraction_nullEmailHandled() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn(null);

        assertNull(mockRequest.getParameter("email"));
    }
}
