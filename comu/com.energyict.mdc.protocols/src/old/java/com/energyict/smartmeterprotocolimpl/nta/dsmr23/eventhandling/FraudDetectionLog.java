/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;

import java.util.Date;
import java.util.List;

public class FraudDetectionLog extends AbstractEvent {

    // Fraud detection log
    protected static final int EVENT_EVENT_LOG_CLEARED = 255;
    protected static final int EVENT_TERMINAL_COVER_REMOVED = 40;
    protected static final int EVENT_TERMINAL_COVER_CLOSED = 41;
    protected static final int EVENT_STRONG_DC_FIELD = 42;
    protected static final int EVENT_STRONG_DC_FIELD_GONE = 43;
    protected static final int EVENT_METER_COVER_REMOVED = 44;
    protected static final int EVENT_METER_COVER_CLOSED = 45;
    protected static final int EVENT_TIMES_WRONG_PASSWORD = 46;

    public FraudDetectionLog(DataContainer dc, final AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    public FraudDetectionLog(DataContainer dc) {
        super(dc);
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        if (!ExtraEvents.extraEvents.containsKey(new Integer(eventId))) {
            switch (eventId) {
                case EVENT_EVENT_LOG_CLEARED: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId, "Fraud detection event log profile cleared"));
                }
                break;
                case EVENT_TERMINAL_COVER_REMOVED: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.TERMINAL_OPENED, eventId, "The terminal cover has been removed"));
                }
                break;
                case EVENT_TERMINAL_COVER_CLOSED: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.TERMINAL_COVER_CLOSED, eventId, "The terminal cover has been closed"));
                }
                break;
                case EVENT_STRONG_DC_FIELD: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.STRONG_DC_FIELD_DETECTED, eventId, "A strong magnetic DC field has been detected"));
                }
                break;
                case EVENT_STRONG_DC_FIELD_GONE: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId, "The strong magnetic DC field disappeared"));
                }
                break;
                case EVENT_METER_COVER_REMOVED: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.COVER_OPENED, eventId, "The meter cover has been removed"));
                }
                break;
                case EVENT_METER_COVER_CLOSED: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.METER_COVER_CLOSED, eventId, "The meter cover has been closed"));
                }
                break;
                case EVENT_TIMES_WRONG_PASSWORD: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.N_TIMES_WRONG_PASSWORD, eventId, "Intrusion Detection, User tried to gain access with a wrong password"));
                }
                break;
                default: {
                    meterEvents.add(createNewFraudDetectionLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
                }
                break;
            }
        } else {
            meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
        }
    }

    public MeterEvent createNewFraudDetectionLogbookEvent(Date eventTimeStamp, int meterEvent, int eventId, String message) {
        return new MeterEvent(eventTimeStamp, meterEvent, eventId, message, EventLogbookId.FraudDetectionEventLogbook.eventLogId(), 0);
    }
}
