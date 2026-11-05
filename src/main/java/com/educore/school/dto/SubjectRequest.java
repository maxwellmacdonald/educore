package com.educore.school.dto;

import jakarta.validation.constraints.NotBlank;

public record SubjectRequest(
        @NotBlank(message = "Subject name is required") String name,
        @NotBlank(message = "Subject code is required") String code,
        @NotBlank(message = "Class name is required") String className,
        @NotBlank(message = "Teacher name is required") String teacherName) {
}
