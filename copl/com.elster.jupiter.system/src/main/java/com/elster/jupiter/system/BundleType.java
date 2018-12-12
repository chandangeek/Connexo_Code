/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

public enum BundleType {
    NOT_APPLICABLE("notApplicable"),
    APPLICATION_SPECIFIC("appSpecific"),
    THIRD_PARTY("thirdParty");

    private final String id;

    BundleType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
