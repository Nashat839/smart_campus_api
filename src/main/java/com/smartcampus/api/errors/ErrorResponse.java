package com.smartcampus.api.errors;

import java.time.Instant;

public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String detail;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String message) {
        this(status, defaultError(status), message, null);
    }

    public ErrorResponse(int status, String error, String message, String detail) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.message = message;
        this.detail = detail;
    }

    private static String defaultError(int status) {
        switch (status) {
            case 400: return "BAD_REQUEST";
            case 403: return "FORBIDDEN";
            case 404: return "NOT_FOUND";
            case 409: return "CONFLICT";
            case 422: return "UNPROCESSABLE_ENTITY";
            default: return "INTERNAL_SERVER_ERROR";
        }
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
