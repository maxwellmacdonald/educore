package com.educore.school.dto;

import com.educore.school.model.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkAttendanceRequest(
        @NotNull(message = "Date is required") LocalDate date,
        @NotEmpty(message = "At least one entry is required") List<@Valid Entry> entries) {

    public record Entry(
            @NotBlank(message = "Student is required") String studentId,
            @NotNull(message = "Status is required") AttendanceStatus status) {
    }
}
