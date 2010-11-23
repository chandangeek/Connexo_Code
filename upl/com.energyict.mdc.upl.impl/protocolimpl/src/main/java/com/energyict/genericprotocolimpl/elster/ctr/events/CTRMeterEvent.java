package com.energyict.genericprotocolimpl.elster.ctr.events;

import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.Index_Q;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 20-okt-2010
 * Time: 11:19:11
 */
public class CTRMeterEvent {

    public final static String EVENT_DESCRIPTION_30 = "Generic";
    public final static String EVENT_DESCRIPTION_31 = "Over Limit";
    public final static String EVENT_DESCRIPTION_32 = "Out of range";
    public final static String EVENT_DESCRIPTION_33 = "Programming";
    public final static String EVENT_DESCRIPTION_34 = "Modification of a relevant parameter";
    public final static String EVENT_DESCRIPTION_35 = "General fault";
    public final static String EVENT_DESCRIPTION_36 = "Primary supply OFF";
    public final static String EVENT_DESCRIPTION_37 = "Battery low";
    public final static String EVENT_DESCRIPTION_38 = "Modify date & time";
    public final static String EVENT_DESCRIPTION_3A = "Calculation error";
    public final static String EVENT_DESCRIPTION_3B = "Memories reset";
    public final static String EVENT_DESCRIPTION_3C = "Relevant seal deactivated";
    public final static String EVENT_DESCRIPTION_3D = "Synchronization error";
    public final static String EVENT_DESCRIPTION_3E = "Reset event queue";
    public final static String EVENT_DESCRIPTION_3F = "Day light saving time programming";
    public final static String EVENT_DESCRIPTION_40 = "Event buffer full";
    public final static String EVENT_DESCRIPTION_41 = "Tariff scheme configuration";
    public final static String EVENT_DESCRIPTION_42 = "Activation of a new tariff scheme";
    public final static String EVENT_DESCRIPTION_43 = "Download of new software";
    public final static String EVENT_DESCRIPTION_44 = "Activation of new software";
    public final static String EVENT_DESCRIPTION_46 = "Fraud attempt";
    public final static String EVENT_DESCRIPTION_47 = "Change of status";
    public final static String EVENT_DESCRIPTION_48 = "Programming failed";
    public final static String EVENT_DESCRIPTION_49 = "Flow cut-off";
    public final static String EVENT_DESCRIPTION_4A = "Pressure cut-off";
    public final static String EVENT_DESCRIPTION_4B = "Halt volume calculation at standard therm. cond.";
    public final static String EVENT_DESCRIPTION_4C = "Modification of security parameters";
    public final static String EVENT_DESCRIPTION_4D = "Replace batteries";


    private final GprsRequestFactory requestFactory;
    private CTRAbstractValue[][] eventRecords;
    private List<CTRAbstractValue[]> allEventRecords = new ArrayList();
    private final TimeZone timeZone;

    public CTRMeterEvent(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        this.timeZone = requestFactory.getTimeZone();
    }

    public CTRMeterEvent(TimeZone timeZone) {
        this.requestFactory = null;
        this.timeZone = timeZone;
    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * Gets all event records from the meter, that happened after a certain date
     *
     * @param fromDate: reference date
     * @return list of protocol events
     * @throws CTRException: if the request factory was not found
     */
    public List<MeterEvent> getMeterEvents(Date fromDate) throws CTRException {
        if (getRequestFactory() == null) {
            throw new CTRException("Error, the request factory was not found");
        }
        if (fromDate == null) {
            fromDate = ParseUtils.getClearLastDayDate(getTimeZone());
        }

        boolean notFound = true;
        int requestCounter = 0;
        int numberOfElements = getRequestFactory().queryEventArray(new Index_Q(0)).getNumberOfEvents().getIntValue();

        //Request the latest 6 events
        while (notFound && (allEventRecords.size() < numberOfElements)) {
            int index_Q = ((6 * requestCounter));
            eventRecords = getRequestFactory().queryEventArray(new Index_Q(index_Q)).getEvento_Short();
            for (CTRAbstractValue[] event : eventRecords) {
                Date eventDate = getDateFromBytes(event);
                if (fromDate.after(eventDate) || !isValidDate(eventDate)) {
                    notFound = false;
                    break;
                }
                allEventRecords.add(event);
            }
            requestCounter++;
        }

        return convertToMeterEvents(allEventRecords);
    }

    private TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Creates a Date object from a given CTR Event object
     *
     * @param event: the CTR Event object
     * @return date object
     */
    private Date getDateFromBytes(CTRAbstractValue[] event) {
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Calendar.YEAR, event[0].getIntValue() + 2000);
        cal.set(Calendar.MONTH, event[1].getIntValue() - 1);
        cal.set(Calendar.DAY_OF_MONTH, event[2].getIntValue());
        cal.set(Calendar.HOUR_OF_DAY, event[3].getIntValue());
        cal.set(Calendar.MINUTE, event[4].getIntValue());
        return cal.getTime();
    }

