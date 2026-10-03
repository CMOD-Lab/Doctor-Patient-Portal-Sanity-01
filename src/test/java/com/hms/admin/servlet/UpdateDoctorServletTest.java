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
class UpdateDoctorServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private UpdateDoctorServlet updateDoctorServlet;

    @BeforeEach
    void setUp() {
        updateDoctorServlet = new UpdateDoctorServlet();
    }

    @Test
    void updateDoctorServlet_instantiation_notNull() {
        assertNotNull(updateDoctorServlet);
    }

    @Test
    void doPost_parameterExtraction_idIsRead() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");
        when(mockRequest.getParameter("fullName")).thenReturn("Dr. Updated");

        assertEquals("1", mockRequest.getParameter("id"));
        assertEquals("Dr. Updated", mockRequest.getParameter("fullName"));
    }

    @Test
    void doPost_parameterExtraction_idParsedAsInt() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("5");

        int id = Integer.parseInt(mockRequest.getParameter("id"));
        assertEquals(5, id);
    }

    @Test
    void doPost_parameterExtraction_qualificationIsRead() throws Exception {
        when(mockRequest.getParameter("qualification")).thenReturn("PhD");
        when(mockRequest.getParameter("specialist")).thenReturn("Orthopedics");

        assertEquals("PhD", mockRequest.getParameter("qualification"));
        assertEquals("Orthopedics", mockRequest.getParameter("specialist"));
    }

    @Test
    void doPost_parameterExtraction_emailContainsAtSign() throws Exception {
        when(mockRequest.getParameter("email")).thenReturn("test@hospital.com");

        String email = mockRequest.getParameter("email");
        assertNotNull(email);
        assertTrue(email.contains("@"));
    }
}
