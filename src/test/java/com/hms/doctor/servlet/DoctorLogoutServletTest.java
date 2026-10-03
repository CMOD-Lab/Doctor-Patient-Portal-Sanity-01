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
class DoctorLogoutServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private DoctorLogoutServlet doctorLogoutServlet;

    @BeforeEach
    void setUp() {
        doctorLogoutServlet = new DoctorLogoutServlet();
    }

    @Test
    void doctorLogoutServlet_instantiation_createsNonNullObject() {
        assertNotNull(doctorLogoutServlet);
    }

    @Test
    void doGet_removesDoctorObjFromSession() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        doctorLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockSession).removeAttribute("doctorObj");
    }

    @Test
    void doGet_setsSuccessMessage() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        doctorLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockSession).setAttribute("successMsg", "Doctor Logout Successfully.");
    }

    @Test
    void doGet_redirectsToDoctorLoginPage() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        doctorLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).sendRedirect("doctor_login.jsp");
    }

    @Test
    void doGet_executesAllStepsInOrder() throws Exception {
        when(mockRequest.getSession()).thenReturn(mockSession);

        doctorLogoutServlet.doGet(mockRequest, mockResponse);

        verify(mockRequest).getSession();
        verify(mockSession).removeAttribute("doctorObj");
        verify(mockSession).setAttribute("successMsg", "Doctor Logout Successfully.");
        verify(mockResponse).sendRedirect("doctor_login.jsp");
    }
}
