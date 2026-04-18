package com.ratelimiter.model;

public class RequestMetadata {
    private final String identifier;
    private final String criteriaType;

    public RequestMetadata(String identifier, String criteriaType) {
        this.identifier = identifier;
        this.criteriaType = criteriaType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getCriteriaType() {
        return criteriaType;
    }
}
