package com.energyict.protocolimplv2.dlms.idis.am130.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.AbstractEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AM130CommunicationLog extends AbstractEvent {

    private final boolean isMirrorConnection;

    public AM130CommunicationLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(dc, timeZone);
        this.isMirrorConnection = isMirrorConnection;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 140:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "No connection timeout"));
                break;
            case 141:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Modem Initialization failure"));
                break;
            case 142:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "SIM Card failure"));
                break;
            case 143:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "SIM Card ok"));
                break;
            case 144:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "GSM registration failure"));
                break;
            case 145:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "GPRS registration failure"));
                break;
            case 146:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "PDP context established"));
                break;
            case 147:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "PDP context destroyed"));
                break;
            case 148:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "PDP context failure"));
                break;
            case 149:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Modem SW reset"));
                break;
            case 150:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Modem HW reset"));
                break;
            case 151:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "GSM outgoing connection"));
                break;
            case 152:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "GSM incoming connection"));
                break;
            case 153:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "GSM hang-up"));
                break;
            case 154:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Diagnostic failure"));
                break;
            case 155:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "User initialization failure"));
                break;
            case 156:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Signal quality low"));
                break;
            case 157:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Auto Answer Number of calls exceeded"));
                break;
            case 158:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Local communication attempt"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
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