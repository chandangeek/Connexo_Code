package com.energyict.genericprotocolimpl.elster.ctr.events;

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

    private final GprsRequestFactory requestFactory;
    private CTRAbstractValue[][] eventRecords;
    private List<CTRAbstractValue[]> allEventRecords = new ArrayList();

    public CTRMeterEvent(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public List<MeterEvent> getMeterEvents(Date fromDate) throws CTRException {

        boolean notFound = true;
        int requestCounter = 0;
        int eventCounter = 0;
        int numberOfElements = getRequestFactory().queryEventArray(new Index_Q(0)).getNumberOfEvents().getIntValue();

        //Request the latest 6 events
        while (notFound && (allEventRecords.size() < numberOfElements)) {
            int index_Q = -1 * (-5 - (6 * requestCounter));
            eventRecords = getRequestFactory().queryEventArray(new Index_Q(index_Q)).getEvento_Short();
            
            eventCounter = 0;
            for (CTRAbstractValue[] event : eventRecords) {
                Date eventDate = getDateFromBytes(event);
                if (fromDate.after(eventDate)) {
                    notFound = false;
                    break;
                }
                allEventRecords.add(event);
                eventCounter++;
            }
            requestCounter++;
        }

        return convertToMeterEvents(allEventRecords);
    }

    private Date getDateFromBytes(CTRAbstractValue[] event) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, event[0].getIntValue() + 2000);
        cal.set(Calendar.MONTH, event[1].getIntValue() - 1);
        cal.set(Calendar.DAY_OF_MONTH, event[2].getIntValue());
        cal.set(Calendar.HOUR_OF_DAY, event[3].getIntValue());
        cal.set(Calendar.MINUTE, event[4].getIntValue());
        return cal.getTime();
    }

    private List<MeterEvent> convertToMeterEvents(List<CTRAbstractValue[]> allEventRecords) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (CTRAbstractValue[] eventRecord : allEventRecords) {

            int code = eventRecord[7].getIntValue();
            Date date = getDateFromBytes(eventRecord);
            date = fixDate(date);
            MeterEvent meterEvent;

            switch (code) {
                case 0x30:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Generic");
                    break;
                case 0x31:
                    meterEvent = new MeterEvent(date, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, "Over Limit");
                    break;
                case 0x32:
                    meterEvent = new MeterEvent(date, MeterEvent.MEASUREMENT_SYSTEM_ERROR, "Out of range");
                    break;
                case 0x33:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Programming");
                    break;
                case 0x34:
                    meterEvent = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, "Modification of a relevant parameter");
                    break;
                case 0x35:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "General fault");
                    break;
                case 0x36:
                    meterEvent = new MeterEvent(date, MeterEvent.POWERDOWN, "Primary supply OFF");
                    break;
                case 0x37:
                    meterEvent = new MeterEvent(date, MeterEvent.BATTERY_VOLTAGE_LOW, "Battery low");
                    break;
                case 0x38:
                    meterEvent = new MeterEvent(date, MeterEvent.SETCLOCK_AFTER, "Modify date & time");
                    break;
                case 0x3A:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Calculation error");
                    break;
                case 0x3B:
                    meterEvent = new MeterEvent(date, MeterEvent.CLEAR_DATA, "Memories reset");
                    break;
                case 0x3C:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Relevant seal deactivated");
                    break;
                case 0x3D:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Synchronizaztion error");
                    break;
                case 0x3E:
                    meterEvent = new MeterEvent(date, MeterEvent.EVENT_LOG_CLEARED, "Reset event queue");
                    break;
                case 0x3F:
                    meterEvent = new MeterEvent(date, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, "Day light saving time programming");
                    break;
                case 0x40:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Event buffer full");
                    break;
                case 0x41:
                    meterEvent = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, "Tariff scheme configuration");
                    break;
                case 0x42:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Activation of a new tariff scheme");
                    break;
                case 0x43:
                    meterEvent = new MeterEvent(date, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, "Download of new software");
                    break;
                case 0x44:
                    meterEvent = new MeterEvent(date, MeterEvent.FIRMWARE_ACTIVATED, "Activation of new software");
                    break;
                case 0x46:
                    meterEvent = new MeterEvent(date, MeterEvent.FRAUD_ATTEMPT_MBUS, "Fraud attempt");
                    break;
                case 0x47:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Change of status");
                    break;
                case 0x48:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Programming failed");
                    break;
                case 0x49:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Flow cut-off");
                    break;
                case 0x4A:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Pressure cut-off");
                    break;
                case 0x4B:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER, "Halt volume calculation at standard therm. cond.");
                    break;
                case 0x4C:
                    meterEvent = new MeterEvent(date, MeterEvent.CONFIGURATIONCHANGE, "Modification of security parameters");
                    break;
                case 0x4D:
                    meterEvent = new MeterEvent(date, MeterEvent.REPLACE_BATTERY_MBUS, "Replace batteries");
                    break;
                default:
                    meterEvent = new MeterEvent(date, MeterEvent.OTHER);
                    break;
            }
            if (checkDate(date)) {
                meterEvents.add(meterEvent);
            }
        }
        return meterEvents;
    }

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

    private boolean checkDate(Date date) {
        Calendar calCurrent = Calendar.getInstance();
        Date dateCurrent = calCurrent.getTime();
        return !date.after(dateCurrent);
    }

}
