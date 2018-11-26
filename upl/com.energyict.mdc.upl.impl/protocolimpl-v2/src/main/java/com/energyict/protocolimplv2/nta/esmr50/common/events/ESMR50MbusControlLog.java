package com.energyict.protocolimplv2.nta.esmr50.common.events;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.AbstractEvent;

import java.util.*;

public class ESMR50MbusControlLog extends AbstractEvent {

    public ESMR50MbusControlLog(DataContainer dc, AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    private int mBusChannel;
    public ESMR50MbusControlLog(DataContainer dc, int mBusChannel) {
        super(dc);
        this.mBusChannel = mBusChannel;
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        //select only event ids that correspond to mbus channel
        //channel 1: 160 .. 169
        //channel 2: 170 .. 179
        //channel 3: 180 .. 189
        //channel 4: 190 .. 199
        int clonedEventId = eventId;
        //if outside range, consider it an unknown event
        if (eventId != 255 && eventId < (150 + mBusChannel * 10) && eventId > (159 + mBusChannel * 10))
            clonedEventId = 0;

        switch (clonedEventId) {
            //channel 1
            case 160:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 1"));
                break;
            case 161:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 1"));
                break;
            case 162:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 1"));
                break;
            case 163:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 1"));
                break;
            case 164:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 1"));
                break;
            //channel 2
            case 170:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 2"));
                break;
            case 171:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 2"));
                break;
            case 172:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 2"));
                break;
            case 173:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 2"));
                break;
            case 174:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 2"));
                break;
            //channel 3
            case 180:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 3"));
                break;
            case 181:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 3"));
                break;
            case 182:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 3"));
                break;
            case 183:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 3"));
                break;
            case 184:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 3"));
                break;
            //channel 4
            case 190:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_DISCONNECTION_MBUS, eventId, "Manual disconnection M-Bus channel 4"));
                break;
            case 191:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MANUAL_CONNECTION_MBUS, eventId, "Manual connection M-Bus channel 4"));
                break;
            case 192:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_DISCONNECTION_MBUS, eventId, "Remote disconnection M-Bus channel 4"));
                break;
            case 193:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REMOTE_CONNECTION_MBUS, eventId, "Remote connection M-Bus channel 4"));
                break;
            case 194:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.VALVE_ALARM_MBUS, eventId, "Valve alarm M-Bus channel 4"));
                break;
            //all channels
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "MBus control event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}