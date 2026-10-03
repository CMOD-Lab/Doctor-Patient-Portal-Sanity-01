package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecialistTest {

    private Specialist specialist;

    @BeforeEach
    void setUp() {
        specialist = new Specialist();
    }

    // ── Default constructor ──────────────────────────────────────────────────
    @Test
    void defaultConstructor_createsNonNullObject() {
        Specialist s = new Specialist();
        assertNotNull(s);
    }

    // ── Full constructor ─────────────────────────────────────────────────────
    @Test
    void fullConstructor_setsAllFields() {
        Specialist s = new Specialist(1, "Cardiology");
        assertEquals(1, s.getId());
        assertEquals("Cardiology", s.getSpecialistName());
    }

    @Test
    void fullConstructor_withDifferentValues_setsCorrectly() {
        Specialist s = new Specialist(99, "Neurology");
        assertEquals(99, s.getId());
        assertEquals("Neurology", s.getSpecialistName());
    }

    // ── Setters / Getters ────────────────────────────────────────────────────
    @Test
    void setAndGetId_returnsCorrectValue() {
        specialist.setId(5);
        assertEquals(5, specialist.getId());
    }

    @Test
    void setAndGetSpecialistName_returnsCorrectValue() {
        specialist.setSpecialistName("Orthopedics");
        assertEquals("Orthopedics", specialist.getSpecialistName());
    }

    @Test
    void setId_withZero_returnsZero() {
        specialist.setId(0);
        assertEquals(0, specialist.getId());
    }

    @Test
    void setId_withNegativeValue_returnsNegative() {
        specialist.setId(-1);
        assertEquals(-1, specialist.getId());
    }

    @Test
    void setSpecialistName_withNull_storesNull() {
        specialist.setSpecialistName(null);
        assertNull(specialist.getSpecialistName());
    }

    @Test
    void setSpecialistName_withEmptyString_storesEmpty() {
        specialist.setSpecialistName("");
        assertEquals("", specialist.getSpecialistName());
    }

    @Test
    void defaultConstructor_allFieldsAreDefault() {
        Specialist s = new Specialist();
        assertEquals(0, s.getId());
        assertNull(s.getSpecialistName());
    }

    @Test
    void setSpecialistName_withLongName_storesCorrectly() {
        String longName = "Gastroenterology and Hepatology Specialist";
        specialist.setSpecialistName(longName);
        assertEquals(longName, specialist.getSpecialistName());
    }

    @Test
    void setId_withLargeValue_returnsCorrectly() {
        specialist.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, specialist.getId());
    }
}
