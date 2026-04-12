package com.warmest.exception;

public final class ErrorMessages {

    public static final String INVALID_INPUT_TYPE_MISMATCH =
            "Invalid value '%s'";
    public static final String ILLEGAL_ARGUMENT =
            "%s";
    public static final String ENDPOINT_NOT_FOUND =
            "Endpoint not found: %s";
    public static final String METHOD_NOT_SUPPORTED =
            "Method %s not supported for this endpoint";
    public static final String UNEXPECTED_ERROR =
            "Unexpected error: %s";

    private ErrorMessages() {
    }
}