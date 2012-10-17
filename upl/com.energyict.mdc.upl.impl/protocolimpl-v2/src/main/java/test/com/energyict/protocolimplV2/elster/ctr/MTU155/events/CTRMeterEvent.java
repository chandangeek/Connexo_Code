package test.com.energyict.protocolimplV2.elster.ctr.MTU155.events;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.RequestFactory;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.exception.CTRParsingException;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.info.DeviceStatus;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.object.field.CTRObjectID;
import test.com.energyict.protocolimplV2.elster.ctr.MTU155.structure.field.Index_Q;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.energyict.protocolimpl.utils.MeterEventUtils.appendToEventMessage;
import static com.energyict.protocolimpl.utils.MeterEventUtils.changeEventDate;
import static com.energyict.protocolimpl.utils.ProtocolTools.getHexStringFromBytes;
import static com.energyict.protocolimpl.utils.ProtocolTools.getIntFromByte;

/**
 * Copyrights EnergyICT
 * Date: 20-okt-2010
 * Time: 11:19:11
 */
public class CTRMeterEvent {

    private final RequestFactory requestFactory;
    private CTRAbstractValue[][] eventRecords;
    private List<CTRAbstractValue[]> allEventRecords = new ArrayList();
    private final TimeZone timeZone;

    public CTRMeterEvent(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        this.timeZone = requestFactory.getTimeZone();
    }

    public CTRMeterEvent(TimeZone timeZone) {
        this.requestFactory = null;
        this.timeZone = timeZone;
    }

    public RequestFactory getRequestFactory() {
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
            fromDate = ProtocolTools.getDateFromYYYYMMddhhmmss("1990-01-01 00:00:00");
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
                if (fromDate.after(eventDate)) {
                    notFound = false;
                    break;
                }
                if (isValidDate(eventDate)) {
                    allEventRecords.add(event);
                }
            }
            requestCounter++;
        }

        List<MeterEvent> meterEvents = convertToMeterEvents(allEventRecords);
        return avoidDuplicateTimeStamps(meterEvents);
    }

    private List<MeterEvent> avoidDuplicateTimeStamps(List<MeterEvent> meterEvents) {
        Collections.sort(
                meterEvents,
                new Comparator<MeterEvent>() {
                    public int compare(MeterEvent meterEvent1, MeterEvent meterEvent2) {
                        String[] me1 = meterEvent1.getMessage().split("\\[");
                        String[] me2 = meterEvent2.getMessage().split("\\[");
                        return me1[me1.length - 1].compareTo(me2[me2.length - 1]);
                    }
                }
        );

        List<MeterEvent> uniqueItems = new ArrayList<MeterEvent>();
        Map<Date, Integer> occurences = new HashMap<Date, Integer>();
        for (MeterEvent meterEvent : meterEvents) {
            Date meterEventTime = meterEvent.getTime();
            Integer count = occurences.get(meterEventTime);
            if (count == null) {
                occurences.put(meterEventTime, 0);
                uniqueItems.add(meterEvent);
            } else {
                count++;
                Date newEventDate = new Date(meterEvent.getTime().getTime() + (1000 * count));
                uniqueItems.add(changeEventDate(meterEvent, newEventDate));
                occurences.put(meterEventTime, count);
            }
        }
        return uniqueItems;
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
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
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
            int seq = eventRecord[5].getIntValue();
            int code = eventRecord[7].getIntValue();
            if ((seq != 0) && (code != 0)) {
                Date date = getDateFromBytes(eventRecord);
                date = fixDate(date);
                MeterEvent meterEvent = EventMapping.getMeterEventFromDeviceCode(code, date);
                meterEvent = appendToEventMessage(meterEvent, getAdditionalInfo(eventRecord));
                meterEvent = appendToEventMessage(meterEvent, " [" + seq + "]");
                if (isValidDate(date)) {
                    meterEvents.add(meterEvent);
                }
            }
        }
        return meterEvents;
    }

    private String getAdditionalInfo(CTRAbstractValue[] eventRecord) {
        int code = eventRecord[7].getIntValue();
        byte[] add1 = eventRecord[9].getBytes();
        byte[] add2 = eventRecord[10].getBytes();
        if (code == 0x38) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, getIntFromByte(add1[0]) + 2000);
            cal.set(Calendar.MONTH, getIntFromByte(add1[1]) - 1);
            cal.set(Calendar.DAY_OF_MONTH, getIntFromByte(add1[2]));
            cal.set(Calendar.HOUR_OF_DAY, getIntFromByte(add2[0]));
            cal.set(Calendar.MINUTE, getIntFromByte(add2[1]));
            cal.set(Calendar.SECOND, getIntFromByte(add2[2]));
            cal.set(Calendar.MILLISECOND, 0);
            return " [to '" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(cal.getTime()) + "']";
        } else if (code == 0x47) {
            String oldSD = DeviceStatus.fromStatusCode(getIntFromByte(add2[2])).getDescription();
            String newSD = DeviceStatus.fromStatusCode(getIntFromByte(add2[3])).getDescription();
            return " [from '" + oldSD + "' to '" + newSD + "']";
        } else if (code == 0x34) {
            String objectID;
            try {
                objectID = new CTRObjectID().parse(add1, 0).toString();
            } catch (CTRParsingException e) {
                objectID = getHexStringFromBytes(add1);
            }
            return " ["+ objectID +"]";
        } else if (code == 0x4C) {
            int psCode = getIntFromByte(add2[0]);
            switch (psCode) {
                case 0x01: return " [Received secret command]";
                case 0x02: return " [KeyC]";
                case 0x03: return " [KeyT]";
                case 0x04: return " [KeyF]";
                case 0x05: return " [Password]";
                default: return " ["+ProtocolTools.getHexStringFromBytes(add2)+"]";
            }
        }
        return "";
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
