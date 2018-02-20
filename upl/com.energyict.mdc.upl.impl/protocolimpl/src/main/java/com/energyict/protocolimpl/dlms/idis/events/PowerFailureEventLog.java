package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PowerFailureEventLog extends AbstractEvent {

    private final boolean isMirrorConnection;

    public PowerFailureEventLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(dc, timeZone);
        this.isMirrorConnection = isMirrorConnection;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VOLTAGE_SAG, eventId, "Long power failure in a phase, duration: " + eventId + " seconds"));
    }

    @SuppressWarnings("Duplicates")
    @Override
    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        Date eventTimeStamp;
        for (int i = 0; i <= (size - 1); i++) {
            int eventId;
            if(isMirrorConnection){
                eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(2) & 0xFF;
            }else {
                eventId = (int) this.dcEvents.getRoot().getStructure(i).getValue(1) & 0xFF; // To prevent negative values
            }
            if (isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))) {
                eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toDate(timeZone);
                buildMeterEvent(meterEvents, eventTimeStamp, eventId);
            }
        }
        return meterEvents;
    }
}