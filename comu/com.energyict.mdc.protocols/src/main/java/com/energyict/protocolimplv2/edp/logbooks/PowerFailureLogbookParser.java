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
public class PowerFailureLogbookParser extends AbstractLogbookParser {

    private static final ObisCode POWER_FAILURE_LOGBOOK = ObisCode.fromString("0.0.99.98.5.255");
    private static final Map<Integer, EventInfo> DESCRIPTIONS = new HashMap<Integer, EventInfo>();

    static {
        DESCRIPTIONS.put(255, new EventInfo(MeterEvent.EVENT_LOG_CLEARED, "Power failure logbook cleared"));
        DESCRIPTIONS.put(1, new EventInfo(MeterEvent.VOLTAGE_SAG, "Event registered when in a period or in the first of a sequence of periods with T minutes: the average supply voltage is below the limit of Un-Δ%"));
        DESCRIPTIONS.put(2, new EventInfo(MeterEvent.VOLTAGE_SAG, "Event registered when in a period or in the first of a sequence of periods with T minutes the average supply voltage of phase L1 is below the limit of Un-Δ%"));
        DESCRIPTIONS.put(3, new EventInfo(MeterEvent.VOLTAGE_SAG, "Event registered when in a period or in the first of a sequence of periods with T minutes the average supply voltage of phase L2 is below the limit of Un-Δ%"));
        DESCRIPTIONS.put(4, new EventInfo(MeterEvent.VOLTAGE_SAG, "Event registered when in a period or in the first of a sequence of periods with T minutes the average supply voltage of phase L3 is below the limit of Un-Δ%"));
        DESCRIPTIONS.put(5, new EventInfo(MeterEvent.VOLTAGE_SWELL, "Event registered when in a period or in the first of a sequence of periods with T minutes: the average supply voltage is above the limit of Un+Δ%"));
        DESCRIPTIONS.put(6, new EventInfo(MeterEvent.VOLTAGE_SWELL, "Event registered when in a period or in the first of a sequence of periods with T minutes the average supply voltage of phase L1 is above the limit of Un+Δ%"));
        DESCRIPTIONS.put(7, new EventInfo(MeterEvent.VOLTAGE_SWELL, "Event registered when in a period or in the first of a sequence of periods with T minutes the average supply voltage of phase L2 is above the limit of Un+Δ%"));
        DESCRIPTIONS.put(8, new EventInfo(MeterEvent.VOLTAGE_SWELL, "Event registered when in a period or in the first of a sequence of periods with T minutes the average supply voltage of phase L3 is above the limit of Un+Δ%"));
        DESCRIPTIONS.put(9, new EventInfo(MeterEvent.PHASE_FAILURE, "Beginning of long power failure in at least one phase (after T minutes without supply)."));
        DESCRIPTIONS.put(10, new EventInfo(MeterEvent.PHASE_FAILURE, "Event registered in the beginning of a long power failure in the phase L1 (after T minutes without supply)"));
        DESCRIPTIONS.put(11, new EventInfo(MeterEvent.PHASE_FAILURE, "Event registered in the beginning of a long power failure in the phase L2 (after T minutes without supply)"));
        DESCRIPTIONS.put(12, new EventInfo(MeterEvent.PHASE_FAILURE, "Event registered in the beginning of a long power failure in the phase L3 (after T minutes without supply)"));
    }

    public PowerFailureLogbookParser(CX20009 protocol, MeteringService meteringService) {
        super(protocol, meteringService);
    }

    protected Map<Integer, EventInfo> getDescriptions() {
        return DESCRIPTIONS;
    }

    public ObisCode getObisCode() {
        return POWER_FAILURE_LOGBOOK;
    }
}