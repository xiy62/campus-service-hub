package com.pm.requestservice.service;

import com.pm.requestservice.dto.ServiceRequestCreateDTO;
import com.pm.requestservice.dto.ServiceRequestResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ServiceRequestService {

    List<ServiceRequestResponseDTO> getServiceRequests();

    ServiceRequestResponseDTO createServiceRequest(ServiceRequestCreateDTO serviceRequestCreateDTO);

    ServiceRequestResponseDTO updateServiceRequest(UUID id, ServiceRequestCreateDTO serviceRequestCreateDTO);

    void deleteServiceRequest(UUID id);
}
