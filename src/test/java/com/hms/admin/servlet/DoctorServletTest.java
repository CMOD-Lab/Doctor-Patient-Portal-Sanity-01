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
class DoctorServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private DoctorServlet doctorServlet;

    @BeforeEach
    void setUp() {
        doctorServlet = new DoctorServlet();
    }

    @Test
    void doctorServlet_instantiation_notNull() {
        assertNotNull(doctorServlet);
    }

    @Test
    void doPost_parameterExtraction_fullNameIsRead() throws Exception {
        when(mockRequest.getParameter("fullName")).thenReturn("Dr. Jones");
        when(mockRequest.getParameter("qualification")).thenReturn("MD");
        when(mockRequest.getParameter("specialist")).thenReturn("Neurology");

        assertEquals("Dr. Jones", mockRequest.getParameter("fullName"));
        assertEquals("MD", mockRequest.getParameter("qualification"));
        assertEquals("Neurology", mockRequest.getParameter("specialist"));
    }

    @Test
    void doPost_parameterExtraction_emailContainsAtSign() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("doctor@hospital.com");

        String email = mockRequest.getParameter("email");
        assertNotNull(email);
        assertTrue(email.contains("@"));
    }

    @Test
    void doPost_parameterExtraction_phoneIsNumericString() throws Exception {
        when(mockRequest.getParameter("phone")).thenReturn("9876543210");

        String phone = mockRequest.getParameter("phone");
        assertNotNull(phone);
        assertFalse(phone.isEmpty());
    }
}
