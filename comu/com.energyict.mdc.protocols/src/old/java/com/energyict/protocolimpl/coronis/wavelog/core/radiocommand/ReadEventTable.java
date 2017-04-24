/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.coronis.wavelog.core.parameter.ApplicationStatus;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

public class ReadEventTable extends AbstractRadioCommand {

    protected ReadEventTable(WaveLog waveLog, int numberOfEvents, int offset) {
        super(waveLog);
        this.numberOfEvents = numberOfEvents;
        this.offset = offset;
    }

    private int numberOfEvents = 10;
    private int offset = 0;
    private int indexFirst = -1;
    private int indexLast = -1;
    private Event[] events;
    private ApplicationStatus status;

    public Event[] getEvents() {
        return events;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("No events stored in the table");
        }

        offset++;         //Skip frame number
        offset++;

        indexFirst = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
        offset += 2;

        indexLast = ProtocolTools.getUnsignedIntFromBytes(data, offset, 2);
        offset += 2;

        int numberOfReceivedEvents = indexFirst - indexLast + 1;
        events = new Event[numberOfReceivedEvents];
             
        int eventStatus;
        int cause;
        Date eventDate;
        for (int index = 0; index < numberOfReceivedEvents; index++) {
            eventStatus = data[offset] & 0xFF;
            offset++;

            cause = data[offset] & 0xFF;
            offset++;

            eventDate = TimeDateRTCParser.parse(data, offset, 7, getWaveLog().getTimeZone()).getTime();
            offset += 7;

            events[index] = new Event(cause, eventDate, eventStatus);
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        byte[] numberBytes = ProtocolTools.getBytesFromInt(numberOfEvents, 2);
        byte[] offsetBytes = ProtocolTools.getBytesFromInt(offset, 2);
        return ProtocolTools.concatByteArrays(numberBytes, offsetBytes);
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadEventsTable;
    }
}
