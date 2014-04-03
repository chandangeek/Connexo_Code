package com.elster.jupiter.systemadmin.rest.response;

public enum LicenseProperties {
    NAME("application.name"),
    TAG("application.tag"),
    TYPE("type"),
    DESCRIPTION("description"),
    EXPIRES("expires"),
    STATUS("status"),
    VALID_FROM("validfrom"),
    GRACEPERIOD("graceperiod"),
    VERSION("version"),
    PROPERTIES("content");

    private String name;

    LicenseProperties(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
