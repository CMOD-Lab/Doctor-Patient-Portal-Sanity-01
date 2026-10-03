package com.hms.user.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyNewServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    private MyNewServlet myNewServlet;

    @BeforeEach
    void setUp() {
        myNewServlet = new MyNewServlet();
    }

    @Test
    void myNewServlet_instantiation_createsNonNullObject() {
        assertNotNull(myNewServlet);
    }

    @Test
    void defaultConstructor_createsInstance() {
        MyNewServlet servlet = new MyNewServlet();
        assertNotNull(servlet);
    }

    @Test
    void doGet_withContextPath_writesResponse() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(mockRequest.getContextPath()).thenReturn("/myapp");
        when(mockResponse.getWriter()).thenReturn(printWriter);

        myNewServlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).getWriter();
        verify(mockRequest).getContextPath();
        assertTrue(stringWriter.toString().contains("/myapp"));
    }

    @Test
    void doPost_delegatesToDoGet() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(mockRequest.getContextPath()).thenReturn("/myapp");
        when(mockResponse.getWriter()).thenReturn(printWriter);

        myNewServlet.doPost(mockRequest, mockResponse);

        verify(mockResponse).getWriter();
    }

    @Test
    void doGet_withEmptyContextPath_writesResponse() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockResponse.getWriter()).thenReturn(printWriter);

        myNewServlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).getWriter();
        assertTrue(stringWriter.toString().contains("Served at:"));
    }
}
