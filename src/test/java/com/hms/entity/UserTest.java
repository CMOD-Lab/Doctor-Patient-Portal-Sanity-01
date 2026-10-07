package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_shouldCreateUserWithDefaultValues() {
        User u = new User();
        assertNotNull(u);
        assertEquals(0, u.getId());
        assertNull(u.getFullName());
        assertNull(u.getEmail());
        assertNull(u.getPassword());
    }

    @Test
    void threeArgConstructor_shouldSetNameEmailPassword() {
        User u = new User("John Doe", "john@example.com", "secret");
        assertEquals("John Doe", u.getFullName());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("secret", u.getPassword());
        assertEquals(0, u.getId()); // id not set by this constructor
    }

    @Test
    void fourArgConstructor_shouldSetAllFields() {
        User u = new User(42, "Jane Doe", "jane@example.com", "pass123");
        assertEquals(42, u.getId());
        assertEquals("Jane Doe", u.getFullName());
        assertEquals("jane@example.com", u.getEmail());
        assertEquals("pass123", u.getPassword());
    }

    @Test
    void fourArgConstructor_withNullValues_shouldStoreNulls() {
        User u = new User(1, null, null, null);
        assertEquals(1, u.getId());
        assertNull(u.getFullName());
        assertNull(u.getEmail());
        assertNull(u.getPassword());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_shouldReturnCorrectValue() {
        user.setId(7);
        assertEquals(7, user.getId());
    }

    @Test
    void setId_withZero_shouldReturnZero() {
        user.setId(0);
        assertEquals(0, user.getId());
    }

    @Test
    void setFullName_andGetFullName_shouldReturnCorrectValue() {
        user.setFullName("Alice");
        assertEquals("Alice", user.getFullName());
    }

    @Test
    void setFullName_withEmptyString_shouldReturnEmptyString() {
        user.setFullName("");
        assertEquals("", user.getFullName());
    }

    @Test
    void setEmail_andGetEmail_shouldReturnCorrectValue() {
        user.setEmail("alice@test.com");
        assertEquals("alice@test.com", user.getEmail());
    }

    @Test
    void setEmail_withNull_shouldReturnNull() {
        user.setEmail(null);
        assertNull(user.getEmail());
    }

    @Test
    void setPassword_andGetPassword_shouldReturnCorrectValue() {
        user.setPassword("mypassword");
        assertEquals("mypassword", user.getPassword());
    }

    @Test
    void setPassword_withEmptyString_shouldReturnEmptyString() {
        user.setPassword("");
        assertEquals("", user.getPassword());
    }

    // ── toString Tests ─────────────────────────────────────────────────────────

    @Test
    void toString_shouldContainAllFields() {
        User u = new User(1, "Bob", "bob@test.com", "pwd");
        String result = u.toString();
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Bob"));
        assertTrue(result.contains("bob@test.com"));
        assertTrue(result.contains("pwd"));
    }

    @Test
    void toString_withDefaultUser_shouldNotThrowException() {
        User u = new User();
        assertDoesNotThrow(() -> u.toString());
    }

    @Test
    void toString_shouldStartWithUserPrefix() {
        User u = new User(1, "Test", "t@t.com", "p");
        assertTrue(u.toString().startsWith("User ["));
    }

    // ── Mutation Tests ─────────────────────────────────────────────────────────

    @Test
    void setters_multipleUpdates_shouldReturnLastValue() {
        user.setFullName("First");
        user.setFullName("Second");
        assertEquals("Second", user.getFullName());
    }

    @Test
    void setId_negativeValue_shouldStoreNegativeValue() {
        user.setId(-5);
        assertEquals(-5, user.getId());
    }
}
