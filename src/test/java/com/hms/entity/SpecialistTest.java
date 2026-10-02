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

    @Test
    void defaultConstructor_idDefaultsToZero() {
        Specialist s = new Specialist();
        assertEquals(0, s.getId());
    }

    @Test
    void defaultConstructor_specialistNameDefaultsToNull() {
        Specialist s = new Specialist();
        assertNull(s.getSpecialistName());
    }

    // ── Parameterized constructor ────────────────────────────────────────────

    @Test
    void parameterizedConstructor_setsIdAndName() {
        Specialist s = new Specialist(1, "Cardiology");
        assertEquals(1, s.getId());
        assertEquals("Cardiology", s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_withZeroId_setsIdToZero() {
        Specialist s = new Specialist(0, "General");
        assertEquals(0, s.getId());
        assertEquals("General", s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_withNullName_storesNull() {
        Specialist s = new Specialist(5, null);
        assertEquals(5, s.getId());
        assertNull(s.getSpecialistName());
    }

    // ── Setters and Getters ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_workCorrectly() {
        specialist.setId(10);
        assertEquals(10, specialist.getId());
    }

    @Test
    void setSpecialistName_andGetSpecialistName_workCorrectly() {
        specialist.setSpecialistName("Neurology");
        assertEquals("Neurology", specialist.getSpecialistName());
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void setId_withNegativeValue_storesNegative() {
        specialist.setId(-5);
        assertEquals(-5, specialist.getId());
    }

    @Test
    void setSpecialistName_withEmptyString_storesEmpty() {
        specialist.setSpecialistName("");
        assertEquals("", specialist.getSpecialistName());
    }

    @Test
    void setSpecialistName_withNull_storesNull() {
        specialist.setSpecialistName(null);
        assertNull(specialist.getSpecialistName());
    }

    @Test
    void setSpecialistName_overwrite_updatesValue() {
        specialist.setSpecialistName("Orthopedics");
        specialist.setSpecialistName("Dermatology");
        assertEquals("Dermatology", specialist.getSpecialistName());
    }

    @Test
    void setId_overwrite_updatesValue() {
        specialist.setId(1);
        specialist.setId(2);
        assertEquals(2, specialist.getId());
    }

    @Test
    void parameterizedConstructor_withLargeId_setsCorrectly() {
        Specialist s = new Specialist(Integer.MAX_VALUE, "Oncology");
        assertEquals(Integer.MAX_VALUE, s.getId());
        assertEquals("Oncology", s.getSpecialistName());
    }

    @Test
    void setSpecialistName_withSpecialCharacters_storesCorrectly() {
        specialist.setSpecialistName("ENT (Ear, Nose & Throat)");
        assertEquals("ENT (Ear, Nose & Throat)", specialist.getSpecialistName());
    }
}
