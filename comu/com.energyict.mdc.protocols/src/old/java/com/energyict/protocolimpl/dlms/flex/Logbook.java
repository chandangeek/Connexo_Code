/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Logbook.java
 *
 * Created on 22 januari 2008
 */

package com.energyict.protocolimpl.dlms.flex;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author gna
 */
public class Logbook {

    private static final int DEBUG = 0;

    TimeZone timeZone;

    private static final int EVENT_PARAMETER_CHANGED = 1;
    private static final int EVENT_EVENT_LOG_CLEARED = 4;
    private static final int EVENT_DAYLIGHT_SAVING_TIME_CHANGE = 9;
    private static final int EVENT_CLOCK_OLD = 10;
    private static final int EVENT_CLOCK_NEW = 11;
    private static final int EVENT_UNDER_VOLTAGE_L1 = 17;
    private static final int EVENT_UNDER_VOLTAGE_L2 = 18;
    private static final int EVENT_UNDER_VOLTAGE_L3 = 19;
    private static final int EVENT_OVER_VOLTAGE_L1 = 20;
    private static final int EVENT_OVER_VOLTAGE_L2 = 21;
    private static final int EVENT_OVER_VOLTAGE_L3 = 22;
    private static final int EVENT_POWER_DOWN = 23;
    private static final int EVENT_POWER_UP_AFTER_SHORT_DOWN = 24;
    private static final int EVENT_ERROR_REGISTER_CLEARED = 45;
    private static final int EVENT_MISSING_VOLTAGE_L1 = 49;
    private static final int EVENT_MISSING_VOLTAGE_L2 = 50;
    private static final int EVENT_MISSING_VOLTAGE_L3 = 51;
    private static final int EVENT_CLOCK_INVALID = 66;
    private static final int EVENT_METER_COMMUNICATION_ERROR = 75;
    private static final int EVENT_TIME_BASE_ACCESS_ERROR = 76;
    private static final int EVENT_FLASH_MEMORY_ACCESS_ERROR = 77;
    private static final int EVENT_REMOTE_COMMUNICATION_MODULE_ERROR = 79;
    private static final int EVENT_PROGRAM_MEMORY_CHECKSUM_ERROR = 81;
    private static final int EVENT_BACKUP_DATA_CHECKSUM_ERROR = 82;
    private static final int EVENT_PARAMETER_CHECKSUM_ERROR = 83;
    private static final int EVENT_PROFILE_DATA_CHECKSUM_ERROR = 84;
    private static final int EVENT_STARTUP_SEQUENCE_INVALID = 89;
    private static final int EVENT_METER_DATA_ERROR = 90;
    private static final int EVENT_TERMINAL_COVER_REMOVED = 133;
    private static final int EVENT_STRONG_DC_FIELD_DETECTED = 134;
    private static final int EVENT_BILLING_VALUES_PROFILE_CLEARED = 158;
    private static final int EVENT_ENERGY_VALUES_PROFILE_CLEARED = 159;
    private static final int EVENT_POWER_UP_AFTER_LONG_DOWN = 160;
    private static final int EVENT_METER_CHANGED = 161;
    private static final int EVENT_TOU_ACTIVATED = 162;
    private static final int EVENT_METER_DATA_OK = 163;
    private static final int EVENT_METER_COMMUNICATION_OK = 164;
    private static final int EVENT_REMOTE_COMMUNICATION_OK = 165;
    private static final int EVENT_FIRMWARE_UPDATED = 166;
    private static final int EVENT_MAXIMUM_DEMAND_EXCEEDED = 175;
    private static final int EVENT_VOLTAGE_L1_OK = 176;
    private static final int EVENT_VOLTAGE_L2_OK = 177;
    private static final int EVENT_VOLTAGE_L3_OK = 178;
    private static final int EVENT_NO_UNDER_VOLTAGE_L1_ANYMORE = 179;
    private static final int EVENT_NO_UNDER_VOLTAGE_L2_ANYMORE = 180;
    private static final int EVENT_NO_UNDER_VOLTAGE_L3_ANYMORE = 181;
    private static final int EVENT_NO_OVER_VOLTAGE_L1_ANYMORE = 182;
    private static final int EVENT_NO_OVER_VOLTAGE_L2_ANYMORE = 183;
    private static final int EVENT_NO_OVER_VOLTAGE_L3_ANYMORE = 184;
    private static final int EVENT_MAXIMUM_DEMAND_OK = 186;
    private static final int EVENT_TERMINAL_COVER_CLOSED = 187;
    private static final int EVENT_NO_STRONG_DC_FIELD_ANYMORE = 188;
    private static final int EVENT_DISCONNECTOR_LOG_CLEARED = 189;
    private static final int EVENT_EXTERNAL_ALERT_OCCURED = 190;
    private static final int EVENT_EXTERNAL_ALERT_DISAPPEARED = 191;




    /** Creates a new instance of Logbook */
    public Logbook(TimeZone timeZone) {
        this.timeZone=timeZone;
    }

