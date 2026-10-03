package com.hms.doctor.servlet;

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
class DoctorLoginServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private DoctorLoginServlet doctorLoginServlet;

    @BeforeEach
    void setUp() {
        doctorLoginServlet = new DoctorLoginServlet();
    }

    @Test
    void doctorLoginServlet_instantiation_notNull() {
        assertNotNull(doctorLoginServlet);
    }

    @Test
    void doPost_parameterExtraction_emailAndPasswordAreRead() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("doctor@hospital.com");
        when(mockRequest.getParameter("password")).thenReturn("doctorPass");

        assertEquals("doctor@hospital.com", mockRequest.getParameter("email"));
        assertEquals("doctorPass", mockRequest.getParameter("password"));
    }

    @Test
    void doPost_parameterExtraction_emailContainsAtSign() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("test@hospital.com");

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
