package com.pm.requestservice.exception.custom;

public class ServiceRequestNotFoundException extends RuntimeException {
    public ServiceRequestNotFoundException(String message) {
        super(message);
    }
}