    public List getMeterEvents(DataContainer dc) {
        List meterEvents = new ArrayList(); // of type MeterEvent
        int size = dc.getRoot().getNrOfElements();
        for (int i = (size-1);i>=0;i--) {
            Date eventTimeStamp = dc.getRoot().getStructure(i).getOctetString(0).toDate(timeZone);
            int eventId = dc.getRoot().getStructure(i).getInteger(2);
            int internalStatus = dc.getRoot().getStructure(i).getInteger(1);
            buildMeterEvent(meterEvents,eventTimeStamp,eventId,internalStatus);
            if (DEBUG >= 1) {
				System.out.println("KV_DEBUG> eventId="+eventId+", eventTimeStamp="+eventTimeStamp);
			}
        }
        return meterEvents;
    }

    private void buildMeterEvent(List meterEvents, Date eventTimeStamp, int eventId, int internalStatus) {


        /* These events can be combined */
        if (eventId == EVENT_PARAMETER_CHANGED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Parameter changed; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_EVENT_LOG_CLEARED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Event log cleared; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_DAYLIGHT_SAVING_TIME_CHANGE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Daylicht saving changed; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_CLOCK_OLD) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.SETCLOCK_BEFORE,"Setclock Old; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_CLOCK_NEW) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.SETCLOCK_AFTER,"Setclock new; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_UNDER_VOLTAGE_L1) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Under voltage L1; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_UNDER_VOLTAGE_L2) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Under voltage L2; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_UNDER_VOLTAGE_L3) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Under voltage L3; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_OVER_VOLTAGE_L1) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Over voltage L1; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_OVER_VOLTAGE_L2) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Over voltage L2; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_OVER_VOLTAGE_L3) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Over voltage L3; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_POWER_DOWN) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.POWERDOWN,"Power Down; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_POWER_UP_AFTER_SHORT_DOWN) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.POWERUP,"Power up after short PD; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_ERROR_REGISTER_CLEARED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.CLEAR_DATA,"Error register cleared; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_MISSING_VOLTAGE_L1) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.PHASE_FAILURE,"Missing voltage L1; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_MISSING_VOLTAGE_L2) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.PHASE_FAILURE,"Missing voltage L2; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_MISSING_VOLTAGE_L3) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.PHASE_FAILURE,"Missing voltage L3; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_CLOCK_INVALID) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Clock invalid; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_METER_COMMUNICATION_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Meter communication error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_TIME_BASE_ACCESS_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Time base access error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_FLASH_MEMORY_ACCESS_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Flash memory access error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_REMOTE_COMMUNICATION_MODULE_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Remote communication module error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_PROGRAM_MEMORY_CHECKSUM_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Memory checksum error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_BACKUP_DATA_CHECKSUM_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Backup data checksum error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_PARAMETER_CHECKSUM_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Parameter checksum error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_PROFILE_DATA_CHECKSUM_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Profile data checksum error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_STARTUP_SEQUENCE_INVALID) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.APPLICATION_ALERT_START,"Startup seq. invalid; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_METER_DATA_ERROR) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Meter data error; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_TERMINAL_COVER_REMOVED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Terminal cover removed; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_STRONG_DC_FIELD_DETECTED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Strong DC field detected; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_BILLING_VALUES_PROFILE_CLEARED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.CLEAR_DATA,"Billing profile cleared; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_ENERGY_VALUES_PROFILE_CLEARED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.CLEAR_DATA,"Energy values profile cleared; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_POWER_UP_AFTER_LONG_DOWN) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.POWERUP,"Power up after long PD; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_METER_CHANGED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Meter changed; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_TOU_ACTIVATED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"TOU activated; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_METER_DATA_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Meter data OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_METER_COMMUNICATION_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Meter communication OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_REMOTE_COMMUNICATION_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Remote communication OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_FIRMWARE_UPDATED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Firmware updated; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_MAXIMUM_DEMAND_EXCEEDED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.REGISTER_OVERFLOW,"Maximum demand exceeded; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_VOLTAGE_L1_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Voltage L1 OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_VOLTAGE_L2_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Voltage L2 OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_VOLTAGE_L3_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"Voltage L3 OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_UNDER_VOLTAGE_L1_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"No more under voltage L1; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_UNDER_VOLTAGE_L2_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"No more under voltage L2; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_UNDER_VOLTAGE_L3_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"No more under voltage L3; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_OVER_VOLTAGE_L1_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"No more over voltage L1; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_OVER_VOLTAGE_L2_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"No more over voltage L2; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_OVER_VOLTAGE_L3_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.METER_ALARM,"No more over voltage L3; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_MAXIMUM_DEMAND_OK) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Maximum demand OK; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_TERMINAL_COVER_CLOSED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"Terminal cover closed; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_NO_STRONG_DC_FIELD_ANYMORE) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"No strong DC field anymore; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_DISCONNECTOR_LOG_CLEARED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.CLEAR_DATA,"Disconnector log cleared; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_EXTERNAL_ALERT_OCCURED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"External alert occured; Status: " + Integer.toString(internalStatus)));
		}

        if (eventId == EVENT_EXTERNAL_ALERT_DISAPPEARED) {
			meterEvents.add(new MeterEvent(eventTimeStamp,MeterEvent.OTHER,"External alert disappeared, Status: " + Integer.toString(internalStatus)));
		}

    } // private void buildMeterEvent(List meterEvents, Date eventTimeStamp, int eventId)

} // public class Logbook
