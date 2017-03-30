/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130StandardEventLog extends StandardEventLog {

    public AM130StandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 20:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "External alert detected"));
                break;
            case 52:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unexpected consumption"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}