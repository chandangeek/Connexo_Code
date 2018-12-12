package com.energyict.protocolimplv2.edmi.mk10.events;

import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 27/02/2017 - 11:51
 */
public enum LogBookDescription {
    SYSTEM(0, ObisCode.fromString("0.0.99.98.0.255"), "System"),
    SETUP(1, ObisCode.fromString("0.0.99.98.1.255"), "Setup"),
    TAMPER(2, ObisCode.fromString("0.0.99.98.2.255"), "Tamper"),
    TRIG(3, ObisCode.fromString("0.0.99.98.3.255"), "Trigger"),
    DIAG(4, ObisCode.fromString("0.0.99.98.4.255"), "Diagnostic"),
    UNKNOWN(-1, ObisCode.fromString("0.0.0.0.0.0"), "Unknown");

    private final int eventLogCode;
    private final ObisCode obisCode;
    private final String description;

    LogBookDescription(int eventLogCode, ObisCode obisCode, String description) {
        this.eventLogCode = eventLogCode;
        this.obisCode = obisCode;
        this.description = description;
    }

    public int getEventLogCode() {
        return eventLogCode;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public String getDescription() {
        return description;
    }

    public static LogBookDescription fromObisCode(ObisCode obisCode) {
        for (LogBookDescription logBookDescription : values()) {
            if (logBookDescription.getObisCode().equals(obisCode)) {
                return logBookDescription;
            }
        }
        return UNKNOWN;
    }
}