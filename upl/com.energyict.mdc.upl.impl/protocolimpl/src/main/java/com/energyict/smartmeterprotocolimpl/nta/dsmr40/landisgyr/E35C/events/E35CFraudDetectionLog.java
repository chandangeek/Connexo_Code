package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C.events;

import com.energyict.cbo.Utils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.FraudDetectionLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 1/07/2015 - 17:55
 */
public class E35CFraudDetectionLog extends FraudDetectionLog {

    private TimeZone timeZone;

    public E35CFraudDetectionLog(DataContainer dc) {
        super(dc, AXDRDateTimeDeviationType.Negative);
    }

    public E35CFraudDetectionLog(DataContainer dc, TimeZone timeZone) {
        super(dc, AXDRDateTimeDeviationType.Negative);
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
}