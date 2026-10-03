package com.hms.dao;

import com.hms.entity.Specialist;
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
class SpecialistDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private SpecialistDAO specialistDAO;

    @BeforeEach
    void setUp() {
        specialistDAO = new SpecialistDAO(mockConnection);
    }

    // ── Constructor ──────────────────────────────────────────────────────────
    @Test
    void constructor_withConnection_createsInstance() {
        SpecialistDAO dao = new SpecialistDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── addSpecialist ────────────────────────────────────────────────────────
    @Test
    void addSpecialist_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = specialistDAO.addSpecialist("Cardiology");

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Cardiology");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addSpecialist_withDifferentName_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = specialistDAO.addSpecialist("Neurology");

        assertTrue(result);
    }

    @Test
    void addSpecialist_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = specialistDAO.addSpecialist("Cardiology");

        assertFalse(result);
    }

    @Test
    void addSpecialist_withEmptyString_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = specialistDAO.addSpecialist("");

        assertTrue(result);
    }

    // ── getAllSpecialist ─────────────────────────────────────────────────────
    @Test
    void getAllSpecialist_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getString(2)).thenReturn("Cardiology");

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Cardiology", result.get(0).getSpecialistName());
    }

    @Test
    void getAllSpecialist_withMultipleResults_returnsAllSpecialists() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(1, 2, 3);
        when(mockResultSet.getString(2)).thenReturn("Cardiology", "Neurology", "Orthopedics");

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Cardiology", result.get(0).getSpecialistName());
        assertEquals("Neurology", result.get(1).getSpecialistName());
        assertEquals("Orthopedics", result.get(2).getSpecialistName());
    }

    @Test
    void getAllSpecialist_withNoResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSpecialist_whenExceptionThrown_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSpecialist_verifyQueryExecuted() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        specialistDAO.getAllSpecialist();

        verify(mockPreparedStatement).executeQuery();
    }
}
