/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterreadings;

public enum UsagePointNameType {

    PURPOSE("Purpose"),
    USAGE_POINT_NAME("UsagePointName");

    private final String nameType;

    UsagePointNameType(String nameType) {
        this.nameType = nameType;
    }

    public String getNameType() {
        return nameType;
    }
}