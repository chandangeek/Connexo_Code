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

public class FirmwareLogbookParser extends AbstractLogbookParser {

    private static final ObisCode FIRMWARE_LOGBOOK = ObisCode.fromString("0.0.99.98.4.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Firmware logbook cleared"));
        DESCRIPTIONS.put(97, new EventInfo(MeterEvent.FIRMWARE_ACTIVATED, "Event registered when occurs a application firmware update of the meter"));
        DESCRIPTIONS.put(119, new EventInfo(MeterEvent.FIRMWARE_ACTIVATED, "Event registered whenever a firmware upgrade occurs in the communications module"));
    }

    public FirmwareLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return FIRMWARE_LOGBOOK;
    }

    protected String getExtraDescription(Structure structure) {
        if (structure.nrOfDataTypes() > 2) {
            return " (active core firmware version: " + structure.getDataType(2).getOctetString().stringValue() + ")";
        }
        return "";
    }
}