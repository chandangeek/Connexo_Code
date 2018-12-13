package com.energyict.protocolimplv2.dlms.a1860.events;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StandardEventLog {

    private List<A1860LogBookFactory.BasicEvent> basicEvents;

    StandardEventLog(List<A1860LogBookFactory.BasicEvent> basicEvents){
        this.basicEvents = basicEvents;
    }

    List<MeterProtocolEvent> buildMeterEvent() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        Date eventTimeStamp;
        int eventId;
        for(A1860LogBookFactory.BasicEvent basicEvent : basicEvents){
            eventTimeStamp = basicEvent.getEventTime();
            eventId = basicEvent.getEventCode();
            switch (eventId) {
                case 1:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERDOWN, eventId, "Power down"));
                    break;
                case 2:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERUP, eventId, "Power up"));
                    break;
                case 3:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_BEFORE, eventId, "Time changed (Old time)"));
                    break;
                case 4:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_AFTER, eventId, "Time changed (New time)"));
                    break;
                case 11:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Configuration change"));
                    break;
                case 18:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Event log cleared"));
                    break;
                case 20:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MAXIMUM_DEMAND_RESET, eventId, "Maximum demand reset"));
                    break;
                case 21:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Self read"));
                    break;
                case 32:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Test mode"));
                    break;
                case 33:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Test mode stopped"));
                    break;
                case 2048:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Enter tier override"));
                    break;
                case 2049:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Exit tier override"));
                    break;
                case 2050:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TAMPER, eventId, "Terminal cover tamper"));
                    break;
                case 2051:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COVER_OPENED, eventId, "Main cover tamper"));
                    break;
                case 2052:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "External event 0"));
                    break;
                case 2053:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "External event 1"));
                    break;
                case 2054:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "External event 2"));
                    break;
                case 2055:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "External event 3"));
                    break;
                case 2056:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Phase a off"));
                    break;
                case 2057:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Phase a on"));
                    break;
                case 2058:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Phase b off"));
                    break;
                case 2059:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Phase b on"));
                    break;
                case 2060:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Phase c off"));
                    break;
                case 2061:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Phase c on"));
                    break;
                case 2062:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NV_MEMORY_ERROR, eventId, "Remote flash failed"));
                    break;
                case 2063:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "RWP event"));
                    break;
                default:
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
            }
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }
}