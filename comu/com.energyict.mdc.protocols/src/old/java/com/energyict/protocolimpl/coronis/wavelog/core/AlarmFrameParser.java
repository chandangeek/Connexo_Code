/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.coronis.wavelog.core.radiocommand.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlarmFrameParser {

    private WaveLog waveLog;
    private int eventStatus;
    private int ioState;
    private int event1 = 0x00;
    private int cause;
    private List<Event> events = new ArrayList<Event>();
    private Date date;
    private int alarmId;

    public AlarmFrameParser(WaveLog waveLog) {
        this.waveLog = waveLog;
    }

    public Date getDate() {
        return date;
    }

    public void parse(byte[] data) throws IOException {
        int offset = 0;

        alarmId = data[offset] & 0xFF;
        offset++;

        int applicationStatus = data[offset] & 0xFF;
        offset++;

        ioState = data[offset] & 0xFF;
        offset++;

        if (alarmId == 0x40) {
            event1 = data[offset] & 0xFF;
            offset++;

            cause = data[offset] & 0xFF;
            offset++;
            date = TimeDateRTCParser.parse(data, offset, 7, waveLog.getTimeZone()).getTime();
            events.add(new Event(cause, date, 0));

        } else if (alarmId == 0x41) {
            for (int index = 0; index < 10; index++) {
                if ((offset + 9) > data.length) {
                    break;             //If no more bytes are sent, stop
                }
                eventStatus = data[offset] & 0xFF;
                offset++;
                cause = data[offset] & 0xFF;
                offset++;
                date = TimeDateRTCParser.parse(data, offset, 7, waveLog.getTimeZone()).getTime();
                offset += 7;
                events.add(new Event(cause, date, eventStatus));
            }
        } else {
            throw new WaveFlowException("Unexpected alarm frame ID: " + alarmId);
        }
    }

    public List<MeterEvent> getMeterEvents() throws IOException {
        if (alarmId == 0x40) {
            return getAlarmEvents();
        } else if (alarmId == 0x41) {
            return getPeriodicEvents();
        } else {
            throw new WaveFlowException("Unexpected alarm ID: " + alarmId);
        }
    }

    private List<MeterEvent> getAlarmEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        //Event 1
        if ((event1 & 0x01) == 0x01) {
            meterEvents.add(new MeterEvent(events.get(0).getEventDate(), MeterEvent.BATTERY_VOLTAGE_LOW, "End of battery life detected"));
        }

        //Event 2
        meterEvents.add(new MeterEvent(events.get(0).getEventDate(), 0, events.get(0).getCauseDescription()));
        return meterEvents;
    }

    private List<MeterEvent> getPeriodicEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (Event event : events) {
            meterEvents.add(new MeterEvent(event.getEventDate(), 0,
                    "Cause: " + event.getCauseDescription() +
                            ". Input 1 level: " + event.getInputStateDescription(1) +
                            ". Input 2 level: " + event.getInputStateDescription(2) +
                            ". Input 3 level: " + event.getInputStateDescription(3) +
                            ". Input 4 level: " + event.getInputStateDescription(4)));
        }
        return meterEvents;
    }

    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponseAck() {
        return new byte[]{alarmId == 0x40 ? (byte) 0xC0 : (byte) 0xC1};
    }
}