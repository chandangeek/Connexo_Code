package com.energyict.protocolimpl.dlms.edp.logbooks;

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
public class AntiFraudLogbookParser extends AbstractLogbookParser {

    private static final ObisCode ANTI_FRAUD_LOGBOOK = ObisCode.fromString("0.0.99.98.1.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Anti fraud logbook cleared"));
        DESCRIPTIONS.put(1, new EventInfo(MeterEvent.TERMINAL_OPENED, "The event marks the opening of the terminal cover"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.TERMINAL_COVER_CLOSED, "The event marks the closing of the terminal cover"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.STRONG_DC_FIELD_DETECTED, "The event marks the detection of a strong magnetic field above the acceptable value (default)"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, "The event marks the disappearance of a strong magnetic field above the acceptable value (default)"));
        DESCRIPTIONS.put(5, new EventInfo(MeterEvent.OTHER, "The event is registered when the meter detects a current but doesn't detect voltage"));
        DESCRIPTIONS.put(6, new EventInfo(MeterEvent.N_TIMES_WRONG_PASSWORD, "The event is registered when there is attempted of communication with the meter with a wrong password"));
    }

    public AntiFraudLogbookParser(CX20009 protocol) {
        super(protocol);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    protected ObisCode getObisCode() {
        return ANTI_FRAUD_LOGBOOK;
    }
}