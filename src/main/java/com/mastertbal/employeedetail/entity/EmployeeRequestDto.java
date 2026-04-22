package com.mastertbal.employeedetail.entity;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeRequestDto {

    private Long id;

    @NotBlank(message = "First name cannot be blank")
    @NotEmpty(message = "First name cannot be empty")
    @NotNull(message = "First name cannot be null")
    @Size(min = 3, max = 50, message = "First name cannot be less and  3 or greater than 50")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @NotEmpty(message = "Last name cannot be empty")
    @NotNull(message = "Last name cannot be null")
    @Size(min = 3, max = 50, message = "Last name cannot be less and  3 or greater than 50")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @NotEmpty(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be null")
    @Email
    private String email;

    @NotBlank(message = "Department cannot be blank")
    @NotEmpty(message = "Department cannot be empty")
    @NotNull(message = "Department cannot be null")
    @Size(max = 100, message = "Department name cannot be greater than 100")
    private String department;

    @NotNull(message = "Salary cannot be null")
    @DecimalMin("0.00")
    private BigDecimal salary;

    @NotNull(message = "Date of joining cannot be null")
    @PastOrPresent(message = "Date of joining cannot be in the future")
    private LocalDate dateOfJoining;

    @NotNull(message = "Active cannot be null")
    @NotBlank(message = "Active cannot be blank")
    @NotEmpty(message = "Active cannot be empty")
    private Boolean active;

    private LocalDate createdAt;

    private LocalDate updatedAt;
}
