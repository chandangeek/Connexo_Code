/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.AbstractEvent;

import java.util.Date;
import java.util.List;

public class AM540CommunicationSessionLog extends AbstractEvent {

    private static final int EVENT_EVENT_LOG_CLEARED = 255;
    private static final int EVENT_METROLOGICAL_MAINTENANCE = 71;
    private static final int EVENT_TECHNICAL_MAINTENANCE = 72;
    private static final int EVENT_RETRIEVE_METER_READINGS_E = 73;
    private static final int EVENT_RETRIEVE_METER_READINGS_G = 74;
    private static final int EVENT_RETRIEVE_INTERVAL_DATA_E = 75;
    private static final int EVENT_RETRIEVE_INTERVAL_DATA_G = 76;

    /**
     * @param dc            the DataContainer, containing all the eventData
     * @param deviationType the interpretation type of the DataTime
     */
    public AM540CommunicationSessionLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case EVENT_EVENT_LOG_CLEARED: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId, "Communication session log profile cleared"));
            }
            break;
            case EVENT_METROLOGICAL_MAINTENANCE: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Metrological maintenance"));
            }
            break;
            case EVENT_TECHNICAL_MAINTENANCE: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Technical maintenance"));
            }
            break;
            case EVENT_RETRIEVE_METER_READINGS_E: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve meter readings E"));
            }
            break;
            case EVENT_RETRIEVE_METER_READINGS_G: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve meter readings G"));
            }
            break;
            case EVENT_RETRIEVE_INTERVAL_DATA_E: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve interval data E"));
            }
            break;
            case EVENT_RETRIEVE_INTERVAL_DATA_G: {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Retrieve interval data E"));
            }
            break;
            default:
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}
