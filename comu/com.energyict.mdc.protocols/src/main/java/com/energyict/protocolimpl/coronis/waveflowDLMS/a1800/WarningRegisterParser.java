/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS.a1800;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflowDLMS.AbstractDLMS;
import com.energyict.protocolimpl.coronis.waveflowDLMS.MeterEventInfo;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarningRegisterParser {

    private static final ObisCode warningByte1 = ObisCode.fromString("0.0.97.97.3.255");
    private static final ObisCode warningByte2 = ObisCode.fromString("0.0.97.97.4.255");
    private static final ObisCode warningByte3 = ObisCode.fromString("0.0.97.97.5.255");


    private static final Map<Integer, MeterEventInfo> warningRegister1Events = new HashMap<Integer, MeterEventInfo>();
    private static final Map<Integer, MeterEventInfo> warningRegister2Events = new HashMap<Integer, MeterEventInfo>();
    private static final Map<Integer, MeterEventInfo> warningRegister3Events = new HashMap<Integer, MeterEventInfo>();

    static {
        warningRegister1Events.put(1, new MeterEventInfo(MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.LOW_BATTERY, "Low battery warning [W1] [000001]"));
        warningRegister1Events.put(2, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.IMPROPER_METER_ENGINE_OPERATION, "Improper meter engine operation warning [W1] [000010]"));
        warningRegister1Events.put(4, new MeterEventInfo(MeterEvent.REVERSE_RUN, EventStatusAndDescription.REVERSE_ENERGY, "Reverse energy flow warning [W1] [000100]"));
        warningRegister1Events.put(16, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.POTENTIAL_INDICATOR, "Potential indicator warning [W1] [010000]"));
        warningRegister1Events.put(32, new MeterEventInfo(MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EventStatusAndDescription.DEMAND_OVERLOAD, "Demand overload warning [W1] [100000]"));
        warningRegister2Events.put(1, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.SERVICE_CURRENT_TEST_FAILURE, "Service current test failure warning [W2] [000001]"));
        warningRegister2Events.put(4, new MeterEventInfo(MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EventStatusAndDescription.DEMAND_THRESHOLD_EXCEEDED, "Demand threshold exceeded warning [W2] [000100]"));
        warningRegister2Events.put(8, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.LINE_FREQUENCY_WARNING, "Line frequency warning warning [W2] [001000]"));
        warningRegister2Events.put(16, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.TRUEQ_TEST_FAILURE, "TRueQ test failure warning [W2] [010000]"));
        warningRegister2Events.put(32, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.END_OF_CALENDAR, "End of calendar warning [W2] [100000]"));
        warningRegister3Events.put(1, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.READ_WITHOUT_POWER_BATTERY_DISCHARGED, "Read without Power battery discharged [W3] [000001]"));
        warningRegister3Events.put(2, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.READ_WITHOUT_POWER_MODE_ACTIVE, "Read without Power mode active [W3] [000010]"));

    }

    public static List<MeterEvent> readMeterEvents(AbstractDLMS protocol) throws IOException {
        List<MeterEvent> result = new ArrayList <MeterEvent>();
        int inversedCode1 = getInversedCode(protocol.readRegister(warningByte1));
        result.addAll(getEventsFromRegister(warningRegister1Events, inversedCode1));
        int inversedCode2 = getInversedCode(protocol.readRegister(warningByte2));
        result.addAll(getEventsFromRegister(warningRegister2Events, inversedCode2));
        int inversedCode3 = getInversedCode(protocol.readRegister(warningByte3));
        result.addAll(getEventsFromRegister(warningRegister3Events, inversedCode3));
        return result;
    }

    /**
     * Iterate over the bits in the flag register and return the proper events
     */
    private static List<MeterEvent> getEventsFromRegister(Map<Integer, MeterEventInfo> errorRegisterEvents, int alarmCode) {
        MeterEventInfo eventInfo;
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        for (int index = 0; index < 6; index++) {
            if (ProtocolTools.isBitSet(alarmCode, index)) {
                eventInfo = errorRegisterEvents.get((int) Math.pow(2, index));
                if (eventInfo != null) {
                    result.add(eventInfo.getMeterEvent());
                }
            }
        }
        return result;
    }

    private static int getInversedCode(RegisterValue registerValue) {
        int alarmCode = registerValue.getQuantity().getAmount().intValue();
        String binary = Integer.toBinaryString(alarmCode);
        String paddedBinary = pad(binary, 6);
        String inversedBinary = new StringBuffer(paddedBinary).reverse().toString().substring(0, 6);
        return Integer.parseInt(inversedBinary, 2);
    }

    private static String pad(String binary, int length) {
        while (binary.length() < length) {
            binary = "0" + binary;
        }
        return binary;
    }
}