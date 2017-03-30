/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class StandardEventLog extends AbstractEvent {

    public StandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(dc, timeZone);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 1:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERDOWN, eventId, "Power down"));
                break;
            case 2:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.POWERUP, eventId, "Power up"));
                break;
            case 3:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, eventId, "Daylight saving time enabled or disabled"));
                break;
            case 4:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_BEFORE, eventId, "Clock adjusted (old date/time)"));
                break;
            case 5:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK_AFTER, eventId, "Clock adjusted (new date/time)"));
                break;
            case 6:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CLOCK_INVALID, eventId, "Clock invalid"));
                break;
            case 7:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY, eventId, "Replace Battery"));
                break;
            case 8:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Battery voltage low"));
                break;
            case 9:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.TOU_ACTIVATED, eventId, "TOU activated"));
                break;
            case 10:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ERROR_REGISTER_CLEARED, eventId, "Error register cleared"));
                break;
            case 11:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ALARM_REGISTER_CLEARED, eventId, "Alarm register cleared"));
                break;
            case 12:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PROGRAM_MEMORY_ERROR, eventId, "Program memory error"));
                break;
            case 13:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.RAM_MEMORY_ERROR, eventId, "RAM  error"));
                break;
            case 14:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NV_MEMORY_ERROR, eventId, "NV memory error"));
                break;
            case 15:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.WATCHDOG_ERROR, eventId, "Watchdog error"));
                break;
            case 16:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Measurement system error"));
                break;
            case 17:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId, "Firmware ready for activation"));
                break;
            case 18:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_ACTIVATED, eventId, "Firmware activated"));
                break;
            case 19:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Passive TOU programmed"));
                break;
            case 47:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "One or more parameters changed"));
                break;
            case 48:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, "Global key(s) changed"));
                break;
            case 51:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "FW verification failed"));
                break;
            case 88:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Phase sequence reversal"));
                break;
            case 89:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Missing neutral"));
                break;
            case 254:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOADPROFILE_CLEARED, eventId, "E-meter load profile cleared"));
                break;
            case 255:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, "Standard event log cleared"));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
        }
    }
}