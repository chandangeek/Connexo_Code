package com.energyict.protocolimplv2.dlms.landisAndGyr.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 *
 * @author db
 * @since 20/01/2022 - 12:57
 */
public class ZMYStandardEventLog extends StandardEventLog {

    public ZMYStandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
    }
}