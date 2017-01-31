/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.events;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DlmsSession;
import com.energyict.protocolimpl.dlms.prime.PrimeProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PrimeEventLogs {

    private final PrimeBasicEventLog[] eventLogs;
    private final DlmsSession session;

    public PrimeEventLogs(DlmsSession session, PrimeProperties properties) {
        this.session = session;
        ObisCode eventLogBookObiscode = properties.getEventLogBookObiscode();
        if (eventLogBookObiscode != null) {
            //read out one specific logbook
            eventLogs = new PrimeBasicEventLog[]{new PrimeBasicEventLog(session, eventLogBookObiscode, getGroupId(eventLogBookObiscode), getLogBookDescription(eventLogBookObiscode))};
        } else {
            //read out all logbooks
            eventLogs = new PrimeBasicEventLog[]{
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.0.255"), 1, "Standard events"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.1.255"), 4, "Fraud detection"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.2.255"), 2, "Disconnect control"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.3.255"), 1, "Power contract"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.4.255"), 1, "Firmware events"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.5.255"), 3, "Power quality log"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.6.255"), 5, "Demand management"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.7.255"), 6, "Common management"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.8.255"), 1, "Synchronization"),
                    new PrimeBasicEventLog(session, ObisCode.fromString("0.0.99.98.9.255"), 3, "Finished quality")
            };
        }
    }

    private int getGroupId(ObisCode eventLogBookObiscode) {
        switch (eventLogBookObiscode.getE()) {
            case 0:
                return 1;
            case 1:
                return 4;
            case 2:
                return 2;
            case 3:
                return 1;
            case 4:
                return 1;
            case 5:
                return 3;
            case 6:
                return 5;
            case 7:
                return 6;
            case 8:
                return 1;
            case 9:
                return 3;
            default:
                return 1;
        }
    }

    private String getLogBookDescription(ObisCode eventLogBookObiscode) {
        switch (eventLogBookObiscode.getE()) {
            case 0:
                return "Standard events";
            case 1:
                return "Fraud detection";
            case 2:
                return "Disconnect control";
            case 3:
                return "Power contract";
            case 4:
                return "Firmware events";
            case 5:
                return "Power quality log";
            case 6:
                return "Demand management";
            case 7:
                return "Common management";
            case 8:
                return "Synchronization";
            case 9:
                return "Finished quality";
            default:
                return "Unknown logbook";
        }

    }

    /**
     * Iterate over all the AS300D events, and fetch all the events using the from and to {@link java.util.Date}
     * If we are unable to read one of the event log books, we'll log it, and continue with the next one.
     *
     * @param fromDate The from date to start reading from
     * @param toDate   The to date
     * @return A List of MeterEvents
     */
    public List<MeterEvent> getMeterEvents(Date fromDate, Date toDate) {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        Calendar from = getCalendar(fromDate);
        Calendar to = getCalendar(toDate);

        for (PrimeBasicEventLog eventLog : eventLogs) {
            try {
                List<MeterEvent> eventLogEvents = eventLog.getEvents(from, to);
                events.addAll(eventLogEvents);
            } catch (IOException e) {
                this.session.getLogger().warning("Unable to read events from [" + eventLog.toString() + "]. " + e.getMessage());
            }
        }

        return events;
    }

    /**
     * Convert a given date to an {@link java.util.Calendar} object, taking the session {@link java.util.TimeZone} in account
     *
     * @param date The {@link java.util.Date} to convert
     * @return The new {@link java.util.Calendar} object
     */
    private final Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(session.getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

}

