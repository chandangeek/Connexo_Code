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

public class ClockSyncLogbookParser extends AbstractLogbookParser {

    private static final ObisCode CLOCK_SYNCHRONIZATION_LOGBOOK = ObisCode.fromString("0.0.99.98.8.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Clock synchronization logbook cleared"));
        DESCRIPTIONS.put(98, new EventInfo(MeterEvent.SETCLOCK, "Event registered when a synchronization of the real time clock occurs"));
    }

    public ClockSyncLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return CLOCK_SYNCHRONIZATION_LOGBOOK;
    }

    @Override
    protected String getExtraDescription(Structure structure) {
        if (structure.nrOfDataTypes() > 2) {
            return " (former clock: " + structure.getDataType(2).getOctetString().getDateTime(getProtocol().getTimeZone()).getValue().getTime().toString() + ")";
        }
        return "";
    }
}