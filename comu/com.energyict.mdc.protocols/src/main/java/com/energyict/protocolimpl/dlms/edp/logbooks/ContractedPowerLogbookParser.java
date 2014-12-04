package com.energyict.protocolimpl.dlms.edp.logbooks;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.edp.CX20009;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 11/02/14
 * Time: 10:01
 * Author: khe
 */
public class ContractedPowerLogbookParser extends AbstractLogbookParser {

    private static final ObisCode CONTRACTED_POWER_LOGBOOK = ObisCode.fromString("0.0.99.98.3.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Contracted power logbook cleared"));
        DESCRIPTIONS.put(96, new EventInfo(MeterEvent.CONFIGURATIONCHANGE, "Event registered when a change is made to the values of contracted power"));
    }

    public ContractedPowerLogbookParser(CX20009 protocol) {
        super(protocol);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    protected ObisCode getObisCode() {
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