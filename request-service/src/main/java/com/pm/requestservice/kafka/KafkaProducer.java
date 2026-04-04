package com.pm.requestservice.kafka;

import com.pm.requestservice.model.ServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import request.events.ServiceRequestEvent;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private static final String SERVICE_REQUEST_TOPIC = "campus.service.requests";
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendEvent(ServiceRequest serviceRequest, String eventType) {
        ServiceRequestEvent event = ServiceRequestEvent.newBuilder()
                .setRequestId(serviceRequest.getId().toString())
                .setRequestType(serviceRequest.getRequestType())
                .setStudentId(serviceRequest.getStudentId())
                .setStudentEmail(serviceRequest.getStudentEmail())
                .setDepartment(serviceRequest.getDepartment())
                .setSubmittedAt(serviceRequest.getSubmittedAt().toString())
                .setStatus(serviceRequest.getStatus().name())
                .setEventType(eventType)
                .build();

        try {
            kafkaTemplate.send(SERVICE_REQUEST_TOPIC, event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending service request event: {}", event, e);
        }
    }
}