    /**
     * Converts a list of CTR Meter events to a list of common protocol events
     *
     * @param allEventRecords: a list of CTR Meter events
     * @return a list of common protocol events
     */
    public List<MeterEvent> convertToMeterEvents(List<CTRAbstractValue[]> allEventRecords) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (CTRAbstractValue[] eventRecord : allEventRecords) {

            int code = eventRecord[7].getIntValue();
            Date date = getDateFromBytes(eventRecord);
            date = fixDate(date);
            MeterEvent meterEvent;

            switch (code) {
                case 0x30:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_30);
                    break;
                case 0x31:
                    meterEvent = new MeterEvent(date, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, code, EVENT_DESCRIPTION_31);
                    break;
                case 0x32:
                    meterEvent = new MeterEvent(date, MeterEvent.MEASUREMENT_SYSTEM_ERROR, code, EVENT_DESCRIPTION_32);
                    break;
                case 0x33:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_33);
                    break;
                case 0x34:
                    meterEvent = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, code, EVENT_DESCRIPTION_34);
                    break;
                case 0x35:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_35);
                    break;
                case 0x36:
                    meterEvent = new MeterEvent(date, MeterEvent.POWERDOWN, code, EVENT_DESCRIPTION_36);
                    break;
                case 0x37:
                    meterEvent = new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, code, EVENT_DESCRIPTION_37);
                    break;
                case 0x38:
                    meterEvent = new MeterEvent(date, MeterEvent.SETCLOCK_AFTER, code, EVENT_DESCRIPTION_38);
                    break;
                case 0x3A:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_3A);
                    break;
                case 0x3B:
                    meterEvent = new MeterEvent(date, MeterEvent.CLEAR_DATA, code, EVENT_DESCRIPTION_3B);
                    break;
                case 0x3C:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_3C);
                    break;
                case 0x3D:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_3D);
                    break;
                case 0x3E:
                    meterEvent = new MeterEvent(date, MeterEvent.EVENT_LOG_CLEARED, code, EVENT_DESCRIPTION_3E);
                    break;
                case 0x3F:
                    meterEvent = new MeterEvent(date, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, code, EVENT_DESCRIPTION_3F);
                    break;
                case 0x40:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_40);
                    break;
                case 0x41:
                    meterEvent = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, code, EVENT_DESCRIPTION_41);
                    break;
                case 0x42:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_42);
                    break;
                case 0x43:
                    meterEvent = new MeterEvent(date, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, code, EVENT_DESCRIPTION_43);
                    break;
                case 0x44:
                    meterEvent = new MeterEvent(date, MeterEvent.FIRMWARE_ACTIVATED, code, EVENT_DESCRIPTION_44);
                    break;
                case 0x46:
                    meterEvent = new MeterEvent(date, MeterEvent.TAMPER, code, EVENT_DESCRIPTION_46);
                    break;
                case 0x47:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_47);
                    break;
                case 0x48:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_48);
                    break;
                case 0x49:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_49);
                    break;
                case 0x4A:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_4A);
                    break;
                case 0x4B:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, code, EVENT_DESCRIPTION_4B);
                    break;
                case 0x4C:
                    meterEvent = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, code, EVENT_DESCRIPTION_4C);
                    break;
                case 0x4D:
                    meterEvent = new MeterEvent(date, MeterEvent.REPLACE_BATTERY, code, EVENT_DESCRIPTION_4D);
                    break;
                default:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER);
                    break;
            }
            if (isValidDate(date)) {
                meterEvents.add(meterEvent);
            }
        }
        return meterEvents;
    }

    /**
     * Checks if the hours / minutes have an overflow. This indicates that a time shift is in progress.
     *
     * @param date: the date that needs to be checked
     * @return the real date without the overflow
     */
    private Date fixDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);


        if (hour > 23) {
            hour -= 30;
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minutes > 59) {
            minutes -= 60;
            cal.set(Calendar.MINUTE, minutes);
        }

        return cal.getTime();
    }

    /**
     * Checks if the event's date is valid (data has to be in the past)
     *
     * @param date: the date that needs to be checked
     * @return boolean, whether or not the date is valid
     */
    private boolean isValidDate(Date date) {
        Calendar calCurrent = Calendar.getInstance();
        Date dateCurrent = calCurrent.getTime();
        return !date.after(dateCurrent);
    }

}
