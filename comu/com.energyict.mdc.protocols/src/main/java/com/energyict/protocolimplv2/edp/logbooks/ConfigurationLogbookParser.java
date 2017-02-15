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

public class ConfigurationLogbookParser extends AbstractLogbookParser {

    private static final ObisCode CONFIGURATION_LOGBOOK = ObisCode.fromString("0.0.99.98.10.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Configuration logbook cleared"));
        DESCRIPTIONS.put(118, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever changes occur in the parameters of disconnector's tripping curve "));
        DESCRIPTIONS.put(122, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a configuration change is made to the channels of load profile"));
        DESCRIPTIONS.put(123, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made to the integration period of average demand"));
        DESCRIPTIONS.put(124, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made to the configuration of at least one of the free registers"));
        DESCRIPTIONS.put(126, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever there is a change to the configuration of the automatic scroll"));
        DESCRIPTIONS.put(127, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever there is a change to the configuration of the manual scroll"));
        DESCRIPTIONS.put(129, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when there is a change to the configuration of the exposure time of each function"));
        DESCRIPTIONS.put(130, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when there is a change in the return time to auto scroll"));
        DESCRIPTIONS.put(131, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made to the monthly billing configuration of the contract 1"));
        DESCRIPTIONS.put(132, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made to the monthly billing configuration of the contract 2"));
        DESCRIPTIONS.put(133, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made to the daily billing configuration of the contract 1"));
        DESCRIPTIONS.put(134, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered whenever a change is made to the daily billing configuration of the contract 2"));
    }

    public ConfigurationLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return CONFIGURATION_LOGBOOK;
    }
}