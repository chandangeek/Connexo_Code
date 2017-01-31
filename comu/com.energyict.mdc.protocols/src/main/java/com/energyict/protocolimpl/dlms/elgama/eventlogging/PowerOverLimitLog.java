/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.elgama.eventlogging;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PowerOverLimitLog extends AbstractEvent {

    private static final int POWER_OVER_LIMIT_START = 0x01;
    private static final int POWER_OVER_LIMIT_END = 0x00;

    public PowerOverLimitLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case POWER_OVER_LIMIT_START:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Contractual power overlimit start"));
                break;
            case POWER_OVER_LIMIT_END:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_OK, eventId, "Contractual power overlimit end"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}