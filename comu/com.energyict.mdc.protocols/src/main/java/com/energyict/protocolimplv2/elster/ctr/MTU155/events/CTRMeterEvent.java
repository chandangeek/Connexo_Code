package com.energyict.protocolimplv2.elster.ctr.MTU155.events;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.MeterEventUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.DeviceStatus;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRBINValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Index_Q;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.energyict.protocolimpl.utils.ProtocolTools.getHexStringFromBytes;
import static com.energyict.protocolimpl.utils.ProtocolTools.getIntFromByte;

/**
 * Copyrights EnergyICT
 * Date: 20-okt-2010
 * Time: 11:19:11
 */
public class CTRMeterEvent {

    protected final RequestFactory requestFactory;
    protected CTRAbstractValue[][] eventRecords;
    protected List<CTRAbstractValue[]> allEventRecords = new ArrayList();
    protected final TimeZone timeZone;

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

    protected List<MeterEvent> avoidDuplicateTimeStamps(List<MeterEvent> meterEvents) {
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
                uniqueItems.add(MeterEventUtils.changeEventDate(meterEvent, newEventDate));
                occurences.put(meterEventTime, count);
            }
        }
        return uniqueItems;
    }

    protected TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Creates a Date object from a given CTR Event object
     *
     * @param event: the CTR Event object
     * @return date object
     */
    public Date getDateFromBytes(CTRAbstractValue[] event) {
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Calendar.YEAR, event[0].getIntValue() + 2000);
        cal.set(Calendar.MONTH, (event[1].getIntValue() % 16) - 1);     //  Bits 0-3 contain the month - bits 4-7 indicate the operator
        cal.set(Calendar.DAY_OF_MONTH, (event[2].getIntValue() % 32));  //  Bits 0-4 contain the day - bits 5-7 indicate the profile
        cal.set(Calendar.HOUR_OF_DAY, (event[3].getIntValue()) % 30);   // hour + 30, if in DST
        cal.set(Calendar.MINUTE, (event[4].getIntValue()) % 60);        // minutes + 60, if time shift is in progress
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
                MeterEvent meterEvent = EventMapping.getMeterEventFromDeviceCode(code, date);
                meterEvent = MeterEventUtils.appendToEventMessage(meterEvent, getAdditionalInfo(eventRecord));
                meterEvent = MeterEventUtils.appendToEventMessage(meterEvent, " [" + seq + "]");
                if (isValidDate(date)) {
                    meterEvents.add(meterEvent);
                }
            }
        }
        return meterEvents;
    }

    protected String getAdditionalInfo(CTRAbstractValue[] eventRecord) {
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
        } else if (code == 0x82) {
            try {
                String info;
                CTRObjectFactory factory = new CTRObjectFactory();
                AttributeType attributeType = new AttributeType();
                attributeType.setHasQualifier(true);
                attributeType.setHasValueFields(true);

                byte[] rawData = ProtocolTools.concatByteArrays(eventRecord[8].getBytes(), eventRecord[9].getBytes());
                AbstractCTRObject object = factory.parse(rawData, 0, attributeType, "2.1.0");
                CTRBINValue tot_Vb = (CTRBINValue) object.getValue(0);
                Unit unit = tot_Vb.getUnit();
                BigDecimal amount = tot_Vb.getValue();
                amount = amount.movePointRight(object.getQlf().getKmoltFactor());

                info = " [Tot_Vb: " + amount + " " + unit;
                info += " - ";

                attributeType.setHasQualifier(false);
                rawData = eventRecord[10].getBytes();
                object = factory.parse(rawData, 0, attributeType, "2.0.0");
                CTRBINValue tot_Vm = (CTRBINValue) object.getValue(0);
                unit = tot_Vm.getUnit();
                amount = tot_Vm.getValue();

                info += "Tot_Vm: " + amount + " " + unit +"]";
                return info;
            } catch (CTRParsingException e) {
                return "";
            }
        }
        return "";
    }

    /**
     * Checks if the event's date is valid (data has to be in the past)
     *
     * @param date: the date that needs to be checked
     * @return boolean, whether or not the date is valid
     */
    protected boolean isValidDate(Date date) {
        Calendar calCurrent = Calendar.getInstance();
        Date dateCurrent = calCurrent.getTime();
        return !date.after(dateCurrent);
    }

    /**
     * Retrieve all raw CTR event records, relating to the installation archive (having event code 0x82)
     * @return a list of all events, who belong to the installation archive
     */
    public List<CTRAbstractValue[]> getInstallationArchiveEventRecords() {
        List<CTRAbstractValue[]> installationArchiveRecords = new ArrayList();
        for (CTRAbstractValue[] each : allEventRecords) {
            if (each[7].getIntValue() == 0x82) {
                installationArchiveRecords.add(each);
            }
        }
        return installationArchiveRecords;
    }
}
