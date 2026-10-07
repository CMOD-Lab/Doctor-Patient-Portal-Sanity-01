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
class MyNewServletTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private MyNewServlet myNewServlet;

    @BeforeEach
    void setUp() {
        myNewServlet = new MyNewServlet();
    }

    @Test
    void myNewServlet_shouldBeInstantiable() {
        assertNotNull(myNewServlet);
    }

    @Test
    void defaultConstructor_shouldCreateServletInstance() {
        MyNewServlet servlet = new MyNewServlet();
        assertNotNull(servlet);
    }

    @Test
    void doGet_shouldWriteContextPath() throws Exception {
        when(mockRequest.getContextPath()).thenReturn("/myapp");
        jakarta.servlet.http.HttpServletResponseWrapper wrapper =
                mock(jakarta.servlet.http.HttpServletResponseWrapper.class);
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(writer);
        when(writer.append(anyString())).thenReturn(writer);

        myNewServlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).getWriter();
        verify(mockRequest).getContextPath();
    }

    @Test
    void doPost_shouldDelegateToDoGet() throws Exception {
        when(mockRequest.getContextPath()).thenReturn("/myapp");
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(writer);
        when(writer.append(anyString())).thenReturn(writer);

        myNewServlet.doPost(mockRequest, mockResponse);

        verify(mockResponse).getWriter();
    }
}
