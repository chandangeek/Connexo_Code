/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

public enum DeliverableType {
    BILLING("billing"),
    NUMERICAL("numerical"),
    TEXT("text"),
    FLAGS("flags");

    private String name;

    DeliverableType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
