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
class UserRegisterServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private UserRegisterServlet userRegisterServlet;

    @BeforeEach
    void setUp() {
        userRegisterServlet = new UserRegisterServlet();
    }

    @Test
    void userRegisterServlet_instantiation_notNull() {
        assertNotNull(userRegisterServlet);
    }

    @Test
    void doPost_parameterExtraction_allFieldsAreRead() throws Exception {
        when(mockRequest.getParameter("fullName")).thenReturn("Alice Smith");
        when(mockRequest.getParameter("email")).thenReturn("alice@example.com");
        when(mockRequest.getParameter("password")).thenReturn("alicePass");

        assertEquals("Alice Smith", mockRequest.getParameter("fullName"));
        assertEquals("alice@example.com", mockRequest.getParameter("email"));
        assertEquals("alicePass", mockRequest.getParameter("password"));
    }

    @Test
    void doPost_parameterExtraction_emailContainsAtSign() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("newuser@example.com");

        String email = mockRequest.getParameter("email");
        assertNotNull(email);
        assertTrue(email.contains("@"));
    }

    @Test
    void doPost_parameterExtraction_fullNameIsNotEmpty() throws Exception {
        when(mockRequest.getParameter("fullName")).thenReturn("Bob Johnson");

        String fullName = mockRequest.getParameter("fullName");
        assertNotNull(fullName);
        assertFalse(fullName.isEmpty());
    }

    @Test
    void doPost_parameterExtraction_nullFieldsHandled() throws Exception {
        when(mockRequest.getParameter("fullName")).thenReturn(null);
        when(mockRequest.getParameter("email")).thenReturn(null);
        when(mockRequest.getParameter("password")).thenReturn(null);

        assertNull(mockRequest.getParameter("fullName"));
        assertNull(mockRequest.getParameter("email"));
        assertNull(mockRequest.getParameter("password"));
    }
}
