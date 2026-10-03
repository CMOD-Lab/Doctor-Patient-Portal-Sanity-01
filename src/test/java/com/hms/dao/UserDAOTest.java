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
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        User user = new User("Alice", "alice@example.com", "password123");

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void userRegister_verifyParametersSet() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        User user = new User("Bob", "bob@example.com", "securePass");

        userDAO.userRegister(user);

        verify(mockPreparedStatement).setString(1, "Bob");
        verify(mockPreparedStatement).setString(2, "bob@example.com");
        verify(mockPreparedStatement).setString(3, "securePass");
    }

    @Test
    void userRegister_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        User user = new User("Alice", "alice@example.com", "password123");

        boolean result = userDAO.userRegister(user);

        assertFalse(result);
    }

    // ── loginUser ────────────────────────────────────────────────────────────
    @Test
    void loginUser_withValidCredentials_returnsUser() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("Alice");
        when(mockResultSet.getString("email")).thenReturn("alice@example.com");
        when(mockResultSet.getString("password")).thenReturn("password123");

        User result = userDAO.loginUser("alice@example.com", "password123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Alice", result.getFullName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
    }

    @Test
    void loginUser_withInvalidCredentials_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = userDAO.loginUser("wrong@email.com", "wrongPass");

        assertNull(result);
    }

    @Test
    void loginUser_whenExceptionThrown_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        User result = userDAO.loginUser("alice@example.com", "password123");

        assertNull(result);
    }

    @Test
    void loginUser_verifyParametersSet() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        userDAO.loginUser("test@example.com", "testPass");

        verify(mockPreparedStatement).setString(1, "test@example.com");
        verify(mockPreparedStatement).setString(2, "testPass");
    }

    // ── checkOldPassword ─────────────────────────────────────────────────────
    @Test
    void checkOldPassword_withMatchingPassword_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = userDAO.checkOldPassword(1, "oldPass");

        assertTrue(result);
    }

    @Test
    void checkOldPassword_withNonMatchingPassword_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = userDAO.checkOldPassword(1, "wrongPass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.checkOldPassword(1, "pass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_verifyParametersSet() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        userDAO.checkOldPassword(5, "myPassword");

        verify(mockPreparedStatement).setInt(1, 5);
        verify(mockPreparedStatement).setString(2, "myPassword");
    }

    // ── changePassword ───────────────────────────────────────────────────────
    @Test
    void changePassword_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(1, "newPass");

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void changePassword_verifyParametersSet() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        userDAO.changePassword(3, "updatedPass");

        verify(mockPreparedStatement).setString(1, "updatedPass");
        verify(mockPreparedStatement).setInt(2, 3);
    }

    @Test
    void changePassword_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.changePassword(1, "newPass");

        assertFalse(result);
    }
}
