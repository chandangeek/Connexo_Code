/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorRegisterParser {

    private static final ObisCode errorRegister1 = ObisCode.fromString("1.1.97.97.1.255");
    private static final ObisCode errorRegister2 = ObisCode.fromString("1.1.97.97.2.255");
    private static final ObisCode errorRegister3 = ObisCode.fromString("1.1.97.97.3.255");
    private static final ObisCode errorRegisterFatal = ObisCode.fromString("1.1.97.97.255.255");

    private static final Map<Integer, MeterEventInfo> errorRegister1Events = new HashMap<Integer, MeterEventInfo>();
    private static final Map<Integer, MeterEventInfo> errorRegister2Events = new HashMap<Integer, MeterEventInfo>();
    private static final Map<Integer, MeterEventInfo> errorRegister3Events = new HashMap<Integer, MeterEventInfo>();

    static {
        errorRegister1Events.put(0x10000000, new MeterEventInfo(MeterEvent.CLOCK_INVALID, EventStatusAndDescription.LOSS_OF_TIME_AND_DATE, "Loss of time and date [F.F.1] [10000000]"));
        errorRegister1Events.put(0x02000000, new MeterEventInfo(MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EXTERNAL_BATTERY_EMPTY, "External battery empty [F.F.1] [02000000]"));
        errorRegister1Events.put(0x01000000, new MeterEventInfo(MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.INTERNAL_BATTERY_EMPTY, "Internal battery empty [F.F.1] [01000000]"));
        errorRegister1Events.put(0x00100000, new MeterEventInfo(MeterEvent.STRONG_DC_FIELD_DETECTED, EventStatusAndDescription.MAGNETIC_FIELD_DETECTION, "Magnetic field detection [F.F.1] [00100000]"));
        errorRegister1Events.put(0x00020000, new MeterEventInfo(MeterEvent.COVER_OPENED, EventStatusAndDescription.MAIN_COVER_REMOVAL, "Main cover removal detection [F.F.1] [00020000]"));
        errorRegister1Events.put(0x00010000, new MeterEventInfo(MeterEvent.TERMINAL_OPENED, EventStatusAndDescription.TERMINAL_COVER_REMOVAL, "Terminal cover removal detection [F.F.1] [00010000]"));
        errorRegister1Events.put(0x00001000, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.CHECKSUM_ERROR, "Non fatal checksum error of setting class [F.F.1] [00001000]"));
        errorRegister1Events.put(0x00000010, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.UI_LAODPROFILE_INIT_ERROR, "UI load profile initialization error [F.F.1] [00000010]"));
        errorRegister1Events.put(0x00000001, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.RIPPLE_COMM_ERROR, "Ripple receiver communication error [F.F.1] [00000001]"));

        errorRegister2Events.put(0x00400000, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.THRESHOLD_ACTIVE, "Power quality control threshold active [F.F.2] [00400000]"));
        errorRegister2Events.put(0x00200000, new MeterEventInfo(MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EventStatusAndDescription.THRESHOLD_EXCEEDED2, "Demand overload threshold 2 exceed [F.F.2] [00200000]"));
        errorRegister2Events.put(0x00100000, new MeterEventInfo(MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EventStatusAndDescription.THRESHOLD_EXCEEDED1, "Demand overload threshold 1 exceed [F.F.2] [00100000]"));
        errorRegister2Events.put(0x00010000, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.LOGBOOK_STOPPED, "Load profile / logbook stopped [F.F.2] [00010000]"));
        errorRegister2Events.put(0x00001000, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.COMMUNICATION_ERROR_TO_THE_RIPPLE_RECEIVER, "One time communication error to the ripple receiver [F.F.2] [00001000]"));
        errorRegister2Events.put(0x00000800, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.TANGENT_PHI_Q4_OVERLOAD, "Tangent phi Q4 overload detected [F.F.2] [00000800]"));
        errorRegister2Events.put(0x00000400, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.TANGENT_PHI_Q1_OVERLOAD, "Tangent phi Q1 overload detected [F.F.2] [00000400]"));
        errorRegister2Events.put(0x00000200, new MeterEventInfo(MeterEvent.REVERSE_RUN, EventStatusAndDescription.REVERSE_POWER, "Reverse power detection [F.F.2] [00000200]"));
        errorRegister2Events.put(0x00000100, new MeterEventInfo(MeterEvent.REVERSE_RUN, EventStatusAndDescription.REVERSE_PULSE, "Reverse pulse detection [F.F.2] [00000100]"));
        errorRegister2Events.put(0x00000080, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.NO_LOAD_P3, "No load condition detected in phase 3 [F.F.2] [00000080]"));
        errorRegister2Events.put(0x00000040, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.NO_LOAD_P2, "No load condition detected in phase 2 [F.F.2] [00000040]"));
        errorRegister2Events.put(0x00000020, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.NO_LOAD_P1, "No load condition detected in phase 1 [F.F.2] [00000020]"));
        errorRegister2Events.put(0x00000010, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.COMMUNICATION_ERROR_METER_CHIP, "One time communication error between meter uP and meter chip [F.F.2] [00000010]"));
        errorRegister2Events.put(0x00000008, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.CONFIGURABLE_EVENT2, "Configurable event 2 active [F.F.2] [00000008]"));
        errorRegister2Events.put(0x00000004, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.CONFIGURABLE_EVENT1, "Configurable event 1 active [F.F.2] [00000004]"));
        errorRegister2Events.put(0x00000002, new MeterEventInfo(MeterEvent.OTHER, EventStatusAndDescription.ROTATION_FIELD_WRONG, "Rotation field wrong [F.F.2] [00000002]"));
        errorRegister2Events.put(0x00000001, new MeterEventInfo(MeterEvent.PHASE_FAILURE, EventStatusAndDescription.PHASE_POTENTIAL_MISSING, "Phase potential missing [F.F.2] [00000001]"));

        errorRegister3Events.put(0x20000000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_8_OVERFLOW, "Power quality monitoring value 8 - overflow [F.F.3] [20000000]"));
        errorRegister3Events.put(0x10000000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_8_UNDERFLOW, "Power quality monitoring value 8 - underflow [F.F.3] [10000000]"));
        errorRegister3Events.put(0x02000000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_7_OVERFLOW, "Power quality monitoring value 7 - overflow [F.F.3] [02000000]"));
        errorRegister3Events.put(0x01000000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_7_UNDERFLOW, "Power quality monitoring value 7 - underflow [F.F.3] [01000000]"));
        errorRegister3Events.put(0x00200000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_6_OVERFLOW, "Power quality monitoring value 6 - overflow [F.F.3] [00200000]"));
        errorRegister3Events.put(0x00100000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_6_UNDERFLOW, "Power quality monitoring value 6 - underflow [F.F.3] [00100000]"));
        errorRegister3Events.put(0x00020000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_5_OVERFLOW, "Power quality monitoring value 5 - overflow [F.F.3] [00020000]"));
        errorRegister3Events.put(0x00010000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_5_UNDERFLOW, "Power quality monitoring value 5 - underflow [F.F.3] [00010000]"));
        errorRegister3Events.put(0x00002000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_4_OVERFLOW, "Power quality monitoring value 4 - overflow [F.F.3] [00002000]"));
        errorRegister3Events.put(0x00001000, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_4_UNDERFLOW, "Power quality monitoring value 4 - underflow [F.F.3] [00001000]"));
        errorRegister3Events.put(0x00000200, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_3_OVERFLOW, "Power quality monitoring value 3 - overflow [F.F.3] [00000200]"));
        errorRegister3Events.put(0x00000100, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_3_UNDERFLOW, "Power quality monitoring value 3 - underflow [F.F.3] [00000100]"));
        errorRegister3Events.put(0x00000020, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_2_OVERFLOW, "Power quality monitoring value 2 - overflow [F.F.3] [00000020]"));
        errorRegister3Events.put(0x00000010, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_2_UNDERFLOW, "Power quality monitoring value 2 - underflow [F.F.3] [00000010]"));
        errorRegister3Events.put(0x00000002, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_1_OVERFLOW, "Power quality monitoring value 1 - overflow [F.F.3] [00000002]"));
        errorRegister3Events.put(0x00000001, new MeterEventInfo(MeterEvent.REGISTER_OVERFLOW, EventStatusAndDescription.POWER_QUALITY_MONITORING_VALUE_1_UNDERFLOW, "Power quality monitoring value 1 - underflow [F.F.3] [00000001]"));
    }

    public static List<MeterEvent> readMeterEvents(AbstractDLMS protocol) throws IOException {
        List<MeterEvent> result = new ArrayList<MeterEvent>();

        RegisterValue registerValue = protocol.readRegister(errorRegisterFatal);
        String codeFatal = registerValue.getText().replace("$", "");
        if (!codeFatal.equals("00000000")) {
            result.add(new MeterEvent(new Date(), MeterEvent.FATAL_ERROR, EventStatusAndDescription.FATAL_ERROR, "F.F meter fatal error [F.F] [" + codeFatal + "]"));
        }

        result.addAll(getEventsFromRegister(errorRegister1Events, protocol.readRegister(errorRegister1)));
        result.addAll(getEventsFromRegister(errorRegister2Events, protocol.readRegister(errorRegister2)));
        result.addAll(getEventsFromRegister(errorRegister3Events, protocol.readRegister(errorRegister3)));
        return result;
    }

    /**
     * Iterate over the bits in the flag register and return the proper events
     */
    private static List<MeterEvent> getEventsFromRegister(Map<Integer, MeterEventInfo> errorRegisterEvents, RegisterValue registerValue) {
        MeterEventInfo eventInfo;
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        int alarmCode = ProtocolTools.getIntFromBytes(ProtocolTools.getBytesFromHexString(registerValue.getText()));//E.g. text $01$00$00$00 is 0x01000000
        for (int index = 0; index < 32; index++) {
            if (ProtocolTools.isBitSet(alarmCode, index)) {
                eventInfo = errorRegisterEvents.get((int) Math.pow(2, index));
                if (eventInfo != null) {
                    result.add(eventInfo.getMeterEvent());
                }
            }

        }
        return result;
    }
}