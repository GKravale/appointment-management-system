package com.appointment.system.util;

import com.appointment.system.entity.Appointment;
import com.appointment.system.entity.Availability;
import com.appointment.system.entity.TimeBlock;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TimeSlotCalculator {

    public List<LocalDateTime> calculateAvailableSlots(LocalDate date, List<Availability> availabilities,
                                                       List<TimeBlock> timeBlocks,
                                                       List<Appointment> existingAppointments, int durationMinutes,
                                                       int bufferMinutes) {
        List<LocalDateTime> slots = new ArrayList<>();

        List<Availability> dayAvailability = availabilities.stream()
                .filter(a -> a.getDayOfWeek() == date.getDayOfWeek())
                .filter(a -> isEffective(a, date))
                .toList();

        if (dayAvailability.isEmpty()) return slots;

        int slotLength = durationMinutes + bufferMinutes;

        for (Availability availability : dayAvailability) {
            LocalDateTime current = LocalDateTime.of(date, availability.getStartTime());
            LocalDateTime windowEnd = LocalDateTime.of(date, availability.getEndTime());

            while (!current.plusMinutes(durationMinutes).isAfter(windowEnd)) {
                LocalDateTime slotEnd = current.plusMinutes(durationMinutes);

                if (!overlapsTimeBlock(current, slotEnd, timeBlocks, date) && !overlapsAppointment(current, slotEnd,
                        existingAppointments, bufferMinutes)) {
                    slots.add(current);
                }

                current = current.plusMinutes(slotLength > 0 ? slotLength : durationMinutes);
            }
        }

        return slots;
    }

    private boolean isEffective(Availability availability, LocalDate date) {
        if (availability.getEffectiveFrom() != null && date.isBefore(availability.getEffectiveFrom())) {
            return false;
        }
        return availability.getEffectiveTo() == null || !date.isAfter(availability.getEffectiveTo());
    }

    private boolean overlapsTimeBlock(LocalDateTime slotStart, LocalDateTime slotEnd, List<TimeBlock> timeBlocks,
                                      LocalDate date) {
        return timeBlocks.stream()
                .filter(tb -> tb.getStartDateTime().toLocalDate().equals(date) || tb.getEndDateTime().toLocalDate().equals(date))
                .anyMatch(tb -> slotStart.isBefore(tb.getEndDateTime()) && slotEnd.isAfter(tb.getStartDateTime()));
    }

    private boolean overlapsAppointment(LocalDateTime slotStart, LocalDateTime slotEnd,
                                        List<Appointment> appointments, int bufferMinutes) {
        return appointments.stream()
                .filter(a -> a.getStatus() != com.appointment.system.enums.AppointmentStatus.CANCELLED).anyMatch(a -> {
                    LocalDateTime appointmentEnd = a.getEndTime().plusMinutes(bufferMinutes);
                    return slotStart.isBefore(appointmentEnd) && slotEnd.isAfter(a.getStartTime());
                });
    }
}