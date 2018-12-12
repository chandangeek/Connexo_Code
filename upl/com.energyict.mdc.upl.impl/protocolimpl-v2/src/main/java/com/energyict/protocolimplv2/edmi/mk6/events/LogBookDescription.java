/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.events;

import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 27/02/2017 - 11:51
 */
public enum LogBookDescription {

    STANDARD_LOG("Event_Log Std", ObisCode.fromString("0.0.99.98.0.255"), "Standard") { // MV90 compatible log recording max 100 system, setup, billing, diagnostic and user events
        @Override
        public EventLogParser getCorrespondingEventLogParser() {
            return new StandardEventLogParser();
        }
    },
    SYSTEM_LOG("Event_Log 2 System", ObisCode.fromString("0.0.99.98.1.255"), "System") { // Records max 30 system and diagnostic events
        @Override
        public EventLogParser getCorrespondingEventLogParser() {
            return new StandardEventLogParser();
        }
    },
    SAG_SWELL_LOG("Sag-Swell", ObisCode.fromString("0.0.99.98.2.255"), "Sag/swell") { // Records max 600 sag/swell events
        @Override
        public EventLogParser getCorrespondingEventLogParser() {
            return new SagSwellEventLogParser();
        }
    },
    UNKNOWN("Unknown", ObisCode.fromString("0.0.0.0.0.0"), "Unknown") {
        @Override
        public EventLogParser getCorrespondingEventLogParser() {
            return new StandardEventLogParser();
        }
    };

    private final String extensionName;
    private final ObisCode obisCode;
    private final String description;

    LogBookDescription(String extensionName, ObisCode obisCode, String description) {
        this.extensionName = extensionName;
        this.obisCode = obisCode;
        this.description = description;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public String getDescription() {
        return description;
    }

    public abstract EventLogParser getCorrespondingEventLogParser();

    public static LogBookDescription fromObisCode(ObisCode obisCode) {
        for (LogBookDescription logBookDescription : values()) {
            if (logBookDescription.getObisCode().equals(obisCode)) {
                return logBookDescription;
            }
        }
        return UNKNOWN;
    }
}