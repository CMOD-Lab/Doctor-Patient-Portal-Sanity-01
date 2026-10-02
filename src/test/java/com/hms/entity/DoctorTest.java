package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
    }

    // ── Default constructor ──────────────────────────────────────────────────

    @Test
    void defaultConstructor_createsNonNullObject() {
        Doctor d = new Doctor();
        assertNotNull(d);
    }

    @Test
    void defaultConstructor_intFieldDefaultsToZero() {
        Doctor d = new Doctor();
        assertEquals(0, d.getId());
    }

    @Test
    void defaultConstructor_stringFieldsDefaultToNull() {
        Doctor d = new Doctor();
        assertNull(d.getFullName());
        assertNull(d.getDateOfBirth());
        assertNull(d.getQualification());
        assertNull(d.getSpecialist());
        assertNull(d.getEmail());
        assertNull(d.getPhone());
        assertNull(d.getPassword());
    }

    // ── Constructor without id ───────────────────────────────────────────────

    @Test
    void constructorWithoutId_setsAllFieldsExceptId() {
        Doctor d = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "1112223333", "pass123");

        assertEquals(0, d.getId());
        assertEquals("Dr. Smith", d.getFullName());
        assertEquals("1980-05-15", d.getDateOfBirth());
        assertEquals("MBBS", d.getQualification());
        assertEquals("Cardiology", d.getSpecialist());
        assertEquals("smith@hospital.com", d.getEmail());
        assertEquals("1112223333", d.getPhone());
        assertEquals("pass123", d.getPassword());
    }

    // ── Full constructor (with id) ───────────────────────────────────────────

    @Test
    void fullConstructor_setsAllFields() {
        Doctor d = new Doctor(10, "Dr. Jones", "1975-08-20", "MD", "Neurology",
                "jones@hospital.com", "4445556666", "secret");

        assertEquals(10, d.getId());
        assertEquals("Dr. Jones", d.getFullName());
        assertEquals("1975-08-20", d.getDateOfBirth());
        assertEquals("MD", d.getQualification());
        assertEquals("Neurology", d.getSpecialist());
        assertEquals("jones@hospital.com", d.getEmail());
        assertEquals("4445556666", d.getPhone());
        assertEquals("secret", d.getPassword());
    }

    // ── Setters and Getters ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_workCorrectly() {
        doctor.setId(5);
        assertEquals(5, doctor.getId());
    }

    @Test
    void setFullName_andGetFullName_workCorrectly() {
        doctor.setFullName("Dr. Alice");
        assertEquals("Dr. Alice", doctor.getFullName());
    }

    @Test
    void setDateOfBirth_andGetDateOfBirth_workCorrectly() {
        doctor.setDateOfBirth("1990-01-01");
        assertEquals("1990-01-01", doctor.getDateOfBirth());
    }

    @Test
    void setQualification_andGetQualification_workCorrectly() {
        doctor.setQualification("PhD");
        assertEquals("PhD", doctor.getQualification());
    }

    @Test
    void setSpecialist_andGetSpecialist_workCorrectly() {
        doctor.setSpecialist("Orthopedics");
        assertEquals("Orthopedics", doctor.getSpecialist());
    }

    @Test
    void setEmail_andGetEmail_workCorrectly() {
        doctor.setEmail("doctor@clinic.com");
        assertEquals("doctor@clinic.com", doctor.getEmail());
    }

    @Test
    void setPhone_andGetPhone_workCorrectly() {
        doctor.setPhone("9998887777");
        assertEquals("9998887777", doctor.getPhone());
    }

    @Test
    void setPassword_andGetPassword_workCorrectly() {
        doctor.setPassword("newPass456");
        assertEquals("newPass456", doctor.getPassword());
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void setFullName_withNull_storesNull() {
        doctor.setFullName(null);
        assertNull(doctor.getFullName());
    }

    @Test
    void setPassword_withEmptyString_storesEmpty() {
        doctor.setPassword("");
        assertEquals("", doctor.getPassword());
    }

    @Test
    void setId_withNegativeValue_storesNegative() {
        doctor.setId(-10);
        assertEquals(-10, doctor.getId());
    }

    @Test
    void setEmail_withSpecialCharacters_storesCorrectly() {
        doctor.setEmail("dr.test+alias@hospital.org");
        assertEquals("dr.test+alias@hospital.org", doctor.getEmail());
    }

    @Test
    void setSpecialist_overwrite_updatesValue() {
        doctor.setSpecialist("Cardiology");
        doctor.setSpecialist("Dermatology");
        assertEquals("Dermatology", doctor.getSpecialist());
    }

    @Test
    void fullConstructor_withZeroId_setsIdToZero() {
        Doctor d = new Doctor(0, "Dr. Zero", "2000-01-01", "BDS", "Dentistry",
                "zero@clinic.com", "0000000000", "pwd");
        assertEquals(0, d.getId());
    }
}
