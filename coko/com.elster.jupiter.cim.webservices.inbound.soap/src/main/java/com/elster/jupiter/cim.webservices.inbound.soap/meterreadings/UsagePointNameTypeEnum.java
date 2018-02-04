/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

public enum UsagePointNameTypeEnum {

    PURPOSE("Purpose"),
    USAGE_POINT_NAME("UsagePointName");

    private final String nameType;

    UsagePointNameTypeEnum(String nameType) {
        this.nameType = nameType;
    }

    public String getNameType() {
        return nameType;
    }
}