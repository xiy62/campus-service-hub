package com.campushub.requestservice.mapper;

import com.campushub.requestservice.dto.ServiceRequestCreateDTO;
import com.campushub.requestservice.dto.ServiceRequestResponseDTO;
import com.campushub.requestservice.model.ServiceRequest;
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
