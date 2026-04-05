package com.campushub.analytics.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import request.events.ServiceRequestEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private static final String SERVICE_REQUEST_TOPIC = "campus.service.requests";

    @KafkaListener(topics = SERVICE_REQUEST_TOPIC, groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            ServiceRequestEvent requestEvent = ServiceRequestEvent.parseFrom(event);

            log.info(
                    "Received Campus Service Request Event: [RequestId={}, RequestType={}, StudentId={}, StudentEmail={}, Department={}, SubmittedAt={}, Status={}, Type={}]",
                    requestEvent.getRequestId(),
                    requestEvent.getRequestType(),
                    requestEvent.getStudentId(),
                    requestEvent.getStudentEmail(),
                    requestEvent.getDepartment(),
                    requestEvent.getSubmittedAt(),
                    requestEvent.getStatus(),
                    requestEvent.getEventType()
            );
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event: {}", e.getMessage());
        }
    }
}
