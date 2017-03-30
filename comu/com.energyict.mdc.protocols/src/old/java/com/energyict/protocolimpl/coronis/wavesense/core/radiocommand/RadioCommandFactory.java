/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class RadioCommandFactory {


    private WaveSense waveSense;

    // Cached, only needs to be read out once.
    private FirmwareVersion firmwareVersion = null;
    private ModuleType moduleType = null;

    public RadioCommandFactory(WaveSense waveSense) {
        this.waveSense = waveSense;
    }

    public final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(waveSense);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public final ModuleType readModuleType() throws IOException {
        if (moduleType == null) {
            moduleType = new ModuleType(waveSense);
            moduleType.set();
        }
        return moduleType;
    }

    public final DetectionTable readDetectionTable() throws IOException {
        DetectionTable detectionTable = new DetectionTable(waveSense);
        detectionTable.set();
        return detectionTable;
    }

    public Date readTimeDateRTC() throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveSense);
        timeDateRTC.set();
        return timeDateRTC.getCalendar().getTime();
    }

    public void writeTimeDateRTC(Date date) throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveSense);
        Calendar calendar = Calendar.getInstance(waveSense.getTimeZone());
        calendar.setTime(date);
        timeDateRTC.setCalendar(calendar);
        timeDateRTC.set();
    }

    public double readCurrentValue() throws IOException {
        CurrentReading currentReading = new CurrentReading(waveSense);
        currentReading.set();
        return currentReading.getCurrentValue();
    }

    public ExtendedDataloggingTable readProfileData(int nrOfValues, Date toDate) throws IOException {
        ExtendedDataloggingTable extendedDataloggingTable = new ExtendedDataloggingTable(waveSense, nrOfValues, toDate);
        extendedDataloggingTable.set();
        return extendedDataloggingTable;
    }

    public ExtendedDataloggingTable readProfileData(int nrOfValues, long offset) throws IOException {
        ExtendedDataloggingTable extendedDataloggingTable = new ExtendedDataloggingTable(waveSense, nrOfValues, offset);
        extendedDataloggingTable.set();
        return extendedDataloggingTable;
    }

    /**
     * This requests the most recent record, and checks it timestamp.
     * @return most recent timestamp
     * @throws java.io.IOException when the communication fails
     */
    public ExtendedDataloggingTable getMostRecentRecord() throws IOException {
        ExtendedDataloggingTable newestRecord = new ExtendedDataloggingTable(waveSense, 1, 0);
        newestRecord.set();
        return newestRecord;
    }

    public double readRSSI() throws IOException {
        RSSILevel rssiLevel = new RSSILevel(waveSense);
        rssiLevel.set();
        return rssiLevel.getRssiLevel();
    }
}