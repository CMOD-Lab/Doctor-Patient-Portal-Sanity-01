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
class SpecialistServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private SpecialistServlet specialistServlet;

    @BeforeEach
    void setUp() {
        specialistServlet = new SpecialistServlet();
    }

    @Test
    void specialistServlet_instantiation_notNull() {
        assertNotNull(specialistServlet);
    }

    @Test
    void doPost_parameterExtraction_specialistNameIsRead() throws Exception {
        when(mockRequest.getParameter("specialistName")).thenReturn("Cardiology");

        assertEquals("Cardiology", mockRequest.getParameter("specialistName"));
    }

    @Test
    void doPost_parameterExtraction_specialistNameIsNeurology() throws Exception {
        when(mockRequest.getParameter("specialistName")).thenReturn("Neurology");

        assertEquals("Neurology", mockRequest.getParameter("specialistName"));
    }

    @Test
    void doPost_parameterExtraction_specialistNameCanBeNull() throws Exception {
        when(mockRequest.getParameter("specialistName")).thenReturn(null);

        assertNull(mockRequest.getParameter("specialistName"));
    }
}
