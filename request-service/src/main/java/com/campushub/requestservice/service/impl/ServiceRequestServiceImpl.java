package com.campushub.requestservice.service.impl;

import com.campushub.requestservice.dto.ServiceRequestCreateDTO;
import com.campushub.requestservice.dto.ServiceRequestResponseDTO;
import com.campushub.requestservice.exception.custom.EmailAlreadyExistsException;
import com.campushub.requestservice.exception.custom.ServiceRequestNotFoundException;
import com.campushub.requestservice.grpc.BillingServiceGrpcClient;
import com.campushub.requestservice.kafka.KafkaProducer;
import com.campushub.requestservice.mapper.ServiceRequestMapper;
import com.campushub.requestservice.model.ServiceRequest;
import com.campushub.requestservice.model.RequestStatus;
import com.campushub.requestservice.repository.ServiceRequestRepository;
import com.campushub.requestservice.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    @Override
    public List<ServiceRequestResponseDTO> getServiceRequests() {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAll();
        List<ServiceRequestResponseDTO> serviceRequestResponseDTOs = serviceRequests.stream()
                .map(ServiceRequestMapper::toDTO).toList();

        return serviceRequestResponseDTOs;
    }

    @Override
    public ServiceRequestResponseDTO createServiceRequest(ServiceRequestCreateDTO serviceRequestCreateDTO) {
        if (serviceRequestRepository.existsByStudentEmail(serviceRequestCreateDTO.getStudentEmail())) {
            throw new EmailAlreadyExistsException(
                    "A service request already exists for this student email: " + serviceRequestCreateDTO.getStudentEmail()
            );
        }
        ServiceRequest serviceRequest = ServiceRequestMapper.toModel(serviceRequestCreateDTO);
        serviceRequest.setStatus(RequestStatus.SUBMITTED);
        serviceRequest = serviceRequestRepository.save(serviceRequest);
        kafkaProducer.sendEvent(serviceRequest, "SERVICE_REQUEST_SUBMITTED");

        try {
            serviceRequest.setStatus(RequestStatus.PROVISIONING_STARTED);
            serviceRequest = serviceRequestRepository.save(serviceRequest);
            kafkaProducer.sendEvent(serviceRequest, "SERVICE_REQUEST_PROVISIONING_STARTED");

            // gRPC call for downstream provisioning workflow
            billingServiceGrpcClient.createBillingAccount(
                    serviceRequest.getId().toString(),
                    serviceRequest.getRequestType(),
                    serviceRequest.getStudentEmail()
            );
            serviceRequest.setStatus(RequestStatus.PROVISIONED);
            serviceRequest = serviceRequestRepository.save(serviceRequest);
            kafkaProducer.sendEvent(serviceRequest, "SERVICE_REQUEST_PROVISIONED");
        } catch (Exception e) {
            serviceRequest.setStatus(RequestStatus.FAILED);
            serviceRequest = serviceRequestRepository.save(serviceRequest);
            kafkaProducer.sendEvent(serviceRequest, "SERVICE_REQUEST_PROVISIONING_FAILED");
        }

        return ServiceRequestMapper.toDTO(serviceRequest);
    }

    @Override
    public ServiceRequestResponseDTO updateServiceRequest(UUID id, ServiceRequestCreateDTO serviceRequestCreateDTO) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id).orElseThrow(
                () -> new ServiceRequestNotFoundException("Service request not found with ID: " + id)
        );
        if (serviceRequestRepository.existsByStudentEmailAndIdNot(serviceRequestCreateDTO.getStudentEmail(), id)) {
            throw new EmailAlreadyExistsException(
                    "A service request already exists for this student email: " + serviceRequestCreateDTO.getStudentEmail()
            );
        }
        serviceRequest.setRequestType(serviceRequestCreateDTO.getRequestType());
        serviceRequest.setDepartment(serviceRequestCreateDTO.getDepartment());
        serviceRequest.setStudentId(serviceRequestCreateDTO.getStudentId());
        serviceRequest.setStudentEmail(serviceRequestCreateDTO.getStudentEmail());

        ServiceRequest updatedServiceRequest = serviceRequestRepository.save(serviceRequest);
        return ServiceRequestMapper.toDTO(updatedServiceRequest);
    }

    @Override
    public void deleteServiceRequest(UUID id) {
        serviceRequestRepository.deleteById(id);
    }
}
