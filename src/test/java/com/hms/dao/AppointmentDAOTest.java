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

    // ── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructor_withConnection_createsInstance() {
        AppointmentDAO dao = new AppointmentDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── addAppointment ───────────────────────────────────────────────────────

    @Test
    void addAppointment_success_returnsTrue() throws Exception {
        Appointment appointment = new Appointment(1, 10, "John Doe", "Male", "30",
                "2024-01-15", "john@example.com", "1234567890",
                "Fever", 5, "123 Main St", "Pending");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.addAppointment(appointment);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, appointment.getUserId());
        verify(mockPreparedStatement).setString(2, appointment.getFullName());
        verify(mockPreparedStatement).setString(3, appointment.getGender());
        verify(mockPreparedStatement).setString(4, appointment.getAge());
        verify(mockPreparedStatement).setString(5, appointment.getAppointmentDate());
        verify(mockPreparedStatement).setString(6, appointment.getEmail());
        verify(mockPreparedStatement).setString(7, appointment.getPhone());
        verify(mockPreparedStatement).setString(8, appointment.getDiseases());
        verify(mockPreparedStatement).setInt(9, appointment.getDoctorId());
        verify(mockPreparedStatement).setString(10, appointment.getAddress());
        verify(mockPreparedStatement).setString(11, appointment.getStatus());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addAppointment_sqlException_returnsFalse() throws Exception {
        Appointment appointment = new Appointment(1, 10, "John Doe", "Male", "30",
                "2024-01-15", "john@example.com", "1234567890",
                "Fever", 5, "123 Main St", "Pending");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = appointmentDAO.addAppointment(appointment);

        assertFalse(result);
    }

    // ── getAllAppointmentByLoginUser ──────────────────────────────────────────

    @Test
    void getAllAppointmentByLoginUser_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getInt(2)).thenReturn(10);
        when(mockResultSet.getString(3)).thenReturn("John Doe");
        when(mockResultSet.getString(4)).thenReturn("Male");
        when(mockResultSet.getString(5)).thenReturn("30");
        when(mockResultSet.getString(6)).thenReturn("2024-01-15");
        when(mockResultSet.getString(7)).thenReturn("john@example.com");
        when(mockResultSet.getString(8)).thenReturn("1234567890");
        when(mockResultSet.getString(9)).thenReturn("Fever");
        when(mockResultSet.getInt(10)).thenReturn(5);
        when(mockResultSet.getString(11)).thenReturn("123 Main St");
        when(mockResultSet.getString(12)).thenReturn("Pending");

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals("Pending", result.get(0).getStatus());
    }

    @Test
    void getAllAppointmentByLoginUser_noResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(99);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointmentByLoginUser_sqlException_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getAllAppointmentByLoginDoctor ────────────────────────────────────────

    @Test
    void getAllAppointmentByLoginDoctor_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(2);
        when(mockResultSet.getInt(2)).thenReturn(20);
        when(mockResultSet.getString(3)).thenReturn("Jane Doe");
        when(mockResultSet.getString(4)).thenReturn("Female");
        when(mockResultSet.getString(5)).thenReturn("25");
        when(mockResultSet.getString(6)).thenReturn("2024-02-20");
        when(mockResultSet.getString(7)).thenReturn("jane@example.com");
        when(mockResultSet.getString(8)).thenReturn("9876543210");
        when(mockResultSet.getString(9)).thenReturn("Cold");
        when(mockResultSet.getInt(10)).thenReturn(3);
        when(mockResultSet.getString(11)).thenReturn("456 Oak Ave");
        when(mockResultSet.getString(12)).thenReturn("Confirmed");

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane Doe", result.get(0).getFullName());
    }

    @Test
    void getAllAppointmentByLoginDoctor_noResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(999);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointmentByLoginDoctor_sqlException_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(3);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getAppointmentById ───────────────────────────────────────────────────

    @Test
    void getAppointmentById_found_returnsAppointment() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(5);
        when(mockResultSet.getInt(2)).thenReturn(10);
        when(mockResultSet.getString(3)).thenReturn("Test Patient");
        when(mockResultSet.getString(4)).thenReturn("Male");
        when(mockResultSet.getString(5)).thenReturn("40");
        when(mockResultSet.getString(6)).thenReturn("2024-03-01");
        when(mockResultSet.getString(7)).thenReturn("test@test.com");
        when(mockResultSet.getString(8)).thenReturn("1111111111");
        when(mockResultSet.getString(9)).thenReturn("Headache");
        when(mockResultSet.getInt(10)).thenReturn(7);
        when(mockResultSet.getString(11)).thenReturn("789 Elm St");
        when(mockResultSet.getString(12)).thenReturn("Pending");

        Appointment result = appointmentDAO.getAppointmentById(5);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals("Test Patient", result.getFullName());
    }

    @Test
    void getAppointmentById_notFound_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Appointment result = appointmentDAO.getAppointmentById(999);

        assertNull(result);
    }

    @Test
    void getAppointmentById_sqlException_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Appointment result = appointmentDAO.getAppointmentById(1);

        assertNull(result);
    }

    // ── updateDrAppointmentCommentStatus ─────────────────────────────────────

    @Test
    void updateDrAppointmentCommentStatus_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.updateDrAppointmentCommentStatus(1, 5, "Reviewed");

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Reviewed");
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).setInt(3, 5);
    }

    @Test
    void updateDrAppointmentCommentStatus_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = appointmentDAO.updateDrAppointmentCommentStatus(1, 5, "Reviewed");

        assertFalse(result);
    }

    // ── getAllAppointment ─────────────────────────────────────────────────────

    @Test
    void getAllAppointment_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(1, 2);
        when(mockResultSet.getInt(2)).thenReturn(10, 20);
        when(mockResultSet.getString(3)).thenReturn("Patient A", "Patient B");
        when(mockResultSet.getString(4)).thenReturn("Male", "Female");
        when(mockResultSet.getString(5)).thenReturn("30", "25");
        when(mockResultSet.getString(6)).thenReturn("2024-01-01", "2024-01-02");
        when(mockResultSet.getString(7)).thenReturn("a@test.com", "b@test.com");
        when(mockResultSet.getString(8)).thenReturn("1111111111", "2222222222");
        when(mockResultSet.getString(9)).thenReturn("Fever", "Cold");
        when(mockResultSet.getInt(10)).thenReturn(5, 6);
        when(mockResultSet.getString(11)).thenReturn("Addr A", "Addr B");
        when(mockResultSet.getString(12)).thenReturn("Pending", "Confirmed");

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllAppointment_noResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointment_sqlException_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
