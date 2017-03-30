/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

public enum FirmwareType {
    COMMUNICATION("communication", "Communication firmware"),
    METER("meter", "Meter firmware");

    private String type;
    private String description;

    FirmwareType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}