package com.pm.requestservice.controller;

import com.pm.requestservice.dto.ServiceRequestCreateDTO;
import com.pm.requestservice.dto.ServiceRequestResponseDTO;
import com.pm.requestservice.dto.validators.CreateServiceRequestValidationGroup;
import com.pm.requestservice.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/service-requests")
@RequiredArgsConstructor
@Tag(name = "Campus Service Requests", description = "API for managing campus service requests")
public class ServiceRequestController {
    private final ServiceRequestService serviceRequestService;

    @GetMapping
    @Operation(summary = "Get service requests")
    public ResponseEntity<List<ServiceRequestResponseDTO>> getServiceRequests() {
        List<ServiceRequestResponseDTO> serviceRequests = serviceRequestService.getServiceRequests();
        return ResponseEntity.ok().body(serviceRequests);
    }

    @PostMapping
    @Operation(summary = "Create a service request")
    public ResponseEntity<ServiceRequestResponseDTO> createServiceRequest(@Validated({Default.class, CreateServiceRequestValidationGroup.class}) @RequestBody ServiceRequestCreateDTO serviceRequestCreateDTO) {
        ServiceRequestResponseDTO serviceRequestResponse = serviceRequestService.createServiceRequest(serviceRequestCreateDTO);
        return ResponseEntity.ok().body(serviceRequestResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing service request")
    public ResponseEntity<ServiceRequestResponseDTO> updateServiceRequest(
            @Validated({Default.class}) @RequestBody ServiceRequestCreateDTO serviceRequestCreateDTO,
            @PathVariable UUID id) {
        ServiceRequestResponseDTO serviceRequestResponse = serviceRequestService.updateServiceRequest(id, serviceRequestCreateDTO);
        return ResponseEntity.ok().body(serviceRequestResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an existing service request")
    public ResponseEntity<Void> deleteServiceRequest(@PathVariable UUID id) {
        serviceRequestService.deleteServiceRequest(id);
        return ResponseEntity.noContent().build();
    }
}
