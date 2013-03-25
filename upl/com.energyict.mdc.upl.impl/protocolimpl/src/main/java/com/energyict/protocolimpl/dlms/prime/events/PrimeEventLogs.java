package com.energyict.protocolimpl.dlms.prime.events;

import com.energyict.dlms.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

/**
 * Provides functionality to collect the events from the device and return them as a list of {@link com.energyict.protocol.MeterEvent}
 * <p/>
 * Copyrights EnergyICT
 * Date: 24-02-2012
 * Time: 13:33:07
 */
public class PrimeEventLogs {

    private final PrimeBasicEventLog[] eventLogs;
    private final DlmsSession session;

    public PrimeEventLogs(DlmsSession session) {
        this.session = session;
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

    /**
     * Iterate over all the AS300D events, and fetch all the events using the from and to {@link Date}
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
     * Convert a given date to an {@link Calendar} object, taking the session {@link TimeZone} in account
     *
     * @param date The {@link Date} to convert
     * @return The new {@link Calendar} object
     */
    private final Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(session.getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

}

