package com.educore.school.dto;

import com.educore.school.model.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceRequest(
        @NotBlank(message = "Student is required") String studentId,
        @NotNull(message = "Date is required") LocalDate date,
        @NotNull(message = "Status is required") AttendanceStatus status) {
}
