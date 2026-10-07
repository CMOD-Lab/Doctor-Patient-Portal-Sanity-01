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

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_shouldCreateAppointmentWithDefaultValues() {
        Appointment a = new Appointment();
        assertNotNull(a);
        assertEquals(0, a.getId());
        assertEquals(0, a.getUserId());
        assertNull(a.getFullName());
        assertNull(a.getGender());
        assertNull(a.getAge());
        assertNull(a.getAppointmentDate());
        assertNull(a.getEmail());
        assertNull(a.getPhone());
        assertNull(a.getDiseases());
        assertEquals(0, a.getDoctorId());
        assertNull(a.getAddress());
        assertNull(a.getStatus());
    }

    @Test
    void elevenArgConstructor_shouldSetAllFieldsExceptId() {
        Appointment a = new Appointment(1, "John", "Male", "30", "2024-01-15",
                "john@test.com", "1234567890", "Fever", 2, "123 Main St", "Pending");
        assertEquals(1, a.getUserId());
        assertEquals("John", a.getFullName());
        assertEquals("Male", a.getGender());
        assertEquals("30", a.getAge());
        assertEquals("2024-01-15", a.getAppointmentDate());
        assertEquals("john@test.com", a.getEmail());
        assertEquals("1234567890", a.getPhone());
        assertEquals("Fever", a.getDiseases());
        assertEquals(2, a.getDoctorId());
        assertEquals("123 Main St", a.getAddress());
        assertEquals("Pending", a.getStatus());
        assertEquals(0, a.getId()); // id not set
    }

    @Test
    void twelveArgConstructor_shouldSetAllFields() {
        Appointment a = new Appointment(10, 1, "Jane", "Female", "25", "2024-02-20",
                "jane@test.com", "9876543210", "Cold", 3, "456 Oak Ave", "Approved");
        assertEquals(10, a.getId());
        assertEquals(1, a.getUserId());
        assertEquals("Jane", a.getFullName());
        assertEquals("Female", a.getGender());
        assertEquals("25", a.getAge());
        assertEquals("2024-02-20", a.getAppointmentDate());
        assertEquals("jane@test.com", a.getEmail());
        assertEquals("9876543210", a.getPhone());
        assertEquals("Cold", a.getDiseases());
        assertEquals(3, a.getDoctorId());
        assertEquals("456 Oak Ave", a.getAddress());
        assertEquals("Approved", a.getStatus());
    }

    @Test
    void twelveArgConstructor_withNullValues_shouldStoreNulls() {
        Appointment a = new Appointment(1, 1, null, null, null, null, null, null, null, 1, null, null);
        assertNull(a.getFullName());
        assertNull(a.getGender());
        assertNull(a.getStatus());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setId_andGetId_shouldReturnCorrectValue() {
        appointment.setId(5);
        assertEquals(5, appointment.getId());
    }

    @Test
    void setUserId_andGetUserId_shouldReturnCorrectValue() {
        appointment.setUserId(3);
        assertEquals(3, appointment.getUserId());
    }

    @Test
    void setFullName_andGetFullName_shouldReturnCorrectValue() {
        appointment.setFullName("Alice Smith");
        assertEquals("Alice Smith", appointment.getFullName());
    }

    @Test
    void setGender_andGetGender_shouldReturnCorrectValue() {
        appointment.setGender("Female");
        assertEquals("Female", appointment.getGender());
    }

    @Test
    void setAge_andGetAge_shouldReturnCorrectValue() {
        appointment.setAge("28");
        assertEquals("28", appointment.getAge());
    }

    @Test
    void setAppointmentDate_andGetAppointmentDate_shouldReturnCorrectValue() {
        appointment.setAppointmentDate("2024-03-10");
        assertEquals("2024-03-10", appointment.getAppointmentDate());
    }

    @Test
    void setEmail_andGetEmail_shouldReturnCorrectValue() {
        appointment.setEmail("test@example.com");
        assertEquals("test@example.com", appointment.getEmail());
    }

    @Test
    void setPhone_andGetPhone_shouldReturnCorrectValue() {
        appointment.setPhone("5551234567");
        assertEquals("5551234567", appointment.getPhone());
    }

    @Test
    void setDiseases_andGetDiseases_shouldReturnCorrectValue() {
        appointment.setDiseases("Diabetes");
        assertEquals("Diabetes", appointment.getDiseases());
    }

    @Test
    void setDoctorId_andGetDoctorId_shouldReturnCorrectValue() {
        appointment.setDoctorId(7);
        assertEquals(7, appointment.getDoctorId());
    }

    @Test
    void setAddress_andGetAddress_shouldReturnCorrectValue() {
        appointment.setAddress("789 Pine Rd");
        assertEquals("789 Pine Rd", appointment.getAddress());
    }

    @Test
    void setStatus_andGetStatus_shouldReturnCorrectValue() {
        appointment.setStatus("Completed");
        assertEquals("Completed", appointment.getStatus());
    }

    @Test
    void setStatus_withPending_shouldReturnPending() {
        appointment.setStatus("Pending");
        assertEquals("Pending", appointment.getStatus());
    }

    @Test
    void setStatus_withNull_shouldReturnNull() {
        appointment.setStatus(null);
        assertNull(appointment.getStatus());
    }

    @Test
    void setters_multipleUpdates_shouldReturnLastValue() {
        appointment.setFullName("First");
        appointment.setFullName("Second");
        assertEquals("Second", appointment.getFullName());
    }

    @Test
    void setId_withNegativeValue_shouldStoreNegativeValue() {
        appointment.setId(-1);
        assertEquals(-1, appointment.getId());
    }
}
