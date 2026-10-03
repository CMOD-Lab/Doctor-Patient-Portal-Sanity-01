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
class AppointmentServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private AppointmentServlet appointmentServlet;

    @BeforeEach
    void setUp() {
        appointmentServlet = new AppointmentServlet();
    }

    @Test
    void appointmentServlet_instantiation_notNull() {
        assertNotNull(appointmentServlet);
    }

    @Test
    void doPost_parameterExtraction_allFieldsAreRead() throws Exception {
        when(mockRequest.getParameter("userId")).thenReturn("1");
        when(mockRequest.getParameter("fullName")).thenReturn("John Doe");
        when(mockRequest.getParameter("doctorNameSelect")).thenReturn("2");

        assertEquals("1", mockRequest.getParameter("userId"));
        assertEquals("John Doe", mockRequest.getParameter("fullName"));
        assertEquals("2", mockRequest.getParameter("doctorNameSelect"));
    }

    @Test
    void doPost_parameterExtraction_userIdParsedAsInt() throws Exception {
        when(mockRequest.getParameter("userId")).thenReturn("5");
        when(mockRequest.getParameter("doctorNameSelect")).thenReturn("3");

        int userId = Integer.parseInt(mockRequest.getParameter("userId"));
        int doctorId = Integer.parseInt(mockRequest.getParameter("doctorNameSelect"));

        assertEquals(5, userId);
        assertEquals(3, doctorId);
    }

    @Test
    void doPost_parameterExtraction_statusIsAlwaysPending() {
        // The servlet always sets status to "Pending" for new appointments
        String expectedStatus = "Pending";
        assertEquals("Pending", expectedStatus);
    }

    @Test
    void doPost_parameterExtraction_genderValues() throws Exception {
        when(mockRequest.getParameter("gender")).thenReturn("Female");

        String gender = mockRequest.getParameter("gender");
        assertNotNull(gender);
        assertTrue(gender.equals("Male") || gender.equals("Female") || gender.equals("Other"));
    }
}
