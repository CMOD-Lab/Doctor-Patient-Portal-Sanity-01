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

    @Test
    void defaultConstructor_idDefaultsToZero() {
        User u = new User();
        assertEquals(0, u.getId());
    }

    @Test
    void defaultConstructor_stringFieldsDefaultToNull() {
        User u = new User();
        assertNull(u.getFullName());
        assertNull(u.getEmail());
        assertNull(u.getPassword());
    }

    // ── Constructor with id ──────────────────────────────────────────────────

    @Test
    void constructorWithId_setsAllFields() {
        User u = new User(1, "John Doe", "john@example.com", "password123");
        assertEquals(1, u.getId());
        assertEquals("John Doe", u.getFullName());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("password123", u.getPassword());
    }

    // ── Constructor without id ───────────────────────────────────────────────

    @Test
    void constructorWithoutId_setsFieldsExceptId() {
        User u = new User("Jane Doe", "jane@example.com", "secret");
        assertEquals(0, u.getId());
        assertEquals("Jane Doe", u.getFullName());
        assertEquals("jane@example.com", u.getEmail());
        assertEquals("secret", u.getPassword());
    }

    // ── Setters and Getters ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_workCorrectly() {
        user.setId(99);
        assertEquals(99, user.getId());
    }

    @Test
    void setFullName_andGetFullName_workCorrectly() {
        user.setFullName("Alice");
        assertEquals("Alice", user.getFullName());
    }

    @Test
    void setEmail_andGetEmail_workCorrectly() {
        user.setEmail("alice@test.com");
        assertEquals("alice@test.com", user.getEmail());
    }

    @Test
    void setPassword_andGetPassword_workCorrectly() {
        user.setPassword("myPassword");
        assertEquals("myPassword", user.getPassword());
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    void toString_containsAllFields() {
        User u = new User(1, "Bob", "bob@test.com", "pass");
        String result = u.toString();
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Bob"));
        assertTrue(result.contains("bob@test.com"));
        assertTrue(result.contains("pass"));
    }

    @Test
    void toString_returnsExpectedFormat() {
        User u = new User(2, "Carol", "carol@test.com", "abc");
        String expected = "User [id=2, fullName=Carol, email=carol@test.com, password=abc]";
        assertEquals(expected, u.toString());
    }

    @Test
    void toString_withDefaultConstructor_containsZeroId() {
        User u = new User();
        String result = u.toString();
        assertTrue(result.contains("id=0"));
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

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
    void setId_withNegativeValue_storesNegative() {
        user.setId(-1);
        assertEquals(-1, user.getId());
    }

    @Test
    void setFullName_overwrite_updatesValue() {
        user.setFullName("First");
        user.setFullName("Second");
        assertEquals("Second", user.getFullName());
    }

    @Test
    void constructorWithId_withZeroId_setsIdToZero() {
        User u = new User(0, "Test", "test@test.com", "pwd");
        assertEquals(0, u.getId());
    }
}
