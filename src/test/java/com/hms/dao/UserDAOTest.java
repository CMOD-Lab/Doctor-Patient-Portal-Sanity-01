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

    // ── Constructor ──────────────────────────────────────────────────────────

    @Test
    void constructor_withConnection_createsInstance() {
        UserDAO dao = new UserDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── userRegister ─────────────────────────────────────────────────────────

    @Test
    void userRegister_success_returnsTrue() throws Exception {
        User user = new User("John Doe", "john@example.com", "password123");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, user.getFullName());
        verify(mockPreparedStatement).setString(2, user.getEmail());
        verify(mockPreparedStatement).setString(3, user.getPassword());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void userRegister_sqlException_returnsFalse() throws Exception {
        User user = new User("John Doe", "john@example.com", "password123");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.userRegister(user);

        assertFalse(result);
    }

    @Test
    void userRegister_withNullFields_callsPreparedStatement() throws Exception {
        User user = new User(null, null, null);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
    }

    // ── loginUser ────────────────────────────────────────────────────────────

    @Test
    void loginUser_validCredentials_returnsUser() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("John Doe");
        when(mockResultSet.getString("email")).thenReturn("john@example.com");
        when(mockResultSet.getString("password")).thenReturn("password123");

        User result = userDAO.loginUser("john@example.com", "password123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
    }

    @Test
    void loginUser_invalidCredentials_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = userDAO.loginUser("wrong@email.com", "wrongpass");

        assertNull(result);
    }

    @Test
    void loginUser_sqlException_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        User result = userDAO.loginUser("email@test.com", "pass");

        assertNull(result);
    }

    @Test
    void loginUser_setsAllUserFields() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(42);
        when(mockResultSet.getString("full_name")).thenReturn("Alice Smith");
        when(mockResultSet.getString("email")).thenReturn("alice@test.com");
        when(mockResultSet.getString("password")).thenReturn("alicepass");

        User result = userDAO.loginUser("alice@test.com", "alicepass");

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals("Alice Smith", result.getFullName());
        assertEquals("alice@test.com", result.getEmail());
        assertEquals("alicepass", result.getPassword());
    }

    // ── checkOldPassword ─────────────────────────────────────────────────────

    @Test
    void checkOldPassword_passwordMatches_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = userDAO.checkOldPassword(1, "correctPassword");

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setString(2, "correctPassword");
    }

    @Test
    void checkOldPassword_passwordDoesNotMatch_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = userDAO.checkOldPassword(1, "wrongPassword");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.checkOldPassword(1, "password");

        assertFalse(result);
    }

    // ── changePassword ───────────────────────────────────────────────────────

    @Test
    void changePassword_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(1, "newPassword");

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "newPassword");
        verify(mockPreparedStatement).setInt(2, 1);
    }

    @Test
    void changePassword_sqlException_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.changePassword(1, "newPassword");

        assertFalse(result);
    }

    @Test
    void changePassword_withEmptyPassword_callsPreparedStatement() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(5, "");

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "");
        verify(mockPreparedStatement).setInt(2, 5);
    }
}
