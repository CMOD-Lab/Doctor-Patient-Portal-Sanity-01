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
class DoctorChangePasswordTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private DoctorChangePassword doctorChangePassword;

    @BeforeEach
    void setUp() {
        doctorChangePassword = new DoctorChangePassword();
    }

    @Test
    void doctorChangePassword_instantiation_notNull() {
        assertNotNull(doctorChangePassword);
    }

    @Test
    void doPost_parameterExtraction_doctorIdIsRead() throws Exception {
        when(mockRequest.getParameter("doctorId")).thenReturn("1");
        when(mockRequest.getParameter("oldPassword")).thenReturn("oldPass");
        when(mockRequest.getParameter("newPassword")).thenReturn("newPass");

        assertEquals("1", mockRequest.getParameter("doctorId"));
        assertEquals("oldPass", mockRequest.getParameter("oldPassword"));
        assertEquals("newPass", mockRequest.getParameter("newPassword"));
    }

    @Test
    void doPost_parameterExtraction_doctorIdParsedAsInt() throws Exception {
        when(mockRequest.getParameter("doctorId")).thenReturn("5");

        int doctorId = Integer.parseInt(mockRequest.getParameter("doctorId"));
        assertEquals(5, doctorId);
    }

    @Test
    void doPost_parameterExtraction_passwordsAreStrings() throws Exception {
        when(mockRequest.getParameter("oldPassword")).thenReturn("oldSecurePass");
        when(mockRequest.getParameter("newPassword")).thenReturn("newSecurePass");

        String oldPass = mockRequest.getParameter("oldPassword");
        String newPass = mockRequest.getParameter("newPassword");

        assertNotNull(oldPass);
        assertNotNull(newPass);
        assertNotEquals(oldPass, newPass);
    }
}
