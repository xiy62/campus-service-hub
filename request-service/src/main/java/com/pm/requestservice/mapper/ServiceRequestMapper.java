package com.pm.requestservice.mapper;

import com.pm.requestservice.dto.ServiceRequestCreateDTO;
import com.pm.requestservice.dto.ServiceRequestResponseDTO;
import com.pm.requestservice.model.ServiceRequest;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

public class ServiceRequestMapper {
    public static ServiceRequestResponseDTO toDTO(ServiceRequest serviceRequest) {
        ServiceRequestResponseDTO serviceRequestResponseDTO = new ServiceRequestResponseDTO();
        BeanUtils.copyProperties(serviceRequest, serviceRequestResponseDTO);
        serviceRequestResponseDTO.setId(serviceRequest.getId().toString());
        serviceRequestResponseDTO.setSubmittedAt(serviceRequest.getSubmittedAt().toString());
        serviceRequestResponseDTO.setStatus(serviceRequest.getStatus().name());
        return serviceRequestResponseDTO;
    }

    public static ServiceRequest toModel(ServiceRequestCreateDTO serviceRequestCreateDTO) {
        ServiceRequest serviceRequest = new ServiceRequest();
        BeanUtils.copyProperties(serviceRequestCreateDTO, serviceRequest);
        serviceRequest.setSubmittedAt(LocalDateTime.parse(serviceRequestCreateDTO.getSubmittedAt()));
        return serviceRequest;
    }
}
