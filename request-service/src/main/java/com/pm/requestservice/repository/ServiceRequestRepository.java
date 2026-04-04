package com.pm.requestservice.repository;

import com.pm.requestservice.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {

    boolean existsByStudentEmail(String studentEmail);

    boolean existsByStudentEmailAndIdNot(String studentEmail, UUID id);
}
