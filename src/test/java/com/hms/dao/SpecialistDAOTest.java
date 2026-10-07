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

    // ── Constructor Test ───────────────────────────────────────────────────────

    @Test
    void constructor_shouldCreateSpecialistDAOWithConnection() {
        SpecialistDAO dao = new SpecialistDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── addSpecialist Tests ────────────────────────────────────────────────────

    @Test
    void addSpecialist_whenSuccessful_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = specialistDAO.addSpecialist("Cardiology");

        assertTrue(result);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setString(1, "Cardiology");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addSpecialist_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = specialistDAO.addSpecialist("Cardiology");

        assertFalse(result);
    }

    @Test
    void addSpecialist_withEmptyString_shouldAttemptInsert() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = specialistDAO.addSpecialist("");

        assertTrue(result);
    }

    @Test
    void addSpecialist_withNullValue_shouldAttemptInsert() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), isNull());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = specialistDAO.addSpecialist(null);

        assertTrue(result);
    }

    // ── getAllSpecialist Tests ─────────────────────────────────────────────────

    @Test
    void getAllSpecialist_whenNoResults_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSpecialist_whenOneResult_shouldReturnListWithOneItem() throws Exception {
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
    void getAllSpecialist_whenMultipleResults_shouldReturnAllItems() throws Exception {
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
    void getAllSpecialist_whenExceptionThrown_shouldReturnEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllSpecialist_shouldReturnListNotNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Specialist> result = specialistDAO.getAllSpecialist();

        assertNotNull(result);
    }
}
