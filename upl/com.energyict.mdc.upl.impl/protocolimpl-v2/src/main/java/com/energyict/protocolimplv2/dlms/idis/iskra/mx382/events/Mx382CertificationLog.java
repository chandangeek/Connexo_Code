package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by cisac on 1/15/2016.
 */
public class Mx382CertificationLog extends AbstractEvent {

    public int eventIndex = 0;

    public Mx382CertificationLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        DataStructure structure = dcEvents.getRoot().getStructure(eventIndex);
        ObisCode obisCode = structure.getOctetString(1).toObisCode();
        Integer oldValue = structure.getInteger(2);
        Integer newValue = structure.getInteger(3);

        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Critical parameter with obis code : "+obisCode+" changed. " + "Old value: "+ oldValue + " New value: "+ newValue));
    }

    /**
     * @return the MeterEvent List
     */
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        eventIndex = 0;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toDate(timeZone);
                buildMeterEvent(meterEvents, eventTimeStamp, eventId);
            }
            eventIndex++;
        }
        return meterEvents;
    }

}
