package com.campushub.requestservice.dto;

import com.campushub.requestservice.dto.validators.CreateServiceRequestValidationGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServiceRequestCreateDTO {
    @NotBlank(message = "Request type is required")
    @Size(max = 100, message = "Request type cannot exceed 100 characters")
    private String requestType;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Student email is required")
    @Email(message = "Student email should be valid")
    @Size(max = 255, message = "Student email cannot exceed 255 characters")
    private String studentEmail;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(groups = CreateServiceRequestValidationGroup.class, message = "Submitted timestamp is required")
    private String submittedAt;
}
