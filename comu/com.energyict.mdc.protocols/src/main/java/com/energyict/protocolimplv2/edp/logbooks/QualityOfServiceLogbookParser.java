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

public class QualityOfServiceLogbookParser extends AbstractLogbookParser {

    private static final ObisCode QUALITY_OF_SERVICE_LOGBOOK = ObisCode.fromString("0.0.99.98.9.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Quality of service logbook cleared"));
        DESCRIPTIONS.put(13, new EventInfo(MeterEvent.OTHER, "Event registered when after one or more periods with T minutes: the average supply voltage during a period restores above the limit of Un-Δ%"));
        DESCRIPTIONS.put(14, new EventInfo(MeterEvent.OTHER, "Event registered when the average supply voltage of phase L1 during a period restores in the range Un±Δ% after one or more periods with T minutes below the limit of Un-Δ%"));
        DESCRIPTIONS.put(15, new EventInfo(MeterEvent.OTHER, "Event registered when the average supply voltage of phase L2 during a period restores in the range Un±Δ% after one or more periods with T minutes below the limit of Un-Δ%"));
        DESCRIPTIONS.put(16, new EventInfo(MeterEvent.OTHER, "Event registered when the average supply voltage of phase L3 during a period restores in the range Un±Δ% after one or more periods with T minutes below the limit of Un-Δ%"));
        DESCRIPTIONS.put(17, new EventInfo(MeterEvent.OTHER, "Event registered when after one or more periods with T minutes: the average supply voltage during a period restores below the limit of Un+Δ%"));
        DESCRIPTIONS.put(18, new EventInfo(MeterEvent.OTHER, "Event registered when the average supply voltage of phase L1 during a period restores in the range Un±Δ% after one or more periods with T minutes above the limit of Un+Δ%"));
        DESCRIPTIONS.put(19, new EventInfo(MeterEvent.OTHER, "Event registered when the average supply voltage of phase L2 during a period restores in the range Un±Δ% after one or more periods with T minutes above the limit of Un+Δ%"));
        DESCRIPTIONS.put(20, new EventInfo(MeterEvent.OTHER, "Event registered when the average supply voltage of phase L2 during a period restores in the range Un±Δ% after one or more periods with T minutes above the limit of Un+Δ%"));
        DESCRIPTIONS.put(21, new EventInfo(MeterEvent.OTHER, "End of a long power failure when all phases are restored"));
        DESCRIPTIONS.put(22, new EventInfo(MeterEvent.OTHER, "Event registered at the end of a long power failure in the phase L1"));
        DESCRIPTIONS.put(23, new EventInfo(MeterEvent.OTHER, "Event registered at the end of a long power failure in the phase L2"));
        DESCRIPTIONS.put(24, new EventInfo(MeterEvent.OTHER, "Event registered at the end of a long power failure in the phase L3"));
    }

    public QualityOfServiceLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return QUALITY_OF_SERVICE_LOGBOOK;
    }

    @Override
    protected String getExtraDescription(Structure structure) {
        if (structure.nrOfDataTypes() > 2) {
            String eventTime = structure.getDataType(2).getOctetString().getDateTime(getProtocol().getTimeZone()).getValue().getTime().toString();
            return " (beginning of event: " + eventTime + ")";
        }
        return "";
    }
}