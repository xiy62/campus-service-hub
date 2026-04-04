package com.pm.requestservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_request")
@Data
public class ServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String requestType;

    @NotNull
    @Column(unique = true)
    private String studentId;

    @NotNull
    @Email
    @Column(unique = true)
    private String studentEmail;

    @NotNull
    private String department;

    @NotNull
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;
}
