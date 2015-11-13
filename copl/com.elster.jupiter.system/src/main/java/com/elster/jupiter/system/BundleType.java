package com.elster.jupiter.system;

public enum BundleType {
    APPLICATION_SPECIFIC("appSpecific"),
    THIRDPARTY("thirdParty");

    private final String id;

    BundleType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
