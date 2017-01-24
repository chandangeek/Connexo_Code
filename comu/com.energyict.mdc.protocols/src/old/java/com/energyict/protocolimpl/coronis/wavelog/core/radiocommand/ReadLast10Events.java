package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.coronis.wavelog.core.parameter.ApplicationStatus;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 1-apr-2011
 * Time: 15:49:53
 */
public class ReadLast10Events extends AbstractRadioCommand {

    protected ReadLast10Events(WaveLog waveLog) {
        super(waveLog);
    }

    private Event[] events = new Event[10];
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

        status = new ApplicationStatus(getWaveLog(), data[offset] & 0xFF);
        offset++;

        int state = data[offset] & 0xFF;
        offset++;

        int eventStatus;
        int cause;
        Date eventDate;
        for (int index = 0; index < 10; index++) {
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
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadLast10Events;
    }
}
