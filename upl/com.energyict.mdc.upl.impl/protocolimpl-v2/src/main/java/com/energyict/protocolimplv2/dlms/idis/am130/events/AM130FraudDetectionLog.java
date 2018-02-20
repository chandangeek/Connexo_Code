package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.FraudDetectionLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130FraudDetectionLog extends FraudDetectionLog {

    private final boolean isMirrorConnection;

    public AM130FraudDetectionLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(timeZone, dc);
        this.isMirrorConnection = isMirrorConnection;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 91:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REVERSE_RUN, eventId, "Current Reversal"));
                break;
            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
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