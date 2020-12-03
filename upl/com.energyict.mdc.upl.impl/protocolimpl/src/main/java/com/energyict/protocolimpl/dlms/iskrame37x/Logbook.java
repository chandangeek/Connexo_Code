package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.MeterEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class Logbook {
    
    private static final int DEBUG = 5;
    
    private TimeZone timeZone;
    
    private static final int EVENT_STATUS_FATAL_ERROR=0x0001; // X
    private static final int EVENT_STATUS_DEVICE_CLOCK_RESERVE=0x0002;
    private static final int EVENT_STATUS_VALUE_CORRUPT=0x0004;
    private static final int EVENT_STATUS_DAYLIGHT_CHANGE=0x0008;
    private static final int EVENT_STATUS_BILLING_RESET=0x0010; // X
    private static final int EVENT_STATUS_DEVICE_CLOCK_CHANGED=0x0020; // X
    private static final int EVENT_STATUS_POWER_RETURNED=0x0040; // X
    private static final int EVENT_STATUS_POWER_FAILURE=0x0080; // X
    private static final int EVENT_STATUS_VARIABLE_SET=0x0100; //
    private static final int EVENT_STATUS_UNRELIABLE_OPERATING_CONDITIONS=0x0200;
    private static final int EVENT_STATUS_END_OF_UNRELIABLE_OPERATING_CONDITIONS=0x0400;
    private static final int EVENT_STATUS_UNRELIABLE_EXTERNAL_CONTROL=0x0800;
    private static final int EVENT_STATUS_END_OF_UNRELIABLE_EXTERNAL_CONTROL=0x1000;
    private static final int EVENT_STATUS_EVENTLOG_CLEARED=0x2000; // X
    private static final int EVENT_STATUS_LOADPROFILE_CLEARED=0x4000; // X
    private static final int EVENT_STATUS_L1_POWER_FAILURE=0x8001; // X
    private static final int EVENT_STATUS_L2_POWER_FAILURE=0x8002; // X
    private static final int EVENT_STATUS_L3_POWER_FAILURE=0x8003; // X
    private static final int EVENT_STATUS_L1_POWER_RETURNED=0x8004; // X
    private static final int EVENT_STATUS_L2_POWER_RETURNED=0x8005; // X
    private static final int EVENT_STATUS_L3_POWER_RETURNED=0x8006; // X
    private static final int EVENT_STATUS_METER_COVER_OPENED=0x8010; // X
    private static final int EVENT_STATUS_TERMINAL_COVER_OPENED=0x8011; // X
    
    /** Creates a new instance of Logbook */
    public Logbook(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
    
    
    public List<MeterEvent> getMeterEvents(DataContainer dc) {
        List<MeterEvent> meterEvents = new ArrayList<>(); // of type MeterEvent
        int size = dc.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i<=(size-1); i++) {
        	int eventId = (int) dc.getRoot().getStructure(i).getValue(1);
        	if ( isOctetString(dc.getRoot().getStructure(i)) )
                eventTimeStamp = dc.getRoot().getStructure(i).getOctetString(0).toDate(timeZone);

            meterEvents.addAll(buildMeterEvent(eventTimeStamp,eventId));
            if (DEBUG >= 1) System.out.println("KV_DEBUG> eventId="+eventId+", eventTimeStamp="+eventTimeStamp);

        }
        return meterEvents;
    }
    
    private boolean isOctetString(DataStructure structure) {
    	
    	if ( structure.getElement(0) instanceof com.energyict.dlms.OctetString )
    		return true;
    	else if ( structure.getElement(0) instanceof java.lang.Integer )
    		return false;
    	else
    		return false;
	}

    private boolean hasEvent(int eventId, int deviceEventCode) {
        return (eventId & deviceEventCode) == deviceEventCode;
    }

	private List<MeterEvent> buildMeterEvent(Date eventTimeStamp, int eventId) {
        List<com.energyict.protocol.MeterEvent> meterEvents = new ArrayList<>();

        int aloneEventId = eventId + 0x10000;

        if ((eventId & 0x8000) == 0) {

            /* These events can be combined */
            if (hasEvent(eventId, EVENT_STATUS_FATAL_ERROR)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId,"Device disturbance"));
            }
            if (hasEvent(eventId, EVENT_STATUS_DEVICE_CLOCK_RESERVE)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLOCK_ERROR, eventId,"Event status device clock reserve"));
            }
            if (hasEvent(eventId, EVENT_STATUS_VALUE_CORRUPT)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId,"Event status value corrupt"));
            }
            if (hasEvent(eventId, EVENT_STATUS_DAYLIGHT_CHANGE)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, eventId,"Event status daylight change"));
            }
            if (hasEvent(eventId, EVENT_STATUS_BILLING_RESET)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.BILLING_ACTION, eventId,"Billing action occured"));
            }
            if (hasEvent(eventId, EVENT_STATUS_DEVICE_CLOCK_CHANGED)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId,"Set time"));
            }
            if (hasEvent(eventId, EVENT_STATUS_POWER_RETURNED)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, eventId,"PowerUp"));
            }
            if (hasEvent(eventId, EVENT_STATUS_POWER_FAILURE)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId,"PowerDown"));
            }
            if (hasEvent(eventId, EVENT_STATUS_VARIABLE_SET)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CONFIGURATIONCHANGE, eventId,"Event status variable set"));
            }
            if (hasEvent(eventId, EVENT_STATUS_UNRELIABLE_OPERATING_CONDITIONS)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.APPLICATION_ALERT_START, eventId,"Event status unreliable operating conditions"));
            }
            if (hasEvent(eventId, EVENT_STATUS_END_OF_UNRELIABLE_OPERATING_CONDITIONS)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.APPLICATION_ALERT_STOP, eventId,"Event status end of unreliable operating conditions"));
            }
            if (hasEvent(eventId, EVENT_STATUS_UNRELIABLE_EXTERNAL_CONTROL)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId,"Event status unreliable external control"));
            }
            if (hasEvent(eventId, EVENT_STATUS_END_OF_UNRELIABLE_EXTERNAL_CONTROL)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId,"Event status end of unreliable external control"));
            }
            if (hasEvent(eventId, EVENT_STATUS_EVENTLOG_CLEARED)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId,"Event status event log cleared"));
            }
            if (hasEvent(eventId, EVENT_STATUS_LOADPROFILE_CLEARED)) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.LOADPROFILE_CLEARED, eventId,"Event status load profile cleared"));
            }
        } else {

            /* These events occur alone */
            if (aloneEventId == EVENT_STATUS_L1_POWER_FAILURE) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_A_OFF, aloneEventId, "Event status L1 phase failure"));
            }
            if (aloneEventId == EVENT_STATUS_L2_POWER_FAILURE) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_B_OFF, aloneEventId, "Event status L2 phase failure"));
            }
            if (aloneEventId == EVENT_STATUS_L3_POWER_FAILURE) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_C_OFF, aloneEventId, "Event status L3 phase failure"));
            }
            if (aloneEventId == EVENT_STATUS_L1_POWER_RETURNED) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_A_ON, aloneEventId, "Event status end of L1 phase failure"));
            }
            if (aloneEventId == EVENT_STATUS_L2_POWER_RETURNED) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_B_ON, aloneEventId, "Event status end of L2 phase failure"));
            }
            if (aloneEventId == EVENT_STATUS_L3_POWER_RETURNED) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_C_ON, aloneEventId, "Event status end of L3 phase failure"));
            }
            if (aloneEventId == EVENT_STATUS_METER_COVER_OPENED) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_COVER_OPENED, aloneEventId, "Event status meter cover opened"));
            }
            if (aloneEventId == EVENT_STATUS_TERMINAL_COVER_OPENED) {
                meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TERMINAL_OPENED, aloneEventId,  "Event status meter cover opened"));
            }
        }
        return meterEvents;
        
    }

}
