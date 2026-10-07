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

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_shouldCreateDoctorWithDefaultValues() {
        Doctor d = new Doctor();
        assertNotNull(d);
        assertEquals(0, d.getId());
        assertNull(d.getFullName());
        assertNull(d.getDateOfBirth());
        assertNull(d.getQualification());
        assertNull(d.getSpecialist());
        assertNull(d.getEmail());
        assertNull(d.getPhone());
        assertNull(d.getPassword());
    }

    @Test
    void sevenArgConstructor_shouldSetAllFieldsExceptId() {
        Doctor d = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "9876543210", "pass123");
        assertEquals("Dr. Smith", d.getFullName());
        assertEquals("1980-05-15", d.getDateOfBirth());
        assertEquals("MBBS", d.getQualification());
        assertEquals("Cardiology", d.getSpecialist());
        assertEquals("smith@hospital.com", d.getEmail());
        assertEquals("9876543210", d.getPhone());
        assertEquals("pass123", d.getPassword());
        assertEquals(0, d.getId()); // id not set
    }

    @Test
    void eightArgConstructor_shouldSetAllFields() {
        Doctor d = new Doctor(1, "Dr. Jones", "1975-08-20", "MD", "Neurology",
                "jones@hospital.com", "1234567890", "secure");
        assertEquals(1, d.getId());
        assertEquals("Dr. Jones", d.getFullName());
        assertEquals("1975-08-20", d.getDateOfBirth());
        assertEquals("MD", d.getQualification());
        assertEquals("Neurology", d.getSpecialist());
        assertEquals("jones@hospital.com", d.getEmail());
        assertEquals("1234567890", d.getPhone());
        assertEquals("secure", d.getPassword());
    }

    @Test
    void eightArgConstructor_withNullValues_shouldStoreNulls() {
        Doctor d = new Doctor(1, null, null, null, null, null, null, null);
        assertEquals(1, d.getId());
        assertNull(d.getFullName());
        assertNull(d.getEmail());
        assertNull(d.getPassword());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_shouldReturnCorrectValue() {
        doctor.setId(10);
        assertEquals(10, doctor.getId());
    }

    @Test
    void setId_withZero_shouldReturnZero() {
        doctor.setId(0);
        assertEquals(0, doctor.getId());
    }

    @Test
    void setFullName_andGetFullName_shouldReturnCorrectValue() {
        doctor.setFullName("Dr. Brown");
        assertEquals("Dr. Brown", doctor.getFullName());
    }

    @Test
    void setFullName_withEmptyString_shouldReturnEmptyString() {
        doctor.setFullName("");
        assertEquals("", doctor.getFullName());
    }

    @Test
    void setDateOfBirth_andGetDateOfBirth_shouldReturnCorrectValue() {
        doctor.setDateOfBirth("1990-01-01");
        assertEquals("1990-01-01", doctor.getDateOfBirth());
    }

    @Test
    void setQualification_andGetQualification_shouldReturnCorrectValue() {
        doctor.setQualification("PhD");
        assertEquals("PhD", doctor.getQualification());
    }

    @Test
    void setSpecialist_andGetSpecialist_shouldReturnCorrectValue() {
        doctor.setSpecialist("Orthopedics");
        assertEquals("Orthopedics", doctor.getSpecialist());
    }

    @Test
    void setEmail_andGetEmail_shouldReturnCorrectValue() {
        doctor.setEmail("doctor@clinic.com");
        assertEquals("doctor@clinic.com", doctor.getEmail());
    }

    @Test
    void setEmail_withNull_shouldReturnNull() {
        doctor.setEmail(null);
        assertNull(doctor.getEmail());
    }

    @Test
    void setPhone_andGetPhone_shouldReturnCorrectValue() {
        doctor.setPhone("5559876543");
        assertEquals("5559876543", doctor.getPhone());
    }

    @Test
    void setPassword_andGetPassword_shouldReturnCorrectValue() {
        doctor.setPassword("newpassword");
        assertEquals("newpassword", doctor.getPassword());
    }

    @Test
    void setPassword_withEmptyString_shouldReturnEmptyString() {
        doctor.setPassword("");
        assertEquals("", doctor.getPassword());
    }

    @Test
    void setters_multipleUpdates_shouldReturnLastValue() {
        doctor.setFullName("First");
        doctor.setFullName("Second");
        assertEquals("Second", doctor.getFullName());
    }

    @Test
    void setId_negativeValue_shouldStoreNegativeValue() {
        doctor.setId(-3);
        assertEquals(-3, doctor.getId());
    }

    @Test
    void allSetters_shouldWorkIndependently() {
        doctor.setId(99);
        doctor.setFullName("Dr. Test");
        doctor.setDateOfBirth("2000-12-31");
        doctor.setQualification("MBBS");
        doctor.setSpecialist("General");
        doctor.setEmail("test@test.com");
        doctor.setPhone("0000000000");
        doctor.setPassword("testpass");

        assertEquals(99, doctor.getId());
        assertEquals("Dr. Test", doctor.getFullName());
        assertEquals("2000-12-31", doctor.getDateOfBirth());
        assertEquals("MBBS", doctor.getQualification());
        assertEquals("General", doctor.getSpecialist());
        assertEquals("test@test.com", doctor.getEmail());
        assertEquals("0000000000", doctor.getPhone());
        assertEquals("testpass", doctor.getPassword());
    }
}
