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

    // ── Default constructor ──────────────────────────────────────────────────
    @Test
    void defaultConstructor_createsNonNullObject() {
        User u = new User();
        assertNotNull(u);
    }

    // ── Constructor without id ───────────────────────────────────────────────
    @Test
    void constructorWithoutId_setsAllFieldsExceptId() {
        User u = new User("Alice", "alice@example.com", "password123");
        assertEquals(0, u.getId());
        assertEquals("Alice", u.getFullName());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("password123", u.getPassword());
    }

    // ── Full constructor (with id) ───────────────────────────────────────────
    @Test
    void fullConstructor_setsAllFields() {
        User u = new User(1, "Bob", "bob@example.com", "securePass");
        assertEquals(1, u.getId());
        assertEquals("Bob", u.getFullName());
        assertEquals("bob@example.com", u.getEmail());
        assertEquals("securePass", u.getPassword());
    }

    // ── Setters / Getters ────────────────────────────────────────────────────
    @Test
    void setAndGetId_returnsCorrectValue() {
        user.setId(42);
        assertEquals(42, user.getId());
    }

    @Test
    void setAndGetFullName_returnsCorrectValue() {
        user.setFullName("Charlie Brown");
        assertEquals("Charlie Brown", user.getFullName());
    }

    @Test
    void setAndGetEmail_returnsCorrectValue() {
        user.setEmail("charlie@example.com");
        assertEquals("charlie@example.com", user.getEmail());
    }

    @Test
    void setAndGetPassword_returnsCorrectValue() {
        user.setPassword("mySecret");
        assertEquals("mySecret", user.getPassword());
    }

    // ── toString ─────────────────────────────────────────────────────────────
    @Test
    void toString_containsAllFields() {
        User u = new User(1, "Dave", "dave@example.com", "pass");
        String result = u.toString();
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Dave"));
        assertTrue(result.contains("dave@example.com"));
        assertTrue(result.contains("pass"));
    }

    @Test
    void toString_withDefaultConstructor_returnsNonNull() {
        String result = user.toString();
        assertNotNull(result);
    }

    // ── Null / edge cases ────────────────────────────────────────────────────
    @Test
    void setFullName_withNull_storesNull() {
        user.setFullName(null);
        assertNull(user.getFullName());
    }

    @Test
    void setEmail_withNull_storesNull() {
        user.setEmail(null);
        assertNull(user.getEmail());
    }

    @Test
    void setPassword_withEmptyString_storesEmpty() {
        user.setPassword("");
        assertEquals("", user.getPassword());
    }

    @Test
    void setId_withZero_returnsZero() {
        user.setId(0);
        assertEquals(0, user.getId());
    }

    @Test
    void setId_withNegativeValue_returnsNegative() {
        user.setId(-10);
        assertEquals(-10, user.getId());
    }

    @Test
    void defaultConstructor_allFieldsAreDefault() {
        User u = new User();
        assertEquals(0, u.getId());
        assertNull(u.getFullName());
        assertNull(u.getEmail());
        assertNull(u.getPassword());
    }

    @Test
    void toString_format_matchesExpected() {
        User u = new User(5, "Eve", "eve@test.com", "pwd");
        String result = u.toString();
        assertEquals("User [id=5, fullName=Eve, email=eve@test.com, password=pwd]", result);
    }
}
