package com.elster.jupiter.metering.config;

public enum MetrologyConfigurationStatus {

    INACTIVE("inactive"),
    ACTIVE("active"),
    DEPRECATED("deprecated"),
    ;

    private String id;

    MetrologyConfigurationStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}


