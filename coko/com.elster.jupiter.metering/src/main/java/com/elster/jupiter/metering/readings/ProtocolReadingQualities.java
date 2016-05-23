package com.elster.jupiter.metering.readings;

import com.elster.jupiter.metering.ReadingQualityType;

/**
 * Common known reading qualities that can be readout from a device, by a protocol.
 * These were previously known as interval flags.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/05/2016 - 17:08
 */
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

    private ProtocolReadingQualities(String cimCode) {
        this.cimCode = cimCode;
    }

    public String getCimCode() {
        return cimCode;
    }

    public ReadingQualityType getReadingQualityType() {
        return new ReadingQualityType(getCimCode());
    }

    public static ProtocolReadingQualities fromCimCode(String cimCode) {
        for (ProtocolReadingQualities protocolReadingQualities : values()) {
            if (protocolReadingQualities.getCimCode().equals(cimCode)) {
                return protocolReadingQualities;
            }
        }
        return null;
    }
}