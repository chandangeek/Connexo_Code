/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.ReadingQualityType;

import java.util.stream.Stream;

public enum ProtocolReadingQualities {

    POWERDOWN("1.2.1001"),
    POWERUP("1.2.1002"),
    SHORTLONG("1.4.1002"),
    WATCHDOGRESET("1.1.4"),
    CONFIGURATIONCHANGE("1.4.6"),
    CORRUPTED("1.1.7"),
    OVERFLOW("1.4.1"),
    MISSING("1.5.259"),
    MODIFIED("1.7.0"),
    OTHER("1.4.1001"),
    REVERSERUN("1.3.4"),
    PHASEFAILURE("1.2.1003"),
    BADTIME("1.1.9"),
    DEVICE_ERROR("1.1.3"),
    BATTERY_LOW("1.1.1"),
    TEST("1.4.5");

    private final String cimCode;

    ProtocolReadingQualities(String cimCode) {
        this.cimCode = cimCode;
    }

    public String getCimCode() {
        return cimCode;
    }

    public ReadingQualityType getReadingQualityType() {
        return new ReadingQualityType(getCimCode());
    }
    private boolean hasCimCode(String cimCode){
        return this.cimCode.equals(cimCode);
    }

    public static ProtocolReadingQualities fromCimCode(final String cimCode) {
        return Stream.of(ProtocolReadingQualities.values()).filter(rq -> rq.getCimCode().equals(cimCode)).findAny().orElse(null);
    }
}