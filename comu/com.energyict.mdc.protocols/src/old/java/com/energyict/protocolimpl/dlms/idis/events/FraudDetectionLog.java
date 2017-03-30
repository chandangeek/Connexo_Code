/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FraudDetectionLog extends AbstractEvent {

    public FraudDetectionLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 40:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_OPENED, eventId, "Terminal cover removed"));
                break;
            case 41:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TERMINAL_COVER_CLOSED, eventId, "Terminal cover closed"));
                break;
            case 42:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.STRONG_DC_FIELD_DETECTED, eventId, "Strong DC field detected"));
                break;
            case 43:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId, "No strong DC field anymore"));
                break;
            case 44:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COVER_OPENED, eventId, "Meter cover removed"));
                break;
            case 45:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_CLOSED, eventId, "Meter cover closed"));
                break;
            case 46:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Association authentication failure (n time failed authentication)"));
                break;
            case 49:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Decryption or authentication failure (n time failure)"));
                break;
            case 50:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Replay attack"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Fraud detection log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}