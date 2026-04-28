package com.smartcampus.api.errors;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String linkType;
    private final String linkId;

    public LinkedResourceNotFoundException(String linkType, String linkId, String message) {
        super(message);
        this.linkType = linkType;
        this.linkId = linkId;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getLinkId() {
        return linkId;
    }
}
