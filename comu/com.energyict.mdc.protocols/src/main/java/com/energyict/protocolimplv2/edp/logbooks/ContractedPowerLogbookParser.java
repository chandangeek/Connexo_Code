/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimplv2.edp.CX20009;

import java.util.HashMap;
import java.util.Map;

public class ContractedPowerLogbookParser extends AbstractLogbookParser {

    private static final ObisCode CONTRACTED_POWER_LOGBOOK = ObisCode.fromString("0.0.99.98.3.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Contracted power logbook cleared"));
        DESCRIPTIONS.put(96, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when a change is made to the values of contracted power"));
    }

    public ContractedPowerLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return CONTRACTED_POWER_LOGBOOK;
    }

    protected String getExtraDescription(Structure structure) {
        if (structure.nrOfDataTypes() > 2) {
            long newThreshold = structure.getDataType(2).longValue();
            long oldThreshold = structure.getDataType(3).longValue();
            return " (new threshold: " + newThreshold + " VA, old threshold: " + oldThreshold + " VA)";
        }
        return "";
    }
}