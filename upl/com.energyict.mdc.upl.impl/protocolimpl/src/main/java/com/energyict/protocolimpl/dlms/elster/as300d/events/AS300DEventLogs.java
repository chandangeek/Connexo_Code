package com.energyict.protocolimpl.dlms.elster.as300d.events;

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
public class AS300DEventLogs {

    private final AS300DBasicEventLog[] eventLogs;
    private final DlmsSession session;

    public AS300DEventLogs(DlmsSession session) {
        this.session = session;
        eventLogs = new AS300DBasicEventLog[]{
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.0.255"), 1, "Standard events"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.1.255"), 4, "Fraud detection"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.2.255"), 2, "Disconnect control"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.3.255"), 1, "Power contract"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.4.255"), 1, "Firmware events"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.5.255"), 3, "Power quality log"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.6.255"), 5, "Demand management"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.7.255"), 6, "Common management"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.8.255"), 1, "Synchronization"),
                new AS300DBasicEventLog(session, ObisCode.fromString("0.0.99.98.9.255"), 3, "Finished quality")
        };
    }

    public List<MeterEvent> getMeterEvents(Date fromDate, Date toDate) throws IOException {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        Calendar from = getCalendar(fromDate);
        Calendar to = getCalendar(toDate);

        for (AS300DBasicEventLog eventLog : eventLogs) {
            List<MeterEvent> eventLogEvents = eventLog.getEvents(from, to);
            events.addAll(eventLogEvents);
        }

        return events;
    }

    private Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(session.getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

}

