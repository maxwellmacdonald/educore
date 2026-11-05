package com.educore.school.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentRequest(
        @NotBlank(message = "Admission number is required") @Size(max = 30, message = "Admission number must be at most 30 characters") String admissionNumber,
        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name must be at most 100 characters") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be a valid address") String email,
        @NotBlank(message = "Gender is required") @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female or Other") String gender,
        @NotBlank(message = "Class name is required") String className,
        @NotNull(message = "Age is required") @Min(value = 3, message = "Age must be at least 3") @Max(value = 25, message = "Age must be at most 25") Integer age,
        LocalDate registrationDate) {
}
