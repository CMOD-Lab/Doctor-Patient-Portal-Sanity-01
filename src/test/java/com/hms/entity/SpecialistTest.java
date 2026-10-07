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

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_shouldCreateSpecialistWithDefaultValues() {
        Specialist s = new Specialist();
        assertNotNull(s);
        assertEquals(0, s.getId());
        assertNull(s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_shouldSetAllFields() {
        Specialist s = new Specialist(1, "Cardiology");
        assertEquals(1, s.getId());
        assertEquals("Cardiology", s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_withZeroId_shouldSetZeroId() {
        Specialist s = new Specialist(0, "Neurology");
        assertEquals(0, s.getId());
        assertEquals("Neurology", s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_withNullName_shouldSetNullName() {
        Specialist s = new Specialist(5, null);
        assertEquals(5, s.getId());
        assertNull(s.getSpecialistName());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_shouldReturnCorrectValue() {
        specialist.setId(10);
        assertEquals(10, specialist.getId());
    }

    @Test
    void setId_withNegativeValue_shouldStoreNegativeValue() {
        specialist.setId(-1);
        assertEquals(-1, specialist.getId());
    }

    @Test
    void setSpecialistName_andGetSpecialistName_shouldReturnCorrectValue() {
        specialist.setSpecialistName("Orthopedics");
        assertEquals("Orthopedics", specialist.getSpecialistName());
    }

    @Test
    void setSpecialistName_withEmptyString_shouldReturnEmptyString() {
        specialist.setSpecialistName("");
        assertEquals("", specialist.getSpecialistName());
    }

    @Test
    void setSpecialistName_withNull_shouldReturnNull() {
        specialist.setSpecialistName(null);
        assertNull(specialist.getSpecialistName());
    }

    @Test
    void setId_multipleUpdates_shouldReturnLastValue() {
        specialist.setId(1);
        specialist.setId(2);
        specialist.setId(3);
        assertEquals(3, specialist.getId());
    }

    @Test
    void setSpecialistName_multipleUpdates_shouldReturnLastValue() {
        specialist.setSpecialistName("Cardiology");
        specialist.setSpecialistName("Neurology");
        assertEquals("Neurology", specialist.getSpecialistName());
    }
}
