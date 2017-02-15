/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.events;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DlmsSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class G3Events {

    public static final ObisCode MAIN_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode COVER_LOG = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode BREAKER_LOG = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode COMMUNICATION_LOG = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode VOLTAGE_CUT_LOG = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode LQI_EVENT_LOG = ObisCode.fromString("0.0.99.98.10.255");

    private final DlmsSession session;
    private final EventLog[] eventLogs;

    /**
     * Create a new G3Events object. This class is a group of event logs available in the G3 meter
     *
     * @param session The {@link com.energyict.dlms.DlmsSession} used to read the events
     */
    public G3Events(DlmsSession session) {
        this.session = session;
        this.eventLogs = new EventLog[]{
                new G3BasicEventLog(session, MAIN_LOG, "Main", new MainEventMapper()),
                new G3BasicEventLog(session, COVER_LOG, "Cover", new CoverEventMapper()),
                new G3BasicEventLog(session, BREAKER_LOG, "Breaker", new BreakerEventMapper()),
                new G3BasicEventLog(session, COMMUNICATION_LOG, "Communication", new CommunicationEventMapper()),
                new G3BasicEventLog(session, VOLTAGE_CUT_LOG, "Voltage cut", new VoltageCutEventMapper()),
                new G3LqiEventLog(session, LQI_EVENT_LOG, "LQI")
        };
    }

    /**
     * Read all the events from the meter between the two given dates, and return them as a list of {@link com.energyict.protocol.MeterEvent}
     * If an exception occurs during the readout of one EventLog, skip this log and try to continue with the others.
     *
     * @param fromDate The starting date to get the events from
     * @param toDate   The end date to get the events from
     * @return A list of events read from the meter. This list will never be 'null'
     * @throws java.io.IOException
     */
    public final List<MeterEvent> getMeterEvents(Date fromDate, Date toDate) throws IOException {
        final List<MeterEvent> events = new ArrayList<MeterEvent>();
        final Calendar from = getCalendar(fromDate);
        final Calendar to = getCalendar(toDate);

        for (final EventLog eventLog : eventLogs) {
            try {
                final List<MeterEvent> eventLogEvents = eventLog.getEvents(from, to);
                if (events != null) {
                    events.addAll(eventLogEvents);
                }
            } catch (IOException e) {
                session.getLogger().log(Level.WARNING, "Unable to read events from [" + eventLog + "]: " + e.getMessage(), e);
            }
        }

        return events;
    }

    /**
     * Convert a given date to a java Calendar, taking the sessions TimeZone in account.
     *
     * @param date The date to convert
     * @return The new Calendar
     */
    private final Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(session.getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

}