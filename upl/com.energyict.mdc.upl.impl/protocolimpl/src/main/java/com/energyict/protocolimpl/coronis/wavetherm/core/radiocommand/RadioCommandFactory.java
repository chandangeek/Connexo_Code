package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class RadioCommandFactory {

    private final WaveTherm waveTherm;

    // Cached, only needs to be read out once.
    private FirmwareVersion firmwareVersion = null;

    public RadioCommandFactory(WaveTherm waveSense) {
        this.waveTherm = waveSense;
    }

    public final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(waveTherm);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public Date readTimeDateRTC() throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveTherm);
        timeDateRTC.set();
        return timeDateRTC.getCalendar().getTime();
    }

    public void writeTimeDateRTC(Date date) throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveTherm);
        Calendar calendar = Calendar.getInstance(waveTherm.getTimeZone());
        calendar.setTime(date);
        timeDateRTC.setCalendar(calendar);
        timeDateRTC.set();
    }

    public CurrentReading readCurrentValue() throws IOException {
        CurrentReading currentReading = new CurrentReading(waveTherm);
        currentReading.set();
        return currentReading;
    }

    public AlarmTable readAlarmTable() throws IOException {
        AlarmTable table = new AlarmTable(waveTherm);
        table.set();
        return table;
    }

    public ExtendedDataloggingTable readProfileData(int nrOfValues, Date toDate) throws IOException {
        ExtendedDataloggingTable extendedDataloggingTable = new ExtendedDataloggingTable(waveTherm, nrOfValues * waveTherm.getNumberOfChannels(), toDate);
        extendedDataloggingTable.set();
        return extendedDataloggingTable;
    }

    public ExtendedDataloggingTable readProfileData(int nrOfValues, long offset) throws IOException {
        ExtendedDataloggingTable extendedDataloggingTable = new ExtendedDataloggingTable(waveTherm, nrOfValues * waveTherm.getNumberOfChannels(), offset);
        extendedDataloggingTable.set();
        return extendedDataloggingTable;
    }

    /**
     * This requests the most recent record, and checks it timestamp.
     * @return most recent timestamp
     * @throws java.io.IOException when the communication fails
     */
    public ExtendedDataloggingTable getMostRecentRecord() throws IOException {
        ExtendedDataloggingTable newestRecord = new ExtendedDataloggingTable(waveTherm, 1, 0);
        newestRecord.set();
        return newestRecord;
    }

    public double readRSSI() throws IOException {
        RSSILevel rssiLevel = new RSSILevel(waveTherm);
        rssiLevel.set();
        return rssiLevel.getRssiLevel();
    }
}