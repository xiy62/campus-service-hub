package com.campushub.requestservice.service.impl;

import com.campushub.requestservice.dto.ServiceRequestCreateDTO;
import com.campushub.requestservice.dto.ServiceRequestResponseDTO;
import com.campushub.requestservice.grpc.BillingServiceGrpcClient;
import com.campushub.requestservice.kafka.KafkaProducer;
import com.campushub.requestservice.model.ServiceRequest;
import com.campushub.requestservice.model.RequestStatus;
import com.campushub.requestservice.repository.ServiceRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceImplTests {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private BillingServiceGrpcClient billingServiceGrpcClient;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private ServiceRequestServiceImpl serviceRequestService;

    @Test
    void shouldMarkRequestFailedWhenProvisioningThrows() {
        ServiceRequestCreateDTO requestDTO = new ServiceRequestCreateDTO();
        requestDTO.setRequestType("NETWORK_ACCESS");
        requestDTO.setStudentId("S1002001");
        requestDTO.setStudentEmail("failure.case@campus.edu");
        requestDTO.setDepartment("IT Services");
        requestDTO.setSubmittedAt("2026-04-03T12:30:00");

        when(serviceRequestRepository.existsByStudentEmail("failure.case@campus.edu")).thenReturn(false);

        when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest serviceRequest = invocation.getArgument(0);
            if (serviceRequest.getId() == null) {
                serviceRequest.setId(UUID.fromString("423e4567-e89b-12d3-a456-426614174111"));
            }
            return serviceRequest;
        });

        doThrow(new RuntimeException("provisioning unavailable"))
                .when(billingServiceGrpcClient)
                .createBillingAccount(any(String.class), any(String.class), any(String.class));

        ServiceRequestResponseDTO response = serviceRequestService.createServiceRequest(requestDTO);

        assertEquals("FAILED", response.getStatus());

        ArgumentCaptor<ServiceRequest> serviceRequestCaptor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository, times(3)).save(serviceRequestCaptor.capture());
        assertEquals(RequestStatus.FAILED, serviceRequestCaptor.getAllValues().get(2).getStatus());

        verify(kafkaProducer).sendEvent(any(ServiceRequest.class), eq("SERVICE_REQUEST_SUBMITTED"));
        verify(kafkaProducer).sendEvent(any(ServiceRequest.class), eq("SERVICE_REQUEST_PROVISIONING_STARTED"));
        verify(kafkaProducer).sendEvent(any(ServiceRequest.class), eq("SERVICE_REQUEST_PROVISIONING_FAILED"));
    }
}
