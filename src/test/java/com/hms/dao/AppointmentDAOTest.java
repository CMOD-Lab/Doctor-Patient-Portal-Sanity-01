package com.hms.dao;

import com.hms.entity.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private AppointmentDAO appointmentDAO;

    @BeforeEach
    void setUp() {
        appointmentDAO = new AppointmentDAO(mockConnection);
    }

    // ── Constructor Test ───────────────────────────────────────────────────────

    @Test
    void constructor_shouldCreateAppointmentDAOWithConnection() {
        AppointmentDAO dao = new AppointmentDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── addAppointment Tests ───────────────────────────────────────────────────

    @Test
    void addAppointment_whenSuccessful_shouldReturnTrue() throws Exception {
        Appointment appointment = new Appointment(1, "John", "Male", "30",
                "2024-01-15", "john@test.com", "1234567890", "Fever", 2, "123 Main St", "Pending");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.addAppointment(appointment);

        assertTrue(result);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addAppointment_whenExceptionThrown_shouldReturnFalse() throws Exception {
        Appointment appointment = new Appointment(1, "John", "Male", "30",
                "2024-01-15", "john@test.com", "1234567890", "Fever", 2, "123 Main St", "Pending");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = appointmentDAO.addAppointment(appointment);

        assertFalse(result);
    }

    // ── getAllAppointmentByLoginUser Tests ─────────────────────────────────────

    @Test
    void getAllAppointmentByLoginUser_whenNoResults_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointmentByLoginUser_whenResultsExist_shouldReturnList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getInt(2)).thenReturn(1);
        when(mockResultSet.getString(3)).thenReturn("John");
        when(mockResultSet.getString(4)).thenReturn("Male");
        when(mockResultSet.getString(5)).thenReturn("30");
        when(mockResultSet.getString(6)).thenReturn("2024-01-15");
        when(mockResultSet.getString(7)).thenReturn("john@test.com");
        when(mockResultSet.getString(8)).thenReturn("1234567890");
        when(mockResultSet.getString(9)).thenReturn("Fever");
        when(mockResultSet.getInt(10)).thenReturn(2);
        when(mockResultSet.getString(11)).thenReturn("123 Main St");
        when(mockResultSet.getString(12)).thenReturn("Pending");

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFullName());
        assertEquals("Pending", result.get(0).getStatus());
    }

    @Test
    void getAllAppointmentByLoginUser_whenExceptionThrown_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getAllAppointmentByLoginDoctor Tests ───────────────────────────────────

    @Test
    void getAllAppointmentByLoginDoctor_whenNoResults_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointmentByLoginDoctor_whenResultsExist_shouldReturnList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(1, 2);
        when(mockResultSet.getInt(2)).thenReturn(1, 2);
        when(mockResultSet.getString(3)).thenReturn("Patient1", "Patient2");
        when(mockResultSet.getString(4)).thenReturn("Male", "Female");
        when(mockResultSet.getString(5)).thenReturn("30", "25");
        when(mockResultSet.getString(6)).thenReturn("2024-01-15", "2024-01-16");
        when(mockResultSet.getString(7)).thenReturn("p1@test.com", "p2@test.com");
        when(mockResultSet.getString(8)).thenReturn("111", "222");
        when(mockResultSet.getString(9)).thenReturn("Fever", "Cold");
        when(mockResultSet.getInt(10)).thenReturn(2, 2);
        when(mockResultSet.getString(11)).thenReturn("Addr1", "Addr2");
        when(mockResultSet.getString(12)).thenReturn("Pending", "Approved");

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(2);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllAppointmentByLoginDoctor_whenExceptionThrown_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getAppointmentById Tests ───────────────────────────────────────────────

    @Test
    void getAppointmentById_whenFound_shouldReturnAppointment() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(5);
        when(mockResultSet.getInt(2)).thenReturn(1);
        when(mockResultSet.getString(3)).thenReturn("Jane");
        when(mockResultSet.getString(4)).thenReturn("Female");
        when(mockResultSet.getString(5)).thenReturn("28");
        when(mockResultSet.getString(6)).thenReturn("2024-02-10");
        when(mockResultSet.getString(7)).thenReturn("jane@test.com");
        when(mockResultSet.getString(8)).thenReturn("9876543210");
        when(mockResultSet.getString(9)).thenReturn("Headache");
        when(mockResultSet.getInt(10)).thenReturn(3);
        when(mockResultSet.getString(11)).thenReturn("456 Oak Ave");
        when(mockResultSet.getString(12)).thenReturn("Completed");

        Appointment result = appointmentDAO.getAppointmentById(5);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals("Jane", result.getFullName());
        assertEquals("Completed", result.getStatus());
    }

    @Test
    void getAppointmentById_whenNotFound_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Appointment result = appointmentDAO.getAppointmentById(999);

        assertNull(result);
    }

    @Test
    void getAppointmentById_whenExceptionThrown_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        Appointment result = appointmentDAO.getAppointmentById(1);

        assertNull(result);
    }

    // ── updateDrAppointmentCommentStatus Tests ─────────────────────────────────

    @Test
    void updateDrAppointmentCommentStatus_whenSuccessful_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.updateDrAppointmentCommentStatus(1, 2, "Approved");

        assertTrue(result);
    }

    @Test
    void updateDrAppointmentCommentStatus_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = appointmentDAO.updateDrAppointmentCommentStatus(1, 2, "Approved");

        assertFalse(result);
    }

    // ── getAllAppointment Tests ─────────────────────────────────────────────────

    @Test
    void getAllAppointment_whenNoResults_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointment_whenResultsExist_shouldReturnList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getInt(2)).thenReturn(1);
        when(mockResultSet.getString(3)).thenReturn("Bob");
        when(mockResultSet.getString(4)).thenReturn("Male");
        when(mockResultSet.getString(5)).thenReturn("40");
        when(mockResultSet.getString(6)).thenReturn("2024-03-01");
        when(mockResultSet.getString(7)).thenReturn("bob@test.com");
        when(mockResultSet.getString(8)).thenReturn("5551234567");
        when(mockResultSet.getString(9)).thenReturn("Diabetes");
        when(mockResultSet.getInt(10)).thenReturn(1);
        when(mockResultSet.getString(11)).thenReturn("789 Pine Rd");
        when(mockResultSet.getString(12)).thenReturn("Pending");

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getFullName());
    }

    @Test
    void getAllAppointment_whenExceptionThrown_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
