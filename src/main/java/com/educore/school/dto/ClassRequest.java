package com.educore.school.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassRequest(
        @NotBlank(message = "Class name is required") String name,
        @NotBlank(message = "Level is required") String level,
        @NotBlank(message = "Class teacher is required") String classTeacher,
        @NotNull(message = "Capacity is required") @Min(value = 1, message = "Capacity must be at least 1") @Max(value = 200, message = "Capacity must be at most 200") Integer capacity) {
}
