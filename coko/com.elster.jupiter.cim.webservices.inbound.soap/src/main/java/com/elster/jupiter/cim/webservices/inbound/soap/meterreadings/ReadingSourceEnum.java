/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

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
