package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 15/09/2014 - 17:33
 */
public class EDISStatus {

    public static List<MeterEvent> getAllMeterEventsCorrespondingToEDISStatus(int edisStatus, Date dateTime) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        Calendar eventTime = Calendar.getInstance();
        eventTime.setTime(dateTime);

        if ((edisStatus & 0x01) == 0x01) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.FATAL_ERROR, "Fatal error"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x02) == 0x02) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.BATTERY_VOLTAGE_LOW, "Power reserve exhausted"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x04) == 0x04) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Invalid measured value"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x08) == 0x08) {
            // Daylight saving active
        }
        if ((edisStatus & 0x10) == 0x10) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.METER_ALARM, "Billing period reset or operational indication"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x20) == 0x20) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.SETCLOCK, "Clock adjusted"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x40) == 0x40) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.POWERUP, "Power up"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x80) == 0x80) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.POWERDOWN, "Power down"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x2000) == 0x2000) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.EVENT_LOG_CLEARED, "Event log cleared"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x4000) == 0x4000) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.CLEAR_DATA, "Load profile cleared"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x8000) == 0x8000) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.SETCLOCK_BEFORE, "Clock adjusted (old dateTime)"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x200000) == 0x200000) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.CLEAR_DATA, "Energy registers cleared"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x1000000) == 0x1000000) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Current without voltage"));
            eventTime.add(Calendar.SECOND, 1);
        }
        if ((edisStatus & 0x80000000) == 0x80000000) {
            meterEvents.add(new MeterEvent(eventTime.getTime(), MeterEvent.OTHER, "An alert has occurred during the capture period"));
            eventTime.add(Calendar.SECOND, 1);
        }

        return meterEvents;
    }
}