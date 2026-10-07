package com.hms.dao;

import com.hms.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO(mockConnection);
    }

    // ── Constructor Test ───────────────────────────────────────────────────────

    @Test
    void constructor_shouldCreateUserDAOWithConnection() {
        UserDAO dao = new UserDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── userRegister Tests ─────────────────────────────────────────────────────

    @Test
    void userRegister_whenSuccessful_shouldReturnTrue() throws Exception {
        User user = new User("John Doe", "john@example.com", "secret");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setString(1, "John Doe");
        verify(mockPreparedStatement).setString(2, "john@example.com");
        verify(mockPreparedStatement).setString(3, "secret");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void userRegister_whenExceptionThrown_shouldReturnFalse() throws Exception {
        User user = new User("John Doe", "john@example.com", "secret");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = userDAO.userRegister(user);

        assertFalse(result);
    }

    @Test
    void userRegister_withNullFields_shouldAttemptInsert() throws Exception {
        User user = new User(null, null, null);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), isNull());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
    }

    // ── loginUser Tests ────────────────────────────────────────────────────────

    @Test
    void loginUser_whenCredentialsValid_shouldReturnUser() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("John Doe");
        when(mockResultSet.getString("email")).thenReturn("john@example.com");
        when(mockResultSet.getString("password")).thenReturn("secret");

        User result = userDAO.loginUser("john@example.com", "secret");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("secret", result.getPassword());
    }

    @Test
    void loginUser_whenCredentialsInvalid_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = userDAO.loginUser("wrong@email.com", "wrongpass");

        assertNull(result);
    }

    @Test
    void loginUser_whenExceptionThrown_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        User result = userDAO.loginUser("email@test.com", "pass");

        assertNull(result);
    }

    @Test
    void loginUser_withEmptyCredentials_shouldReturnNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = userDAO.loginUser("", "");

        assertNull(result);
    }

    // ── checkOldPassword Tests ─────────────────────────────────────────────────

    @Test
    void checkOldPassword_whenPasswordMatches_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = userDAO.checkOldPassword(1, "oldpass");

        assertTrue(result);
    }

    @Test
    void checkOldPassword_whenPasswordDoesNotMatch_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = userDAO.checkOldPassword(1, "wrongpass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = userDAO.checkOldPassword(1, "pass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_withZeroUserId_shouldAttemptQuery() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = userDAO.checkOldPassword(0, "pass");

        assertFalse(result);
    }

    // ── changePassword Tests ───────────────────────────────────────────────────

    @Test
    void changePassword_whenSuccessful_shouldReturnTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(1, "newpassword");

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "newpassword");
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void changePassword_whenExceptionThrown_shouldReturnFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB Error"));

        boolean result = userDAO.changePassword(1, "newpassword");

        assertFalse(result);
    }

    @Test
    void changePassword_withEmptyPassword_shouldAttemptUpdate() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(1, "");

        assertTrue(result);
    }
}
