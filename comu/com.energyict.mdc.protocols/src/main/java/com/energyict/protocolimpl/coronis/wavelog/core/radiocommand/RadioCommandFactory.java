package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class RadioCommandFactory {


    private WaveLog waveLog;

    // Cached, only needs to be read out once.
    private FirmwareVersion firmwareVersion = null;

    public RadioCommandFactory(WaveLog waveLog) {
        this.waveLog = waveLog;
    }

    public final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(waveLog);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public Date readTimeDateRTC() throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveLog);
        timeDateRTC.set();
        return timeDateRTC.getCalendar().getTime();
    }

    public void writeTimeDateRTC(Date date) throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveLog);
        Calendar calendar = Calendar.getInstance(waveLog.getTimeZone());
        calendar.setTime(date);
        timeDateRTC.setCalendar(calendar);
        timeDateRTC.set();
    }

    /**
     * This initializes the alarm route for the module with the route of this frame.
     */
    public void initializeAlarmRoute() throws IOException {
        InitializeAlarmRoute alarmRoute = new InitializeAlarmRoute(waveLog);
        alarmRoute.set();
    }

    public void resetEventTable() throws IOException {
        ResetEventTable resetEventTable = new ResetEventTable(waveLog);
        resetEventTable.set();
    }

    public Event[] readLast10Events() throws IOException {
        ReadLast10Events readLast10Events = new ReadLast10Events(waveLog);
        readLast10Events.set();
        return readLast10Events.getEvents();
    }

    public Event[] readEvents(int numberOfEvents, int offset) throws IOException {
        ReadEventTable eventTable = new ReadEventTable(waveLog, numberOfEvents, offset);
        eventTable.set();
        return eventTable.getEvents();
    }

    public int readCurrentInputState(int input) throws IOException {
        ReadCurrentState currentState = new ReadCurrentState(waveLog);
        currentState.set();
        return currentState.getInputState(input);
    }

    public int readCurrentOutputState(int output) throws IOException {
        ReadCurrentState currentState = new ReadCurrentState(waveLog);
        currentState.set();
        return currentState.getOutputState(output);
    }

    public double readRSSI() throws IOException {
        RSSILevel rssiLevel = new RSSILevel(waveLog);
        rssiLevel.set();
        return rssiLevel.getRssiLevel();
    }
}