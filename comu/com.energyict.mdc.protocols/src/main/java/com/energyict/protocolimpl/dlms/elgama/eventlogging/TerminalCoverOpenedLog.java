/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TerminalCoverOpenedLog extends AbstractEvent {

    private static final int TERMINAL_COVER_OPEN_START = 0x01;
    private static final int TERMINAL_COVER_OPEN_END = 0x00;

    public TerminalCoverOpenedLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        //Mask to prevent big values...
        switch ((eventId & 0x0F)) {
            case TERMINAL_COVER_OPEN_START:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TERMINAL_OPENED, eventId & 0x0F, "Opening of terminal cover event start"));
                break;
            case TERMINAL_COVER_OPEN_END:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TERMINAL_COVER_CLOSED, eventId & 0x0F, "Opening of terminal cover event end"));
                break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}