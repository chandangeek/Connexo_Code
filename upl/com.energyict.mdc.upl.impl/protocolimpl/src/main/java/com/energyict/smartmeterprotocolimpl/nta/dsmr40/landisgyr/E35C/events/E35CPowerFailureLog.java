package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.events;

import com.energyict.cbo.Utils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 1/07/2015 - 17:57
 */
public class E35CPowerFailureLog extends PowerFailureLog {

    private TimeZone timeZone;

    public E35CPowerFailureLog(DataContainer dc) {
        super(dc);
    }

    public E35CPowerFailureLog(DataContainer dc, TimeZone timeZone) {
        super(dc);
        this.timeZone = timeZone;
    }

    @Override
    public List<MeterEvent> getMeterEvents() throws ProtocolException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i <= (size - 1); i++) {
            long duration = this.dcEvents.getRoot().getStructure(i).getValue(1);
            if (duration < 0) {
                duration += 0xFFFF;
                duration++;
            }
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray()).getBEREncodedByteArray(), 0, Utils.getStandardTimeZone(timeZone)).getValue().getTime();
            }
            if (eventTimeStamp != null) {
                buildMeterEvent(meterEvents, eventTimeStamp, duration);
            }
        }
        return meterEvents;
    }
}
