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
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Doctor doctor = new Doctor("Dr. Smith", "1980-05-15", "MBBS",
                "Cardiology", "smith@hospital.com", "9876543210", "pass123");

        boolean result = doctorDAO.registerDoctor(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void registerDoctor_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor doctor = new Doctor("Dr. Smith", "1980-05-15", "MBBS",
                "Cardiology", "smith@hospital.com", "9876543210", "pass123");

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
        when(mockResultSet.getString("fullName")).thenReturn("Dr. Smith");
        when(mockResultSet.getString("dateOfBirth")).thenReturn("1980-05-15");
        when(mockResultSet.getString("qualification")).thenReturn("MBBS");
        when(mockResultSet.getString("specialist")).thenReturn("Cardiology");
        when(mockResultSet.getString("email")).thenReturn("smith@hospital.com");
        when(mockResultSet.getString("phone")).thenReturn("9876543210");
        when(mockResultSet.getString("password")).thenReturn("pass123");

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).getFullName());
    }

    @Test
    void getAllDoctor_withNoResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDoctor_whenExceptionThrown_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Doctor> result = doctorDAO.getAllDoctor();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getDoctorById ────────────────────────────────────────────────────────
    @Test
    void getDoctorById_withValidId_returnsDoctor() throws Exception {
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
        when(mockResultSet.getString("password")).thenReturn("secret");

        Doctor result = doctorDAO.getDoctorById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Dr. Jones", result.getFullName());
    }

    @Test
    void getDoctorById_withNoResult_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Doctor result = doctorDAO.getDoctorById(999);

        assertNull(result);
    }

    @Test
    void getDoctorById_whenExceptionThrown_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor result = doctorDAO.getDoctorById(1);

        assertNull(result);
    }

    // ── updateDoctor ─────────────────────────────────────────────────────────
    @Test
    void updateDoctor_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Doctor doctor = new Doctor(1, "Dr. Smith Updated", "1980-05-15", "MBBS",
                "Cardiology", "smith@hospital.com", "9876543210", "newPass");

        boolean result = doctorDAO.updateDoctor(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void updateDoctor_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor doctor = new Doctor(1, "Dr. Smith", "1980-05-15", "MBBS",
                "Cardiology", "smith@hospital.com", "9876543210", "pass");

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
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void deleteDoctorById_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.deleteDoctorById(1);

        assertFalse(result);
    }

    // ── loginDoctor ──────────────────────────────────────────────────────────
    @Test
    void loginDoctor_withValidCredentials_returnsDoctor() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
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
    void loginDoctor_withInvalidCredentials_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Doctor result = doctorDAO.loginDoctor("wrong@email.com", "wrongPass");

        assertNull(result);
    }

    @Test
    void loginDoctor_whenExceptionThrown_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor result = doctorDAO.loginDoctor("smith@hospital.com", "pass123");

        assertNull(result);
    }

    // ── countTotalDoctor ─────────────────────────────────────────────────────
    @Test
    void countTotalDoctor_withResults_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);

        int result = doctorDAO.countTotalDoctor();

        assertEquals(3, result);
    }

    @Test
    void countTotalDoctor_withNoResults_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalDoctor();

        assertEquals(0, result);
    }

    @Test
    void countTotalDoctor_whenExceptionThrown_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalDoctor();

        assertEquals(0, result);
    }

    // ── countTotalAppointment ────────────────────────────────────────────────
    @Test
    void countTotalAppointment_withResults_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        int result = doctorDAO.countTotalAppointment();

        assertEquals(2, result);
    }

    @Test
    void countTotalAppointment_withNoResults_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalAppointment();

        assertEquals(0, result);
    }

    @Test
    void countTotalAppointment_whenExceptionThrown_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalAppointment();

        assertEquals(0, result);
    }

    // ── countTotalAppointmentByDoctorId ──────────────────────────────────────
    @Test
    void countTotalAppointmentByDoctorId_withResults_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        int result = doctorDAO.countTotalAppointmentByDoctorId(5);

        assertEquals(1, result);
    }

    @Test
    void countTotalAppointmentByDoctorId_withNoResults_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalAppointmentByDoctorId(999);

        assertEquals(0, result);
    }

    @Test
    void countTotalAppointmentByDoctorId_whenExceptionThrown_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalAppointmentByDoctorId(1);

        assertEquals(0, result);
    }

    // ── countTotalUser ───────────────────────────────────────────────────────
    @Test
    void countTotalUser_withResults_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, true, false);

        int result = doctorDAO.countTotalUser();

        assertEquals(4, result);
    }

    @Test
    void countTotalUser_withNoResults_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalUser();

        assertEquals(0, result);
    }

    @Test
    void countTotalUser_whenExceptionThrown_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalUser();

        assertEquals(0, result);
    }

    // ── countTotalSpecialist ─────────────────────────────────────────────────
    @Test
    void countTotalSpecialist_withResults_returnsCount() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(2, result);
    }

    @Test
    void countTotalSpecialist_withNoResults_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(0, result);
    }

    @Test
    void countTotalSpecialist_whenExceptionThrown_returnsZero() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        int result = doctorDAO.countTotalSpecialist();

        assertEquals(0, result);
    }

    // ── checkOldPassword ─────────────────────────────────────────────────────
    @Test
    void checkOldPassword_withMatchingPassword_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = doctorDAO.checkOldPassword(1, "oldPass");

        assertTrue(result);
    }

    @Test
    void checkOldPassword_withNonMatchingPassword_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = doctorDAO.checkOldPassword(1, "wrongPass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.checkOldPassword(1, "pass");

        assertFalse(result);
    }

    // ── changePassword ───────────────────────────────────────────────────────
    @Test
    void changePassword_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = doctorDAO.changePassword(1, "newPass");

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void changePassword_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = doctorDAO.changePassword(1, "newPass");

        assertFalse(result);
    }

    // ── editDoctorProfile ────────────────────────────────────────────────────
    @Test
    void editDoctorProfile_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Doctor doctor = new Doctor(1, "Dr. Smith Updated", "1980-05-15", "MBBS",
                "Cardiology", "smith@hospital.com", "9876543210", "");

        boolean result = doctorDAO.editDoctorProfile(doctor);

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void editDoctorProfile_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Doctor doctor = new Doctor(1, "Dr. Smith", "1980-05-15", "MBBS",
                "Cardiology", "smith@hospital.com", "9876543210", "");

        boolean result = doctorDAO.editDoctorProfile(doctor);

        assertFalse(result);
    }
}
