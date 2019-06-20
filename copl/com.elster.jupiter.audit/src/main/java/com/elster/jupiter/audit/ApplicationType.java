/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

public enum ApplicationType {

    UNKNOWN_APPLICATION_KEY("UNKNOWN"),
    MDC_APPLICATION_KEY("MDC"),
    MDM_APPLICATION_KEY("INS");

    private final String applicationType;

    ApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getName(){return applicationType;}
}