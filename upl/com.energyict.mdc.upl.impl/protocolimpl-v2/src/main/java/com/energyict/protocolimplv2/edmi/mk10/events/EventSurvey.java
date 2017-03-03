/*
 * EventSurvey.java
 *
 * Created on 31 maart 2006, 14:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimplv2.edmi.mk10.events;

import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.common.command.Atlas1FileAccessReadCommand;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author koen
 */
public class EventSurvey {

    private static final int BASE_REGISTER_ID = 0xD810;
    private static final int EVENT_LOG_OFFSET = 2; // The offset to use, as 0 and 1 are for load surveys
    private static final int READ_USING_RECORD_NR_OPTION = 0;
    private static final int READ_USING_DATE_OPTION = 1;

    private CommandFactory commandFactory;
    private final LogBookDescription logBookDescription;

    /**
     * Creates a new instance of EventSurvey
     */
    public EventSurvey(CommandFactory commandFactory, LogBookDescription logBookDescription) {
        this.setCommandFactory(commandFactory);
        this.logBookDescription = logBookDescription;
    }

    public List<MeterProtocolEvent> readFile(Date lastReading) throws ProtocolException {
        Atlas1FileAccessReadCommand farc;

        long firstEntry = getCommandFactory().getReadCommand(BASE_REGISTER_ID + 0x0005 + getLogBookDescription().getEventLogCode()).getRegister().getBigDecimal().longValue();
        long lastEntry = getCommandFactory().getReadCommand(BASE_REGISTER_ID + 0x000A + getLogBookDescription().getEventLogCode()).getRegister().getBigDecimal().longValue();
        int lastReadingSecondsSince1996 = DateTimeBuilder.getSecondsSince1996FromDate(this.commandFactory.getProtocol().getTimeZone(), lastReading);

        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        boolean readBasedOnDate = true;
        while (firstEntry < lastEntry) {
            farc = readBasedOnDate
                    ? this.getCommandFactory().getAtlas1FileAccessReadCommand(getLogBookDescription().getEventLogCode() + EVENT_LOG_OFFSET, READ_USING_DATE_OPTION, lastReadingSecondsSince1996, 0xFFFF)
                    : this.getCommandFactory().getAtlas1FileAccessReadCommand(getLogBookDescription().getEventLogCode() + EVENT_LOG_OFFSET, READ_USING_RECORD_NR_OPTION, firstEntry, 0xFFFF);
            firstEntry = farc.getStartRecord() + farc.getNumberOfRecords();
            meterProtocolEvents.addAll(getEventData(farc.getData(), getLogBookDescription()));
            readBasedOnDate = false;    // Read out next events using record nr
        }
        return meterProtocolEvents;
    }

    private List<MeterProtocolEvent> getEventData(byte[] data_in, LogBookDescription logBookDescription) throws ProtocolException {
        List<MeterProtocolEvent> meterProtocolEvents = new ArrayList<>();
        TimeZone tz = this.commandFactory.getProtocol().getTimeZone();

        int ptr = 0;
        Event previousEvent = null;
        while (ptr < data_in.length) {
            byte[] eventCodeBytes = ProtocolUtils.getSubArray2(data_in, ptr, 2);
            int eventCode = ProtocolUtils.getIntLE(eventCodeBytes, 0, 2) & 0x0000FFFF;
            int eventTime = ProtocolUtils.getIntLE(data_in, ptr + 2, 4);

            if (isInvalidEvent(eventCode, previousEvent)) {
                break;  // Simply continue
            }

            Event event = new Event(eventTime, eventCode, logBookDescription, previousEvent, tz);
            previousEvent = event;
            ptr += 6;

            // Filter the user logon/logoff events to prevent unused events
            // Every time the mk10 protocol connects, it generates at least two events
            // in the log (Logon and logoff)
            if (((eventCode & 0xFFC0) == 0x2000) ||         // User logged on
                    ((eventCode & 0xFFCF) == 0x2081) ||     // User logged off because a log off was requested by the X command
                    ((eventCode & 0xFFCF) == 0x2082) ||     // USer logged off because of inactivity time-out
                    ((eventCode & 0xFFCF) == 0x2083) ||     // USer logged off because of a lost connection
                    ((eventCode & 0xFFCF) == 0x2085)) {      // User logged off because a logoff was requested via register write
                // don't add the event
            } else {
                meterProtocolEvents.add(event.convertToMeterProtocolEvent());
            }
        }
        return meterProtocolEvents;
    }

    private boolean isInvalidEvent(int eventCode, Event previousEvent) {
        // For events with code 0x7xxx and 0x8xxx the event time doesn't reflect event timestamp, but contains special encoded content.
        // These events are adjacent to a previous event - if no previous event, then these events shouldn't be handled.
        return previousEvent == null && (((eventCode & 0xF000) == 0x7000) || ((eventCode & 0xF000) == 0x8000));
    }

    public LogBookDescription getLogBookDescription() {
        return logBookDescription;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }
}