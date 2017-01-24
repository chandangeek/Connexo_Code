package com.energyict.protocolimpl.coronis.wavelog;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.coronis.wavelog.core.radiocommand.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileDataReader {

    private WaveLog waveLog;

    ProfileDataReader(WaveLog waveLog) {
        this.waveLog = waveLog;
    }

    final ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents(lastReading, toDate));
        }
        return profileData;
    }

    /**
     * Reads out the events, in steps of 10.
     */
    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        int numberOfLoggedEvents = waveLog.getParameterFactory().readNumberOfLoggedEvents();
        if (numberOfLoggedEvents == 0) {
            return meterEvents;
        }

        int offset = 0;

        int counter = 0;
        boolean read = true;

        while (read) {
            Event[] events = waveLog.getRadioCommandFactory().readEvents(10, offset);

            //Add the valid events
            for (Event event : events) {
                if (events.length < 10) {    //Indicates there's no further events after this read
                    read = false;
                }
                if (event.getEventDate().after(lastReading) && event.getEventDate().before(toDate)) {
                    meterEvents.add(new MeterEvent(event.getEventDate(), 0,
                            "Cause: " + event.getCauseDescription() +
                                    ". Input 1 level: " + event.getInputStateDescription(1) +
                                    ". Input 2 level: " + event.getInputStateDescription(2) +
                                    ". Input 3 level: " + event.getInputStateDescription(3) +
                                    ". Input 4 level: " + event.getInputStateDescription(4)));
                } else if (event.getEventDate().before(lastReading)) {
                    read = false;
                    break;
                }
            }
            counter++;
            offset = numberOfLoggedEvents - 10 * counter;
        }

        return meterEvents;
    }
}