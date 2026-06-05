package com.appointment.system.util;

import com.appointment.system.entity.Appointment;
import com.appointment.system.entity.Availability;
import com.appointment.system.entity.TimeBlock;
import com.appointment.system.enums.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotCalculatorTest {

    private TimeSlotCalculator calculator;

    private static final LocalDate MONDAY = LocalDate.of(2025, 1, 6);

    @BeforeEach
    void setUp() {
        calculator = new TimeSlotCalculator();
    }

    private Availability availability(DayOfWeek day, LocalTime start, LocalTime end) {
        Availability availability = new Availability();
        availability.setDayOfWeek(day);
        availability.setStartTime(start);
        availability.setEndTime(end);
        return availability;
    }

    private Appointment bookedAppointment(LocalDateTime start, LocalDateTime end) {
        Appointment appointment = new Appointment();
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointment;
    }

    private Appointment cancelledAppointment(LocalDateTime start, LocalDateTime end) {
        Appointment appointment = new Appointment();
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointment;
    }

    private TimeBlock timeBlock(LocalDateTime start, LocalDateTime end) {
        TimeBlock timeBlock = new TimeBlock();
        timeBlock.setStartDateTime(start);
        timeBlock.setEndDateTime(end);
        return timeBlock;
    }

    @Test
    @DisplayName("Should return empty list when no availability is set")
    void noAvailability_returnsEmpty() {
        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(), List.of(), List.of(), 60, 0);
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when availability is for a different day")
    void wrongDay_returnsEmpty() {
        Availability avail = availability(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                60, 0);
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Should generate correct number of slots for a clean 2-hour window with 60-min appointments")
    void cleanWindow_correctSlotCount() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                60, 0);
        assertEquals(2, slots.size());
        assertEquals(LocalDateTime.of(MONDAY, LocalTime.of(9, 0)), slots.get(0));
        assertEquals(LocalDateTime.of(MONDAY, LocalTime.of(10, 0)), slots.get(1));
    }

    @Test
    @DisplayName("Should exclude slot that overlaps an existing confirmed appointment")
    void existingAppointment_slotExcluded() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        Appointment booked = bookedAppointment(LocalDateTime.of(MONDAY, LocalTime.of(9, 0)),
                LocalDateTime.of(MONDAY, LocalTime.of(10, 0)));
        List<LocalDateTime> slots = calculator.calculateAvailableSlots(
                MONDAY, List.of(avail), List.of(), List.of(booked), 60, 0);

        assertEquals(1, slots.size());
        assertEquals(LocalDateTime.of(MONDAY, LocalTime.of(10, 0)), slots.getFirst());
    }

    @Test
    @DisplayName("Should not exclude slot when overlapping appointment is cancelled")
    void cancelledAppointment_slotNotExcluded() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        Appointment cancelled = cancelledAppointment(LocalDateTime.of(MONDAY, LocalTime.of(9, 0)),
                LocalDateTime.of(MONDAY, LocalTime.of(10, 0)));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(),
                List.of(cancelled), 60, 0);

        assertEquals(2, slots.size());
    }

    @Test
    @DisplayName("Should exclude slot that falls within a time block")
    void timeBlock_slotExcluded() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
        TimeBlock block = timeBlock(LocalDateTime.of(MONDAY, LocalTime.of(10, 0)), LocalDateTime.of(MONDAY,
                LocalTime.of(11, 0)));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(block),
                List.of(), 60, 0);

        assertEquals(2, slots.size());
        assertTrue(slots.contains(LocalDateTime.of(MONDAY, LocalTime.of(9, 0))));
        assertTrue(slots.contains(LocalDateTime.of(MONDAY, LocalTime.of(11, 0))));
    }

    @Test
    @DisplayName("Should apply buffer minutes between appointments")
    void bufferMinutes_reducesAvailableSlots() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                60, 30);

        assertEquals(2, slots.size());
        assertEquals(LocalDateTime.of(MONDAY, LocalTime.of(9, 0)), slots.get(0));
        assertEquals(LocalDateTime.of(MONDAY, LocalTime.of(10, 30)), slots.get(1));
    }

    @Test
    @DisplayName("Should respect effectiveFrom date, no slots before effective date")
    void effectiveFrom_beforeDate_returnsEmpty() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        avail.setEffectiveFrom(MONDAY.plusDays(1));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                60, 0);

        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Should respect effectiveTo date — no slots after expiry")
    void effectiveTo_pastDate_returnsEmpty() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        avail.setEffectiveTo(MONDAY.minusDays(1));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                60, 0);

        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Should return slots when date is within effective range")
    void effectiveRange_withinRange_returnsSlots() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        avail.setEffectiveFrom(MONDAY.minusDays(7));
        avail.setEffectiveTo(MONDAY.plusDays(7));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                60, 0);

        assertEquals(2, slots.size());
    }

    @Test
    @DisplayName("Should handle appointment that has buffer extending past availability end")
    void bufferFromExistingAppointment_blocksOverlappingSlot() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
        Appointment booked = bookedAppointment(LocalDateTime.of(MONDAY, LocalTime.of(9, 0)), LocalDateTime.of(MONDAY,
                LocalTime.of(10, 0)));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(),
                List.of(booked), 60, 30);

        assertEquals(1, slots.size());
        assertEquals(LocalDateTime.of(MONDAY, LocalTime.of(10, 30)), slots.getFirst());
    }

    @Test
    @DisplayName("Should return empty when duration does not fit in availability window")
    void durationLargerThanWindow_returnsEmpty() {
        Availability avail = availability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));

        List<LocalDateTime> slots = calculator.calculateAvailableSlots(MONDAY, List.of(avail), List.of(), List.of(),
                90, 0);

        assertTrue(slots.isEmpty());
    }
}