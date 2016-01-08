package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.LeakageEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AlarmFrameParser {

    private static final String ORIGIN_SAF = " [origin SAF]";
    private WaveFlow waveFlow;
    private int status;
    private Date date;
    private int flow;

    /**
     * 0x40 or 0x41 depending on the alarmtype...
     */
    private int alarmId;
    public static final String A = "A";
    public static final String C = "C";
    public static final String B = "B";
    public static final String D = "D";

    final int getAlarmId() {
        return alarmId;
    }

    public AlarmFrameParser(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public int getFlow() {
        return flow;
    }

    public Date getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public void parse(byte[] data) throws IOException {
        int offset = 0;

        alarmId = WaveflowProtocolUtils.toInt(data[offset++]);

        status = WaveflowProtocolUtils.toInt(data[offset++]);

        TimeZone timeZone = waveFlow.getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        date = TimeDateRTCParser.parse(data, offset, 6, timeZone).getTime();
        offset += 6;

        if (data.length > 7) {
            flow = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
        }
    }

    public List<MeterEvent> getMeterEvents() throws IOException {
        if (alarmId == 0x40) {
            return getAlarmEvents();
        } else if (alarmId == 0x41) {
            return getValveEvents();
        } else {
            throw new WaveFlowException("Unexpected alarm ID: " + alarmId);
        }

    }

    private List<MeterEvent> getValveEvents() {
        List<MeterEvent> events = new ArrayList<MeterEvent>();

        if ((status & 0x01) == 0x01) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Wirecut on valve"));
        }
        if ((status & 0x02) == 0x02) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Fault on water gate"));
        }
        if ((status & 0x04) == 0x04) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_DEFAULT, "Threshold detection on credit"));
        }
        if ((status & 0x08) == 0x08) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_DEFAULT, "Credit is zero"));
        }
        return events;
    }

    private List<MeterEvent> getAlarmEvents() {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        String input = A;
        switch (status & 0x03) {     //b1 and b0 in the status byte indicate the concerning input channel.
            case 0:
                input = C;
                break;
            case 1:
                input = A;
                break;
            case 2:
                input = B;
                break;
            case 3:
                input = D;
                break;
        }
        EventStatusAndDescription translator = new EventStatusAndDescription(waveFlow.getDeviceType());

        if ((status & 0x04) == 0x04) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, A.equals(input) ? EventStatusAndDescription.EVENTCODE_REEDFAULT_A : EventStatusAndDescription.EVENTCODE_REEDFAULT_B, "Reed fault detection on input " + input));
        }
        if ((status & 0x08) == 0x08) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, translator.getProtocolCodeForSimpleBackflow((status & 0x03) - 1), "Backflow detection on input " + input));
        }
        if ((status & 0x10) == 0x10) {
            events.add(new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "End of battery life"));
        }
        if ((status & 0x20) == 0x20) {
            events.add(new MeterEvent(date, translator.getEventCode(0x20), translator.getProtocolCodeForStatus(0x20), translator.getEventDescription(0x20)));
        }
        if ((status & 0x40) == 0x40) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(LeakageEvent.START, LeakageEvent.LEAKAGETYPE_RESIDUAL, input), "Leak on input " + input + ". Flow-rate = " + flow));
        }
        if ((status & 0x80) == 0x80) {
            events.add(new MeterEvent(date, MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(LeakageEvent.START, LeakageEvent.LEAKAGETYPE_EXTREME, input), "Burst on input " + input + ". Flow-rate = " + flow));
        }
        return addOriginNotion(events);
    }

    /**
     * Add the origin of the event to the event message
     *
     * @param events list of the received events
     * @return list of the received events, with the origin of the events added to their messages
     */
    private List<MeterEvent> addOriginNotion(List<MeterEvent> events) {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        for (MeterEvent meterEvent : events) {
            result.add(new MeterEvent(meterEvent.getTime(), meterEvent.getEiCode(), meterEvent.getProtocolCode(), meterEvent.getMessage() + ORIGIN_SAF));
        }
        return result;
    }

    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponseACK() {
        return new byte[]{alarmId == 0x40 ? (byte) 0xC0 : (byte) 0xC1, (byte) status};
    }
}