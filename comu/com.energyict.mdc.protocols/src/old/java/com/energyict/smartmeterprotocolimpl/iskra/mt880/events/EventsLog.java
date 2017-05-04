/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.iskra.mt880.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;

import java.util.Date;
import java.util.List;

public class EventsLog extends AbstractEvent {

   private final EventLogbookId eventLogbookId;

    public EventsLog(DataContainer dc, final AXDRDateTimeDeviationType deviationType, EventLogbookId eventLogbookId) {
        super(dc, deviationType);
        this.eventLogbookId = eventLogbookId;
    }

    /**
     * {@inheritDoc}
     */
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        MT880MeterEvent meterEvent = MT880MeterEvent.getMeterEventForDeviceCode(eventId);

        if (meterEvent != null) {
            meterEvents.add(new MeterEvent(eventTimeStamp, meterEvent.getEiserverCode(), meterEvent.getDeviceCode(), meterEvent.getDescription(), getEventLogbookId(), 0));
        } else {
            System.out.println(eventId + " - " + eventLogbookId.name());
            meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId, getEventLogbookId(), 0));
        }
    }

    public int getEventLogbookId() {
        return eventLogbookId.eventLogId();
    }
}
