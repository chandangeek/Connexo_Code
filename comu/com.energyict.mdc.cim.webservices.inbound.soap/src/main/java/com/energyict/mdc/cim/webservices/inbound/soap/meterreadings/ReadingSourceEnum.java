/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

public enum ReadingSourceEnum {

    METER("Meter"),
    HYBRID("Hybrid"),
    SYSTEM("System");

    private final String source;

    ReadingSourceEnum(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
