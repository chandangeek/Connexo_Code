package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.Structure;
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
public class DemandManagementLogbookParser extends AbstractLogbookParser {

    private static final ObisCode DEMAND_MANAGEMENT_LOGBOOK = ObisCode.fromString("0.0.99.98.6.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Demand management logbook cleared"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.OTHER, "Order reception for start of critical period of demand management, by indication of contracted power percentage's reduction"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.OTHER, "Order reception for start of critical period of demand management, by indication of the power absolute value"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.OTHER, "Order reception for start of no critical period of demand management"));
        DESCRIPTIONS.put(13, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change in the residual power threshold"));
        DESCRIPTIONS.put(14, new EventInfo(MeterEvent.OTHER, "Beginning of period of residual power (no critical demand management period)"));
        DESCRIPTIONS.put(15, new EventInfo(MeterEvent.OTHER, "End of period of residual power (no critical demand management period)"));
        DESCRIPTIONS.put(16, new EventInfo(MeterEvent.OTHER, "Beginning of period of power reduction in % of contracted power (critical demand management period)"));
        DESCRIPTIONS.put(17, new EventInfo(MeterEvent.OTHER, "End of period of power reduction in % of contracted power (critical demand management period)"));
        DESCRIPTIONS.put(18, new EventInfo(MeterEvent.OTHER, "Beginning of period of reduction to the indicated power absolute value (critical demand management period)"));
        DESCRIPTIONS.put(19, new EventInfo(MeterEvent.OTHER, "End of period of reduction to the indicated power absolute value (critical demand management period)"));
        DESCRIPTIONS.put(21, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Change of power limit value during demand management period"));
    }

    public DemandManagementLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return DEMAND_MANAGEMENT_LOGBOOK;
    }

    @Override
    protected String getExtraDescription(Structure structure) {
        if (structure.nrOfDataTypes() > 2) {
            long apparentThreshold = structure.getDataType(2).longValue();
            return " (current apparent power threshold: " + apparentThreshold + " VA)";
        }
        return "";
    }
}