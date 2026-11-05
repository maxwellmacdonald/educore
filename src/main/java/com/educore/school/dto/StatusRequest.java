package com.educore.school.dto;

import com.educore.school.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "Status is required") AttendanceStatus status) {
}
