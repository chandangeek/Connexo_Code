/*
 * Logbook.java
 *
 * Created on 17 november 2004, 9:12
 */

package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

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

    TimeZone timeZone;

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
        List<MeterEvent> meterEvents = new ArrayList<>();
        int size = dc.getRoot().getNrOfElements();
        Date eventTimeStamp = null;
        for (int i = 0; i<=(size-1); i++) {

//        	int eventId = dc.getRoot().getStructure(i).getInteger(1);
        	int eventId = (int) dc.getRoot().getStructure(i).getValue(1);
//        	int eventId = (byte)dc.getRoot().getStructure(i).getValue(1);

        	if ( isOctetString(dc.getRoot().getStructure(i)) )
                eventTimeStamp = dc.getRoot().getStructure(i).getOctetString(0).toDate(timeZone);

            buildMeterEvent(meterEvents,eventTimeStamp,eventId);
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



	private void buildMeterEvent(List meterEvents, Date eventTimeStamp, int eventId) {

		int aloneEventId = eventId + 0x10000;

        if( ( eventId & 0x8000 ) == 0 ) {

            /* These events can be combined */
            if ((eventId & EVENT_STATUS_FATAL_ERROR)==EVENT_STATUS_FATAL_ERROR)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.FATAL_ERROR,"Device disturbance"));
            if ((eventId & EVENT_STATUS_DEVICE_CLOCK_RESERVE)==EVENT_STATUS_DEVICE_CLOCK_RESERVE)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status device clock reserve"));
            if ((eventId & EVENT_STATUS_VALUE_CORRUPT)==EVENT_STATUS_VALUE_CORRUPT)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status value corrupt"));
            if ((eventId & EVENT_STATUS_DAYLIGHT_CHANGE)==EVENT_STATUS_DAYLIGHT_CHANGE)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status daylight change"));
            if ((eventId & EVENT_STATUS_BILLING_RESET)==EVENT_STATUS_BILLING_RESET)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.BILLING_ACTION,"Billing action occured"));
            if ((eventId & EVENT_STATUS_DEVICE_CLOCK_CHANGED)==EVENT_STATUS_DEVICE_CLOCK_CHANGED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.SETCLOCK,"Set time"));
            if ((eventId & EVENT_STATUS_POWER_RETURNED)==EVENT_STATUS_POWER_RETURNED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.POWERUP,"PowerUp"));
            if ((eventId & EVENT_STATUS_POWER_FAILURE)==EVENT_STATUS_POWER_FAILURE)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.POWERDOWN,"PowerDown"));
            if ((eventId & EVENT_STATUS_VARIABLE_SET)==EVENT_STATUS_VARIABLE_SET)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status variable set"));
            if ((eventId & EVENT_STATUS_UNRELIABLE_OPERATING_CONDITIONS)==EVENT_STATUS_UNRELIABLE_OPERATING_CONDITIONS)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status unreliable operating conditions"));
            if ((eventId & EVENT_STATUS_END_OF_UNRELIABLE_OPERATING_CONDITIONS)==EVENT_STATUS_END_OF_UNRELIABLE_OPERATING_CONDITIONS)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status end of unreliable operating conditions"));
            if ((eventId & EVENT_STATUS_UNRELIABLE_EXTERNAL_CONTROL)==EVENT_STATUS_UNRELIABLE_EXTERNAL_CONTROL)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status unreliable external control"));
            if ((eventId & EVENT_STATUS_END_OF_UNRELIABLE_EXTERNAL_CONTROL)==EVENT_STATUS_END_OF_UNRELIABLE_EXTERNAL_CONTROL)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status end of unreliable external control"));
            if ((eventId & EVENT_STATUS_EVENTLOG_CLEARED)==EVENT_STATUS_EVENTLOG_CLEARED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.CLEAR_DATA,"Event status event log cleared"));
            if ((eventId & EVENT_STATUS_LOADPROFILE_CLEARED)==EVENT_STATUS_LOADPROFILE_CLEARED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.CLEAR_DATA,"Event status load profile cleared"));

        } else {

            /* These events occur alone */
            if (aloneEventId == EVENT_STATUS_L1_POWER_FAILURE)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.PHASE_FAILURE,"Event status L1 phase failure"));
            if (aloneEventId == EVENT_STATUS_L2_POWER_FAILURE)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.PHASE_FAILURE,"Event status L2 phase failure"));
            if (aloneEventId == EVENT_STATUS_L3_POWER_FAILURE)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.PHASE_FAILURE,"Event status L3 phase failure"));
            if (aloneEventId == EVENT_STATUS_L1_POWER_RETURNED )
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status end of L1 phase failure"));
            if (aloneEventId == EVENT_STATUS_L2_POWER_RETURNED )
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status end of L2 phase failure"));
            if (aloneEventId == EVENT_STATUS_L3_POWER_RETURNED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status end of L3 phase failure"));
            if (aloneEventId == EVENT_STATUS_METER_COVER_OPENED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status meter cover opened"));
            if (aloneEventId == EVENT_STATUS_TERMINAL_COVER_OPENED)
                meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event status terminal cover opened"));

        }

    } // private void buildMeterEvent(List meterEvents, Date eventTimeStamp, int eventId)


} // public class Logbook
