package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;

public class EventsLog extends AbstractEvent {

    // Event log
    protected static final int EVENT_EVENT_LOG_CLEARED = 255;
    protected static final int EVENT_POWER_DOWN = 1;
    protected static final int EVENT_POWER_UP = 2;
    protected static final int EVENT_DAYLING_SAVING_TIME_CHANGE = 3;
    protected static final int EVENT_CLOCK_ADJUSTED_OLD = 4;
    protected static final int EVENT_CLOCK_ADJUSTED_NEW = 5;
    protected static final int EVENT_CLOCK_INVALID = 6;
    protected static final int EVENT_BATTERY_REPLACE = 7;
    protected static final int EVENT_BATTERY_VOLTAGE_LOW = 8;
    protected static final int EVENT_TOU_ACTIVATED = 9;
    protected static final int EVENT_ERROR_REGISTER_CLEARED = 10;
    protected static final int EVENT_ALARM_REGISTER_CLEARED = 11;
    protected static final int EVENT_PROGRAM_MEMORY_ERROR = 12;
    protected static final int EVENT_RAM_ERROR = 13;
    protected static final int EVENT_NV_MEMORY_ERROR = 14;
    protected static final int EVENT_WATCHDOG_ERROR = 15;
    protected static final int EVENT_MEASUREMENT_SYSTEM_ERROR = 16;
    protected static final int EVENT_FIRMWARE_READY_ACTIVATION = 17;
    protected static final int EVENT_FIRMWARE_ACTIVATED = 18;

    public EventsLog(DataContainer dc, final AXDRDateTimeDeviationType deviationType) {
        super(dc, deviationType);
    }

    public EventsLog(DataContainer dc) {
        super(dc);
    }

    /**
     * {@inheritDoc}
     */
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

        if (!ExtraEvents.extraEvents.containsKey(new Integer(eventId))) {
            switch (eventId) {
                case EVENT_EVENT_LOG_CLEARED: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.EVENT_LOG_CLEARED, eventId, "Event log profile cleared"));
                }
                break;
                case EVENT_POWER_DOWN: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId, "Powerdown"));
                }
                break;
                case EVENT_POWER_UP: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.POWERUP, eventId, "Powerup"));
                }
                break;
                case EVENT_DAYLING_SAVING_TIME_CHANGE: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, eventId, "Daylight saving time changed (enabled/disabled)"));
                }
                break;
                case EVENT_CLOCK_ADJUSTED_OLD: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.SETCLOCK_BEFORE, eventId, "Clock adjusted (old dateTime)"));
                }
                break;
                case EVENT_CLOCK_ADJUSTED_NEW: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId, "Clock adjusted (new dateTime)"));
                }
                break;
                case EVENT_CLOCK_INVALID: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.CLOCK_INVALID, eventId, "Clock invalid, power reserve may be exhausted"));
                }
                break;
                case EVENT_BATTERY_REPLACE: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY, eventId, "Replace battery"));
                }
                break;
                case EVENT_BATTERY_VOLTAGE_LOW: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Battery voltage low"));
                }
                break;
                case EVENT_TOU_ACTIVATED: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.TOU_ACTIVATED, eventId, "Passive TOU has been activated"));
                }
                break;
                case EVENT_ERROR_REGISTER_CLEARED: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.ERROR_REGISTER_CLEARED, eventId, "Error register cleared"));
                }
                break;
                case EVENT_ALARM_REGISTER_CLEARED: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.ALARM_REGISTER_CLEARED, eventId, "Alarm register cleared"));
                }
                break;
                case EVENT_PROGRAM_MEMORY_ERROR: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.PROGRAM_MEMORY_ERROR, eventId, "Physical or logical error in the Program memory"));
                }
                break;
                case EVENT_RAM_ERROR: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.RAM_MEMORY_ERROR, eventId, "Physical or logical error in RAM"));
                }
                break;
                case EVENT_NV_MEMORY_ERROR: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.NV_MEMORY_ERROR, eventId, "Physical or logical error in non volatile memory"));
                }
                break;
                case EVENT_WATCHDOG_ERROR: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.WATCHDOG_ERROR, eventId, "Watchdog reset or a hardware reset of the microcontroller"));
                }
                break;
                case EVENT_MEASUREMENT_SYSTEM_ERROR: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Physical or logical error in the measurement system"));
                }
                break;
                case EVENT_FIRMWARE_READY_ACTIVATION: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId, "New firmware has been successfully downloaded and verified, i.e. it is ready for activation"));
                }
                break;
                case EVENT_FIRMWARE_ACTIVATED: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.FIRMWARE_ACTIVATED, eventId, "New firmware has been activated"));
                }
                break;
                default: {
                    meterEvents.add(createNewStandardLogbookEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
                }
            }
        } else {
            meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
        }
    }

    public MeterEvent createNewStandardLogbookEvent(Date eventTimeStamp, int meterEvent, int eventId, String message) {
        return new MeterEvent(eventTimeStamp, meterEvent, eventId, message, EventLogbookId.StandardEventLogbook.eventLogId(), 0);
    }
}
