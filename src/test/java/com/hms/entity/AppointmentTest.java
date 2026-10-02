package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
    }

    // ── Default constructor ──────────────────────────────────────────────────

    @Test
    void defaultConstructor_createsNonNullObject() {
        Appointment a = new Appointment();
        assertNotNull(a);
    }

    @Test
    void defaultConstructor_intFieldsDefaultToZero() {
        Appointment a = new Appointment();
        assertEquals(0, a.getId());
        assertEquals(0, a.getUserId());
        assertEquals(0, a.getDoctorId());
    }

    @Test
    void defaultConstructor_stringFieldsDefaultToNull() {
        Appointment a = new Appointment();
        assertNull(a.getFullName());
        assertNull(a.getGender());
        assertNull(a.getAge());
        assertNull(a.getAppointmentDate());
        assertNull(a.getEmail());
        assertNull(a.getPhone());
        assertNull(a.getDiseases());
        assertNull(a.getAddress());
        assertNull(a.getStatus());
    }

    // ── Full constructor (with id) ───────────────────────────────────────────

    @Test
    void fullConstructor_setsAllFields() {
        Appointment a = new Appointment(1, 10, "John Doe", "Male", "30",
                "2024-01-15", "john@example.com", "1234567890",
                "Fever", 5, "123 Main St", "Pending");

        assertEquals(1, a.getId());
        assertEquals(10, a.getUserId());
        assertEquals("John Doe", a.getFullName());
        assertEquals("Male", a.getGender());
        assertEquals("30", a.getAge());
        assertEquals("2024-01-15", a.getAppointmentDate());
        assertEquals("john@example.com", a.getEmail());
        assertEquals("1234567890", a.getPhone());
        assertEquals("Fever", a.getDiseases());
        assertEquals(5, a.getDoctorId());
        assertEquals("123 Main St", a.getAddress());
        assertEquals("Pending", a.getStatus());
    }

    // ── Constructor without id ───────────────────────────────────────────────

    @Test
    void constructorWithoutId_setsAllFieldsExceptId() {
        Appointment a = new Appointment(10, "Jane Doe", "Female", "25",
                "2024-02-20", "jane@example.com", "9876543210",
                "Cold", 3, "456 Oak Ave", "Confirmed");

        assertEquals(0, a.getId());
        assertEquals(10, a.getUserId());
        assertEquals("Jane Doe", a.getFullName());
        assertEquals("Female", a.getGender());
        assertEquals("25", a.getAge());
        assertEquals("2024-02-20", a.getAppointmentDate());
        assertEquals("jane@example.com", a.getEmail());
        assertEquals("9876543210", a.getPhone());
        assertEquals("Cold", a.getDiseases());
        assertEquals(3, a.getDoctorId());
        assertEquals("456 Oak Ave", a.getAddress());
        assertEquals("Confirmed", a.getStatus());
    }

    // ── Setters and Getters ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_workCorrectly() {
        appointment.setId(42);
        assertEquals(42, appointment.getId());
    }

    @Test
    void setUserId_andGetUserId_workCorrectly() {
        appointment.setUserId(7);
        assertEquals(7, appointment.getUserId());
    }

    @Test
    void setFullName_andGetFullName_workCorrectly() {
        appointment.setFullName("Alice Smith");
        assertEquals("Alice Smith", appointment.getFullName());
    }

    @Test
    void setGender_andGetGender_workCorrectly() {
        appointment.setGender("Female");
        assertEquals("Female", appointment.getGender());
    }

    @Test
    void setAge_andGetAge_workCorrectly() {
        appointment.setAge("28");
        assertEquals("28", appointment.getAge());
    }

    @Test
    void setAppointmentDate_andGetAppointmentDate_workCorrectly() {
        appointment.setAppointmentDate("2024-03-10");
        assertEquals("2024-03-10", appointment.getAppointmentDate());
    }

    @Test
    void setEmail_andGetEmail_workCorrectly() {
        appointment.setEmail("alice@example.com");
        assertEquals("alice@example.com", appointment.getEmail());
    }

    @Test
    void setPhone_andGetPhone_workCorrectly() {
        appointment.setPhone("5551234567");
        assertEquals("5551234567", appointment.getPhone());
    }

    @Test
    void setDiseases_andGetDiseases_workCorrectly() {
        appointment.setDiseases("Diabetes");
        assertEquals("Diabetes", appointment.getDiseases());
    }

    @Test
    void setDoctorId_andGetDoctorId_workCorrectly() {
        appointment.setDoctorId(99);
        assertEquals(99, appointment.getDoctorId());
    }

    @Test
    void setAddress_andGetAddress_workCorrectly() {
        appointment.setAddress("789 Pine Rd");
        assertEquals("789 Pine Rd", appointment.getAddress());
    }

    @Test
    void setStatus_andGetStatus_workCorrectly() {
        appointment.setStatus("Completed");
        assertEquals("Completed", appointment.getStatus());
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void setFullName_withNull_storesNull() {
        appointment.setFullName(null);
        assertNull(appointment.getFullName());
    }

    @Test
    void setStatus_withEmptyString_storesEmpty() {
        appointment.setStatus("");
        assertEquals("", appointment.getStatus());
    }

    @Test
    void setId_withNegativeValue_storesNegative() {
        appointment.setId(-1);
        assertEquals(-1, appointment.getId());
    }

    @Test
    void setDoctorId_withZero_storesZero() {
        appointment.setDoctorId(0);
        assertEquals(0, appointment.getDoctorId());
    }

    @Test
    void multipleSettersOnSameObject_overwritePreviousValues() {
        appointment.setFullName("First Name");
        appointment.setFullName("Second Name");
        assertEquals("Second Name", appointment.getFullName());
    }
}
