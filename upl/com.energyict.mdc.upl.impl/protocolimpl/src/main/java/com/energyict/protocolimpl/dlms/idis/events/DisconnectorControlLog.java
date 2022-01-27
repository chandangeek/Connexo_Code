package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DisconnectorControlLog extends AbstractEvent {

    private final boolean isMirrorConnection;

    public DisconnectorControlLog(TimeZone timeZone, DataContainer dc, boolean isMirrorConnection) {
        super(dc, timeZone);
        this.isMirrorConnection = isMirrorConnection;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 59:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DISCONNECTOR_READY_FOR_RECONN, eventId, "Disconnector ready for manual reconnection"));
                break;
            case 60:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION, eventId, "Manual disconnection"));
                break;
            case 61:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION, eventId, "Manual connection"));
                break;
            case 62:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION, eventId, "Remote disconnection"));
                break;
            case 63:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION, eventId, "Remote connection"));
                break;
            case 64:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_DISCONNECTION, eventId, "Local disconnection"));
                break;
            case 65:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "Limiter threshold exceeded"));
                break;
            case 66:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_OK, eventId, "Limiter threshold ok"));
                break;
            case 67:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LIMITER_THRESHOLD_CHANGED, eventId, "Limiter threshold changed"));
                break;
            case 68:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DISCONNECT_RECONNECT_FAIL, eventId, "Disconnect/Reconnect failure"));
                break;
            case 69:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_RECONNECTION, eventId, "Local reconnection"));
                break;
            case 70:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_1_EXCEEDED, eventId, "Supervision monitor 1 threshold exceeded"));
                break;
            case 71:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_1_OK, eventId, "Supervision monitor 1 threshold ok"));
                break;
            case 72:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_2_EXCEEDED, eventId, "Supervision monitor 2 threshold exceeded"));
                break;
            case 73:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_2_OK, eventId, "Supervision monitor 2 threshold ok"));
                break;
            case 74:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_3_EXCEEDED, eventId, "Supervision monitor 3 threshold exceeded"));
                break;
            case 75:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SUPERVISION_3_OK, eventId, "Supervision monitor 3 threshold ok"));
                break;
            case 238:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.STATUS_CHANGED, eventId, "Disconnector physical connect"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Disconnector control event log cleared"));
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