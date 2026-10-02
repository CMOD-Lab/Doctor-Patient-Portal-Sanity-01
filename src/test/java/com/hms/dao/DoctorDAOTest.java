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

    // ── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructor_withConnection_createsInstance() {
        DoctorDAO dao = new DoctorDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── registerDoctor ───────────────────────────────────────────────────────

    @Test
    void registerDoctor_success_returnsTrue() throws Exception {
        Doctor doctor = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "1112223333", "pass123");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.registerDoctor(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, doctor.getFullName());
        verify(mockPreparedStatement).setString(2, doctor.getDateOfBirth());
        verify(mockPreparedStatement).setString(3, doctor.getQualification());
        verify(mockPreparedStatement).setString(4, doctor.getSpecialist());
        verify(mockPreparedStatement).setString(5, doctor.getEmail());
        verify(mockPreparedStatement).setString(6, doctor.getPhone());
        verify(mockPreparedStatement).setString(7, doctor.getPassword());
    }

    @Test
    void registerDoctor_sqlException_returnsFalse() throws Exception {
        Doctor doctor = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "1112223333", "pass123");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.registerDoctor(doctor);

        assertFalse(result);
    }

    // ── getAllDoctor ─────────────────────────────────────────────────────────

    @Test
    void getAllDoctor_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("fullName")).thenReturn("Dr. Jones");
        when(mockResultSet.getString("dateOfBirth")).thenReturn("1975-08-20");
        when(mockResultSet.getString("qualification")).thenReturn("MD");
        when(mockResultSet.getString("specialist")).thenReturn("Neurology");
        when(mockResultSet.getString("email")).thenReturn("jones@hospital.com");
        when(mockResultSet.getString("phone")).thenReturn("4445556666");
        when(mockResultSet.getString("password")).thenReturn("secret");

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Jones", result.get(0).getFullName());
        assertEquals("Neurology", result.get(0).getSpecialist());
    }

    @Test
    void getAllDoctor_noResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDoctor_sqlException_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getDoctorById ────────────────────────────────────────────────────────

    @Test
    void getDoctorById_found_returnsDoctor() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(5);
        when(mockResultSet.getString("fullName")).thenReturn("Dr. Alice");
        when(mockResultSet.getString("dateOfBirth")).thenReturn("1985-03-10");
        when(mockResultSet.getString("qualification")).thenReturn("MBBS");
        when(mockResultSet.getString("specialist")).thenReturn("Pediatrics");
        when(mockResultSet.getString("email")).thenReturn("alice@hospital.com");
        when(mockResultSet.getString("phone")).thenReturn("7778889999");
        when(mockResultSet.getString("password")).thenReturn("pwd123");

        Doctor result = doctorDAO.getDoctorById(5);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals("Dr. Alice", result.getFullName());
    }

    @Test
    void getDoctorById_notFound_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Doctor result = doctorDAO.getDoctorById(999);

        assertNull(result);
    }

    @Test
    void getDoctorById_sqlException_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor result = doctorDAO.getDoctorById(1);

        assertNull(result);
    }

    // ── updateDoctor ─────────────────────────────────────────────────────────

    @Test
    void updateDoctor_success_returnsTrue() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-01-01", "MD", "Cardiology",
                "updated@hospital.com", "1234567890", "newpass");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.updateDoctor(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, doctor.getFullName());
        verify(mockPreparedStatement).setInt(8, doctor.getId());
    }

    @Test
    void updateDoctor_sqlException_returnsFalse() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-01-01", "MD", "Cardiology",
                "updated@hospital.com", "1234567890", "newpass");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.updateDoctor(doctor);

        assertFalse(result);
    }

    // ── deleteDoctorById ─────────────────────────────────────────────────────

    @Test
    void deleteDoctorById_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.deleteDoctorById(1);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
    }

    @Test
    void deleteDoctorById_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.deleteDoctorById(1);

        assertFalse(result);
    }

    // ── loginDoctor ──────────────────────────────────────────────────────────

    @Test
    void loginDoctor_validCredentials_returnsDoctor() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(3);
        when(mockResultSet.getString(2)).thenReturn("Dr. Bob");
        when(mockResultSet.getString(3)).thenReturn("1970-06-15");
        when(mockResultSet.getString(4)).thenReturn("MBBS");
        when(mockResultSet.getString(5)).thenReturn("Surgery");
        when(mockResultSet.getString(6)).thenReturn("bob@hospital.com");
        when(mockResultSet.getString(7)).thenReturn("5556667777");
        when(mockResultSet.getString(8)).thenReturn("bobpass");

        Doctor result = doctorDAO.loginDoctor("bob@hospital.com", "bobpass");

        assertNotNull(result);
        assertEquals("Dr. Bob", result.getFullName());
        assertEquals(3, result.getId());
    }

    @Test
    void loginDoctor_invalidCredentials_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Doctor result = doctorDAO.loginDoctor("wrong@email.com", "wrongpass");

        assertNull(result);
    }

    @Test
    void loginDoctor_sqlException_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor result = doctorDAO.loginDoctor("email@test.com", "pass");

        assertNull(result);
    }

    // ── countTotalDoctor ─────────────────────────────────────────────────────

    @Test
    void countTotalDoctor_withRows_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);

        int result = doctorDAO.countTotalDoctor();

        assertEquals(3, result);
    }

    @Test
    void countTotalDoctor_noRows_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalDoctor();

        assertEquals(0, result);
    }

    @Test
    void countTotalDoctor_sqlException_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalDoctor();

        assertEquals(0, result);
    }

    // ── countTotalAppointment ────────────────────────────────────────────────

    @Test
    void countTotalAppointment_withRows_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        int result = doctorDAO.countTotalAppointment();

        assertEquals(2, result);
    }

    @Test
    void countTotalAppointment_sqlException_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalAppointment();

        assertEquals(0, result);
    }

    // ── countTotalAppointmentByDoctorId ──────────────────────────────────────

    @Test
    void countTotalAppointmentByDoctorId_withRows_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        int result = doctorDAO.countTotalAppointmentByDoctorId(5);

        assertEquals(1, result);
        verify(mockPreparedStatement).setInt(1, 5);
    }

    @Test
    void countTotalAppointmentByDoctorId_sqlException_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalAppointmentByDoctorId(5);

        assertEquals(0, result);
    }

    // ── countTotalUser ───────────────────────────────────────────────────────

    @Test
    void countTotalUser_withRows_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, false);

        int result = doctorDAO.countTotalUser();

        assertEquals(4, result);
    }

    @Test
    void countTotalUser_sqlException_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalUser();

        assertEquals(0, result);
    }

    // ── countTotalSpecialist ─────────────────────────────────────────────────

    @Test
    void countTotalSpecialist_withRows_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(2, result);
    }

    @Test
    void countTotalSpecialist_sqlException_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(0, result);
    }

    // ── checkOldPassword ─────────────────────────────────────────────────────

    @Test
    void checkOldPassword_passwordMatches_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = doctorDAO.checkOldPassword(1, "correctPassword");

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setString(2, "correctPassword");
    }

    @Test
    void checkOldPassword_passwordDoesNotMatch_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = doctorDAO.checkOldPassword(1, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.checkOldPassword(1, "password");

        assertFalse(result);
    }

    // ── changePassword ───────────────────────────────────────────────────────

    @Test
    void changePassword_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.changePassword(1, "newPassword");

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "newPassword");
        verify(mockPreparedStatement).setInt(2, 1);
    }

    @Test
    void changePassword_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.changePassword(1, "newPassword");

        assertFalse(result);
    }

    // ── editDoctorProfile ────────────────────────────────────────────────────

    @Test
    void editDoctorProfile_success_returnsTrue() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-01-01", "MD", "Cardiology",
                "updated@hospital.com", "1234567890", "");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.editDoctorProfile(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, doctor.getFullName());
        verify(mockPreparedStatement).setInt(7, doctor.getId());
    }

    @Test
    void editDoctorProfile_sqlException_returnsFalse() throws Exception {
        Doctor doctor = new Doctor(1, "Dr. Updated", "1980-01-01", "MD", "Cardiology",
                "updated@hospital.com", "1234567890", "");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.editDoctorProfile(doctor);

        assertFalse(result);
    }
}
