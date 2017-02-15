/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimplv2.edp.CX20009;

import java.util.HashMap;
import java.util.Map;

public class CommunicationLogbookParser extends AbstractLogbookParser {

    private static final ObisCode COMMUNICATION_LOGBOOK = ObisCode.fromString("0.0.99.98.7.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Communication logbook cleared"));
        DESCRIPTIONS.put(1, new EventInfo(MeterEvent.OTHER, "Registers the beginning of a remote communication"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.OTHER, "Registers the end of a remote communication"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.OTHER, "Registers the start of a local communication by optical port"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.OTHER, "Registers the end of a local communication by optical port"));
        DESCRIPTIONS.put(5, new EventInfo(MeterEvent.OTHER, "Registers the beginning of a local communication by HAN interface"));
        DESCRIPTIONS.put(6, new EventInfo(MeterEvent.OTHER, "Registers the end of a local communication by HAN interface"));
    }

    public CommunicationLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return COMMUNICATION_LOGBOOK;
    }
}