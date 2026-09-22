package com.bassstrings.database.exception;

/** Thrown by the service layer when a requested id doesn't exist; mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(String resourceName, String id) {
        return new ResourceNotFoundException(resourceName + " not found: " + id);
    }
}
