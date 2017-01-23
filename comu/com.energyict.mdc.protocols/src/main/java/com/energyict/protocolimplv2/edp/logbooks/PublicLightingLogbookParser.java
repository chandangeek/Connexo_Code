package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.edp.CX20009;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 11/02/14
 * Time: 10:01
 * Author: khe
 */
public class PublicLightingLogbookParser extends AbstractLogbookParser {

    private static final ObisCode PUBLIC_LIGHTING_LOGBOOK = ObisCode.fromString("0.0.99.98.11.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Public lighting logbook cleared"));
        DESCRIPTIONS.put(1, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Transition of 1st Public Lighting output to connected state"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.REMOTE_DISCONNECTION, "Transition of 1st Public Lighting output to disconnected state"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 1st Public Lighting output control to state \"0\" - Public Lighting permanently disconnected"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 1st Public Lighting output control to state \"1\" - Public Lighting permanently connected"));
        DESCRIPTIONS.put(5, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 1st Public Lighting output control to state \"2\" - Astronomical clock"));
        DESCRIPTIONS.put(6, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 1st Public Lighting output control to state \"3\" - Time switching table"));
        DESCRIPTIONS.put(7, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the time switching table of 1st Public Lighting output"));
        DESCRIPTIONS.put(8, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the offsets table for the 1st Public Lighting output control"));
        DESCRIPTIONS.put(9, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the GPS coordinates, used as reference to the astronomical clock"));
        DESCRIPTIONS.put(10, new EventInfo(MeterEvent.OTHER, "Detected the absence of power consumption in the circuit when Public Lighting is connected"));
        DESCRIPTIONS.put(11, new EventInfo(MeterEvent.OTHER, "Detected power consumption in Public Lighting circuit that exceeds the limit value defined, when the Public Lighting is disconnected"));
        DESCRIPTIONS.put(12, new EventInfo(MeterEvent.OTHER, "Variation of power consumption above the limit value in Public Lighting circuit, when the Public Lighting is connected"));
        DESCRIPTIONS.put(13, new EventInfo(MeterEvent.OTHER, "Variation of power consumption below the limit value in Public Lighting circuit, when the Public Lighting is connected"));
        DESCRIPTIONS.put(14, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Direct command to connect the 1st Public Lighting output"));
        DESCRIPTIONS.put(15, new EventInfo(MeterEvent.REMOTE_DISCONNECTION, "Direct command to disconnect the 1st Public Lighting output"));
        DESCRIPTIONS.put(16, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Transition of 2nd Public Lighting output to connected state"));
        DESCRIPTIONS.put(17, new EventInfo(MeterEvent.REMOTE_DISCONNECTION, "Transition of 2nd Public Lighting output to disconnected state"));
        DESCRIPTIONS.put(18, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 2nd Public Lighting output control to state \"0\" - Public Lighting permanently disconnected"));
        DESCRIPTIONS.put(19, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 2nd Public Lighting output control to state \"1\" - Public Lighting permanently connected"));
        DESCRIPTIONS.put(20, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 2nd Public Lighting output control to state \"2\" - Astronomical clock"));
        DESCRIPTIONS.put(21, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the operating mode of 2nd Public Lighting output control to state \"3\" - Time switching table"));
        DESCRIPTIONS.put(22, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the time switching table of 2nd Public Lighting output"));
        DESCRIPTIONS.put(23, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of the offsets table for the 2nd Public Lighting output control"));
        DESCRIPTIONS.put(24, new EventInfo(MeterEvent.REMOTE_CONNECTION, "Direct command to connect the 2nd Public Lighting output"));
        DESCRIPTIONS.put(25, new EventInfo(MeterEvent.REMOTE_DISCONNECTION, "Direct command to disconnect the 2nd Public Lighting output"));
    }

    public PublicLightingLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return PUBLIC_LIGHTING_LOGBOOK;
    }
}