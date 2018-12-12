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
public class ClockSyncLogbookParser extends AbstractLogbookParser {

    private static final ObisCode CLOCK_SYNCHRONIZATION_LOGBOOK = ObisCode.fromString("0.0.99.98.8.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Clock synchronization logbook cleared"));
        DESCRIPTIONS.put(98, new EventInfo(MeterEvent.SETCLOCK, "Event registered when a synchronization of the real time clock occurs"));
    }

    public ClockSyncLogbookParser(CX20009 protocol) {
        super(protocol);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    protected ObisCode getObisCode() {
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