/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class InternalErrorLog extends AbstractEvent {

    private static final int INTERNAL_ERROR_START = 0x01;
    private static final int INTERNAL_ERROR_END = 0x00;

    public InternalErrorLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case INTERNAL_ERROR_START:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Internal error event start"));
                break;
            case INTERNAL_ERROR_END:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Internal error event end"));
                break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}