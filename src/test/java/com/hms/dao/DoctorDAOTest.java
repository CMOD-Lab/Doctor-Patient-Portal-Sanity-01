package com.hms.dao;

import com.hms.entity.Doctor;
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
class DoctorDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private DoctorDAO doctorDAO;

    @BeforeEach
    void setUp() {
        doctorDAO = new DoctorDAO(mockConnection);
    }

    // ── Constructor Test ───────────────────────────────────────────────────────

    @Test
    void constructor_shouldCreateDoctorDAOWithConnection() {
        DoctorDAO dao = new DoctorDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── registerDoctor Tests ───────────────────────────────────────────────────

    @Test
    void registerDoctor_whenSuccessful_shouldReturnTrue() throws Exception {
        Doctor doctor = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "9876543210", "pass123");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.registerDoctor(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void registerDoctor_whenExceptionThrown_shouldReturnFalse() throws Exception {
        Doctor doctor = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "9876543210", "pass123");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = doctorDAO.registerDoctor(doctor);

        assertFalse(result);
    }

    // ── getAllDoctor Tests ─────────────────────────────────────────────────────

    @Test
    void getAllDoctor_whenNoResults_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDoctor_whenResultsExist_shouldReturnList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("fullName")).thenReturn("Dr. Jones");
        when(mockResultSet.getString("dateOfBirth")).thenReturn("1975-08-20");
        when(mockResultSet.getString("qualification")).thenReturn("MD");
        when(mockResultSet.getString("specialist")).thenReturn("Neurology");
        when(mockResultSet.getString("email")).thenReturn("jones@hospital.com");
        when(mockResultSet.getString("phone")).thenReturn("1234567890");
        when(mockResultSet.getString("password")).thenReturn("secure");

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Jones", result.get(0).getFullName());
    }

    @Test
    void getAllDoctor_whenExceptionThrown_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getDoctorById Tests ────────────────────────────────────────────────────

    @Test
    void getDoctorById_whenFound_shouldReturnDoctor() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("fullName")).thenReturn("Dr. Brown");
        when(mockResultSet.getString("dateOfBirth")).thenReturn("1985-03-10");
        when(mockResultSet.getString("qualification")).thenReturn("MBBS");
        when(mockResultSet.getString("specialist")).thenReturn("Orthopedics");
        when(mockResultSet.getString("email")).thenReturn("brown@hospital.com");
        when(mockResultSet.getString("phone")).thenReturn("5551234567");
        when(mockResultSet.getString("password")).thenReturn("mypass");

        Doctor result = doctorDAO.getDoctorById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Dr. Brown", result.getFullName());
    }

    @Test
    void getDoctorById_whenNotFound_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Doctor result = doctorDAO.getDoctorById(999);

        assertNull(result);
    }

    @Test
    void getDoctorById_whenExceptionThrown_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        Doctor result = doctorDAO.getDoctorById(1);

        assertNull(result);
    }

    // ── updateDoctor Tests ─────────────────────────────────────────────────────

    @Test
    void updateDoctor_whenSuccessful_shouldReturnTrue() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-05-15", "MBBS", "Cardiology",
                "updated@hospital.com", "9876543210", "newpass");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.updateDoctor(doctor);

        assertTrue(result);
    }

    @Test
    void updateDoctor_whenExceptionThrown_shouldReturnFalse() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-05-15", "MBBS", "Cardiology",
                "updated@hospital.com", "9876543210", "newpass");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = doctorDAO.updateDoctor(doctor);

        assertFalse(result);
    }

    // ── deleteDoctorById Tests ─────────────────────────────────────────────────

    @Test
    void deleteDoctorById_whenSuccessful_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.deleteDoctorById(1);

        assertTrue(result);
    }

    @Test
    void deleteDoctorById_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = doctorDAO.deleteDoctorById(1);

        assertFalse(result);
    }

    // ── loginDoctor Tests ──────────────────────────────────────────────────────

    @Test
    void loginDoctor_whenCredentialsValid_shouldReturnDoctor() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getString(2)).thenReturn("Dr. Smith");
        when(mockResultSet.getString(3)).thenReturn("1980-05-15");
        when(mockResultSet.getString(4)).thenReturn("MBBS");
        when(mockResultSet.getString(5)).thenReturn("Cardiology");
        when(mockResultSet.getString(6)).thenReturn("smith@hospital.com");
        when(mockResultSet.getString(7)).thenReturn("9876543210");
        when(mockResultSet.getString(8)).thenReturn("pass123");

        Doctor result = doctorDAO.loginDoctor("smith@hospital.com", "pass123");

        assertNotNull(result);
        assertEquals("Dr. Smith", result.getFullName());
    }

    @Test
    void loginDoctor_whenCredentialsInvalid_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Doctor result = doctorDAO.loginDoctor("wrong@email.com", "wrongpass");

        assertNull(result);
    }

    @Test
    void loginDoctor_whenExceptionThrown_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        Doctor result = doctorDAO.loginDoctor("email@test.com", "pass");

        assertNull(result);
    }

    // ── countTotalDoctor Tests ─────────────────────────────────────────────────

    @Test
    void countTotalDoctor_whenDoctorsExist_shouldReturnCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);

        int result = doctorDAO.countTotalDoctor();

        assertEquals(3, result);
    }

    @Test
    void countTotalDoctor_whenNoDoctors_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalDoctor();

        assertEquals(0, result);
    }

    @Test
    void countTotalDoctor_whenExceptionThrown_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        int result = doctorDAO.countTotalDoctor();

        assertEquals(0, result);
    }

    // ── countTotalAppointment Tests ────────────────────────────────────────────

    @Test
    void countTotalAppointment_whenAppointmentsExist_shouldReturnCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        int result = doctorDAO.countTotalAppointment();

        assertEquals(2, result);
    }

    @Test
    void countTotalAppointment_whenNoAppointments_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalAppointment();

        assertEquals(0, result);
    }

    @Test
    void countTotalAppointment_whenExceptionThrown_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        int result = doctorDAO.countTotalAppointment();

        assertEquals(0, result);
    }

    // ── countTotalAppointmentByDoctorId Tests ─────────────────────────────────

    @Test
    void countTotalAppointmentByDoctorId_whenAppointmentsExist_shouldReturnCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, false);

        int result = doctorDAO.countTotalAppointmentByDoctorId(1);

        assertEquals(4, result);
    }

    @Test
    void countTotalAppointmentByDoctorId_whenNoAppointments_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalAppointmentByDoctorId(99);

        assertEquals(0, result);
    }

    @Test
    void countTotalAppointmentByDoctorId_whenExceptionThrown_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        int result = doctorDAO.countTotalAppointmentByDoctorId(1);

        assertEquals(0, result);
    }

    // ── countTotalUser Tests ───────────────────────────────────────────────────

    @Test
    void countTotalUser_whenUsersExist_shouldReturnCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        int result = doctorDAO.countTotalUser();

        assertEquals(2, result);
    }

    @Test
    void countTotalUser_whenNoUsers_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalUser();

        assertEquals(0, result);
    }

    @Test
    void countTotalUser_whenExceptionThrown_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        int result = doctorDAO.countTotalUser();

        assertEquals(0, result);
    }

    // ── countTotalSpecialist Tests ─────────────────────────────────────────────

    @Test
    void countTotalSpecialist_whenSpecialistsExist_shouldReturnCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(3, result);
    }

    @Test
    void countTotalSpecialist_whenNoSpecialists_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(0, result);
    }

    @Test
    void countTotalSpecialist_whenExceptionThrown_shouldReturnZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(0, result);
    }

    // ── checkOldPassword Tests ─────────────────────────────────────────────────

    @Test
    void checkOldPassword_whenPasswordMatches_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = doctorDAO.checkOldPassword(1, "oldpass");

        assertTrue(result);
    }

    @Test
    void checkOldPassword_whenPasswordDoesNotMatch_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = doctorDAO.checkOldPassword(1, "wrongpass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = doctorDAO.checkOldPassword(1, "pass");

        assertFalse(result);
    }

    // ── changePassword Tests ───────────────────────────────────────────────────

    @Test
    void changePassword_whenSuccessful_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.changePassword(1, "newpassword");

        assertTrue(result);
    }

    @Test
    void changePassword_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = doctorDAO.changePassword(1, "newpassword");

        assertFalse(result);
    }

    // ── editDoctorProfile Tests ────────────────────────────────────────────────

    @Test
    void editDoctorProfile_whenSuccessful_shouldReturnTrue() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-05-15", "MBBS", "Cardiology",
                "updated@hospital.com", "9876543210", "");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.editDoctorProfile(doctor);

        assertTrue(result);
    }

    @Test
    void editDoctorProfile_whenExceptionThrown_shouldReturnFalse() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-05-15", "MBBS", "Cardiology",
                "updated@hospital.com", "9876543210", "");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = doctorDAO.editDoctorProfile(doctor);

        assertFalse(result);
    }
}
