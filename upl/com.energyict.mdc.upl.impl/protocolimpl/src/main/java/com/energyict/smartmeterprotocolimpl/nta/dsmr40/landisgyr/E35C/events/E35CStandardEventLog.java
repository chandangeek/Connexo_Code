package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.events;

import com.energyict.cbo.Utils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.StandardEventLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 1/07/2015 - 16:51
 */
public class E35CStandardEventLog extends StandardEventLog {

    protected static final int EVENT_BILLING_PERIOD_RESET = 202;
    private TimeZone timeZone;

    public E35CStandardEventLog(DataContainer dc) {
        super(dc);
    }

    public E35CStandardEventLog(DataContainer dc, TimeZone timeZone) {
        super(dc);
        this.timeZone = timeZone;
    }

    public List<MeterEvent> getMeterEvents() throws ProtocolException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray()).getBEREncodedByteArray(), 0, Utils.getStandardTimeZone(timeZone)).getValue().getTime();
            }
            if (eventTimeStamp != null) {
                buildMeterEvent(meterEvents, eventTimeStamp, eventId);
            }
        }
        return meterEvents;
    }

    protected void buildMeterEvent(final List<MeterEvent> meterEvents, final Date eventTimeStamp, final int eventId) {
        switch (eventId) {
            case EVENT_BILLING_PERIOD_RESET:
                meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.BILLING_ACTION, eventId, "Billing period reset"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
