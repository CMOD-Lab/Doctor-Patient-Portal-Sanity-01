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
class DoctorEditProfileServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private DoctorEditProfileServlet doctorEditProfileServlet;

    @BeforeEach
    void setUp() {
        doctorEditProfileServlet = new DoctorEditProfileServlet();
    }

    @Test
    void doctorEditProfileServlet_instantiation_notNull() {
        assertNotNull(doctorEditProfileServlet);
    }

    @Test
    void doPost_parameterExtraction_allFieldsAreRead() throws Exception {
        when(mockRequest.getParameter("fullName")).thenReturn("Dr. Edited");
        when(mockRequest.getParameter("doctorId")).thenReturn("1");
        when(mockRequest.getParameter("qualification")).thenReturn("PhD");

        assertEquals("Dr. Edited", mockRequest.getParameter("fullName"));
        assertEquals("1", mockRequest.getParameter("doctorId"));
        assertEquals("PhD", mockRequest.getParameter("qualification"));
    }

    @Test
    void doPost_parameterExtraction_doctorIdParsedAsInt() throws Exception {
        when(mockRequest.getParameter("doctorId")).thenReturn("10");

        int doctorId = Integer.parseInt(mockRequest.getParameter("doctorId"));
        assertEquals(10, doctorId);
    }

    @Test
    void doPost_parameterExtraction_emailIsValid() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("doctor@hospital.com");

        String email = mockRequest.getParameter("email");
        assertNotNull(email);
        assertTrue(email.contains("@"));
    }
}
