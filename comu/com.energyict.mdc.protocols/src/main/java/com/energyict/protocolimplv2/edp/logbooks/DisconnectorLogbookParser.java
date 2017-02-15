/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimplv2.edp.CX20009;
import com.energyict.protocolimplv2.edp.registers.DisconnectControlState;

import java.util.HashMap;
import java.util.Map;

public class DisconnectorLogbookParser extends AbstractLogbookParser {

    private static final ObisCode DISCONNECT_CONTROL_LOGBOOK = ObisCode.fromString("0.0.99.98.2.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Disconnector logbook cleared"));
        DESCRIPTIONS.put(1, new EventInfo(MeterEvent.MANUAL_CONNECTION, "Event registered when there is a reconnection of the disconnector by button operation"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.REMOTE_DISCONNECTION, "Event registered when is verified the opening of the disconnector by remote control"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Event registered when is verified the closing of the disconnector by remote control"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.LOCAL_DISCONNECTION, "Event registered when is verified the opening of the disconnector because of excessive demand"));
        DESCRIPTIONS.put(5, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Event registered when there is a reconnection of the disconnector by varying the impedance in output circuit of the meter (by action on the Power Control Breaker)"));
        DESCRIPTIONS.put(6, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Event recorded if after the tripping time of disconnector the actual current exceeds the rated breaking current, and is no longer possible to interrupt the supply"));
        DESCRIPTIONS.put(7, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when the control mode of the disconnector is changed from 0 to a different value (transitions allowed)"));
        DESCRIPTIONS.put(8, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when the control mode of the disconnector is changed to 0 (transitions not allowed)"));
        DESCRIPTIONS.put(9, new EventInfo(MeterEvent.LOCAL_DISCONNECTION, "Event registered when is verified the opening of disconnector for exceeding the residual power value during a no critical demand management period"));
        DESCRIPTIONS.put(11, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Event registered when is verified the reconnection of disconnector after exceeding the residual power value during a no critical demand management period"));
        DESCRIPTIONS.put(12, new EventInfo(MeterEvent.LOCAL_DISCONNECTION, "Event registered when is verified the opening of disconnector for exceeding the power limit value during a critical demand management period"));
        DESCRIPTIONS.put(13, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Event registered when is verified the closure of disconnector after exceeding the power limit value during a critical demand management period"));
        DESCRIPTIONS.put(14, new EventInfo(MeterEvent.OTHER, "Event registered when the function of power control of disconnector is inhibited"));
        DESCRIPTIONS.put(15, new EventInfo(MeterEvent.OTHER, "Event registered when the function of power control of disconnector is enabled"));
        DESCRIPTIONS.put(16, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when the control mode of disconnector is changed"));
    }

    public DisconnectorLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return DISCONNECT_CONTROL_LOGBOOK;
    }

    @Override
    protected String getExtraDescription(Structure structure) {
        if (structure.nrOfDataTypes() > 2) {
            String previousState = DisconnectControlState.fromValue(structure.getDataType(2).getTypeEnum().getValue()).getDescription();
            String currentState = DisconnectControlState.fromValue(structure.getDataType(3).getTypeEnum().getValue()).getDescription();
            long apparentThreshold = structure.getDataType(4).longValue();
            long powerControlInhibition = structure.getDataType(5).intValue();
            return " (previous state: " + previousState + ", current state: " + currentState + ", current apparent power threshold: " + apparentThreshold + " VA, power control inhibition: " + powerControlInhibition + ")";
        }
        return "";
    }
}