/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

public enum MeterStatusSource {
    METER("Meter"),
    SYSTEM("System");

    private final String source;

    MeterStatusSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
