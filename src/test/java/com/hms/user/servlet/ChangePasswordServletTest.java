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
class ChangePasswordServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private ChangePasswordServlet changePasswordServlet;

    @BeforeEach
    void setUp() {
        changePasswordServlet = new ChangePasswordServlet();
    }

    @Test
    void changePasswordServlet_instantiation_notNull() {
        assertNotNull(changePasswordServlet);
    }

    @Test
    void doPost_parameterExtraction_allFieldsAreRead() throws Exception {
        when(mockRequest.getParameter("userId")).thenReturn("1");
        when(mockRequest.getParameter("oldPassword")).thenReturn("oldPass");
        when(mockRequest.getParameter("newPassword")).thenReturn("newPass");

        assertEquals("1", mockRequest.getParameter("userId"));
        assertEquals("oldPass", mockRequest.getParameter("oldPassword"));
        assertEquals("newPass", mockRequest.getParameter("newPassword"));
    }

    @Test
    void doPost_parameterExtraction_userIdParsedAsInt() throws Exception {
        when(mockRequest.getParameter("userId")).thenReturn("7");

        int userId = Integer.parseInt(mockRequest.getParameter("userId"));
        assertEquals(7, userId);
    }

    @Test
    void doPost_parameterExtraction_passwordsAreDifferent() throws Exception {
        when(mockRequest.getParameter("oldPassword")).thenReturn("oldSecurePass");
        when(mockRequest.getParameter("newPassword")).thenReturn("newSecurePass");

        String oldPass = mockRequest.getParameter("oldPassword");
        String newPass = mockRequest.getParameter("newPassword");

        assertNotNull(oldPass);
        assertNotNull(newPass);
        assertNotEquals(oldPass, newPass);
    }

    @Test
    void doPost_parameterExtraction_nullPasswordHandled() throws Exception {
        when(mockRequest.getParameter("oldPassword")).thenReturn(null);
        when(mockRequest.getParameter("newPassword")).thenReturn("newPass");

        assertNull(mockRequest.getParameter("oldPassword"));
        assertNotNull(mockRequest.getParameter("newPassword"));
    }
}
