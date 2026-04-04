package com.pm.requestservice.dto;

import lombok.Data;

@Data
public class ServiceRequestResponseDTO {
    private String id;
    private String requestType;
    private String studentId;
    private String studentEmail;
    private String department;
    private String submittedAt;
    private String status;
}
