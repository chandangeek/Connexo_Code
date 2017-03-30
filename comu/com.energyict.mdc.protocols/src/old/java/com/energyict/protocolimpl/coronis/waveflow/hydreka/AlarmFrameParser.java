/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter.BatteryLifeDurationCounter;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AlarmFrameParser {

    private static final String ORIGIN_SAF = " [origin SAF]";
    private WaveFlow waveFlow;

    private Date eventTimeStamp;
    private int alarmStatus;
    private int alarmDataField;

    public AlarmFrameParser(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public void parse(byte[] data) throws IOException {

        //Skip first byte (alarm ID, is always 0x40)
        //Skip the generic header, 19 bytes

        alarmStatus = ProtocolTools.getIntFromBytes(data, 20, 3);

        TimeZone timeZone = waveFlow.getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        eventTimeStamp = TimeDateRTCParser.parse(data, 23, 7, timeZone).getTime();
        alarmDataField = ProtocolTools.getUnsignedIntFromBytes(data, 30, 2);
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        if ((alarmStatus & 0x0100) == 0x0100) {
            double value = 100 - (((BatteryLifeDurationCounter.INITIAL_BATTERY_LIFE_COUNT * 100) - ((alarmDataField << 8) * 100)) / BatteryLifeDurationCounter.INITIAL_BATTERY_LIFE_COUNT);
            double remainingBattery = Math.round(value * 100.0) / 100.0;
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "WaveFlow module low battery detected. Remaining battery is " + remainingBattery + "%." + ORIGIN_SAF));
        }
        if ((alarmStatus & 0x0200) == 0x0200) {
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW_PROBE, "Permalog probe low battery detected" + ORIGIN_SAF));
        }
        if ((alarmStatus & 0x0800) == 0x0800) {
            int currentLevelValue = (alarmDataField >> 8) & 0xFF;
            int currentSpreadValue = alarmDataField & 0xFF;
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_LEAKAGE_RESIDUAL_START_A, "Leakage detected. Current level value is " + currentLevelValue + ", current spread value is " + currentSpreadValue + "." + ORIGIN_SAF));
        }
        if ((alarmStatus & 0x2000) == 0x2000) {
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tampering detected" + ORIGIN_SAF));
        }
        return meterEvents;
    }

    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponseACK() {
        byte[] alarmStatusBytes = ProtocolTools.getBytesFromInt(alarmStatus, 3);
        return ProtocolTools.concatByteArrays(new byte[]{(byte) 0xC0}, alarmStatusBytes);
    }
}