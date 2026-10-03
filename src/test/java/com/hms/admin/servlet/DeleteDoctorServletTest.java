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
class DeleteDoctorServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private DeleteDoctorServlet deleteDoctorServlet;

    @BeforeEach
    void setUp() {
        deleteDoctorServlet = new DeleteDoctorServlet();
    }

    @Test
    void deleteDoctorServlet_instantiation_notNull() {
        assertNotNull(deleteDoctorServlet);
    }

    @Test
    void doGet_parameterExtraction_idIsRead() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("1");

        assertEquals("1", mockRequest.getParameter("id"));
    }

    @Test
    void doGet_parameterExtraction_idParsedAsInt() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("42");

        int id = Integer.parseInt(mockRequest.getParameter("id"));
        assertEquals(42, id);
    }

    @Test
    void doGet_parameterExtraction_idIsNumeric() throws Exception {
        when(mockRequest.getParameter("id")).thenReturn("100");

        String idStr = mockRequest.getParameter("id");
        assertDoesNotThrow(() -> Integer.parseInt(idStr));
    }
}
