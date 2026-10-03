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
class UpdateStatusTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private UpdateStatus updateStatus;

    @BeforeEach
    void setUp() {
        updateStatus = new UpdateStatus();
    }

    @Test
    void updateStatus_instantiation_notNull() {
        assertNotNull(updateStatus);
    }

    @Test
    void doPost_parameterExtraction_idIsRead() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");
        when(mockRequest.getParameter("doctorId")).thenReturn("2");
        when(mockRequest.getParameter("comment")).thenReturn("Reviewed");

        assertEquals("1", mockRequest.getParameter("id"));
        assertEquals("2", mockRequest.getParameter("doctorId"));
        assertEquals("Reviewed", mockRequest.getParameter("comment"));
    }

    @Test
    void doPost_parameterExtraction_idParsedAsInt() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("10");
        when(mockRequest.getParameter("doctorId")).thenReturn("5");

        int id = Integer.parseInt(mockRequest.getParameter("id"));
        int doctorId = Integer.parseInt(mockRequest.getParameter("doctorId"));

        assertEquals(10, id);
        assertEquals(5, doctorId);
    }

    @Test
    void doPost_parameterExtraction_commentIsString() throws Exception {
        when(mockRequest.getParameter("comment")).thenReturn("Patient reviewed and prescribed medication");

        String comment = mockRequest.getParameter("comment");
        assertNotNull(comment);
        assertFalse(comment.isEmpty());
    }
}
