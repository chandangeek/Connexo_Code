/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ParameterFactory;

import java.io.IOException;
import java.util.Date;

public class ParameterFactoryHydreka extends ParameterFactory {

    private OperatingModeHydreka operatingMode = null;
    private BatteryLifeDurationCounter batteryLifeDurationCounter;
    private Date leakageDetectionDate = null;     //Comes in the 0x27 frame

    public ParameterFactoryHydreka(final WaveFlow waveFlow) {
        super(waveFlow);
    }

    public void writeAlarmConfigurationByte(int alarm) throws IOException {
        alarm = alarm & 0x0F;       //Bit7, 6, 5 and 4 are never used
        AlarmConfigHydreka config = new AlarmConfigHydreka(waveFlow);
        config.setAlarmConfig(alarm);
        config.write();
    }

    public int readAlarmConfigurationValue() throws IOException {
        AlarmConfigHydreka config = new AlarmConfigHydreka(waveFlow);
        config.read();
        return config.getAlarmConfig();
    }

    public OperatingModeHydreka readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingModeHydreka(waveFlow);
            operatingMode.read();
        }
        return operatingMode;
    }

    @Override
    public double readBatteryLifeDurationCounter() throws IOException {
        if (batteryLifeDurationCounter == null) {
            batteryLifeDurationCounter = new BatteryLifeDurationCounter(waveFlow);
            batteryLifeDurationCounter.read();
        }
        return batteryLifeDurationCounter.remainingBatteryLife();
    }

    public void enablePushFrames() throws IOException {
        writeWorkingMode(0x0800, 0x0800);     //Set flag in the extended operation mode
    }

    public void disablePushFrames() throws IOException {
        writeWorkingMode(0x0000, 0x0800);     //Reset flag in the extended operation mode
    }

    public int readReadingHourLeakageStatus() throws IOException {
        ReadingHourLeakageStatus hourLeakageStatus = new ReadingHourLeakageStatus(waveFlow);
        hourLeakageStatus.read();
        return hourLeakageStatus.getHour();
    }

    public void writeReadingHourLeakageStatus(int hour) throws IOException {
        ReadingHourLeakageStatus hourLeakageStatus = new ReadingHourLeakageStatus(waveFlow);
        hourLeakageStatus.setHour(hour);
        hourLeakageStatus.write();
    }

    @Override
    public void writeOperatingMode(int operatingModeVal) throws IOException {
        super.writeWorkingMode(operatingModeVal, 0xFF);         //Hydreka specific way of writing the operation mode...
    }

    public int readReadingHourHistogram() throws IOException {
        ReadingHourHistogram hourHistogram = new ReadingHourHistogram(waveFlow);
        hourHistogram.read();
        return hourHistogram.getHour();
    }

    public void writeReadingHourHistogram(int hour) throws IOException {
        ReadingHourHistogram hourHistogram = new ReadingHourHistogram(waveFlow);
        hourHistogram.setHour(hour);
        hourHistogram.write();
    }

    public int readRTCResynchPeriod() throws IOException {
        RTCResynchPeriod period = new RTCResynchPeriod(waveFlow);
        period.read();
        return period.getPeriod();
    }

    public void writeRTCResynchPeriod(int period) throws IOException {
        RTCResynchPeriod resynchPeriod = new RTCResynchPeriod(waveFlow);
        resynchPeriod.setPeriod(period);
        resynchPeriod.write();
    }

    @Override
    public Date readTimeDateRTC() throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveFlow);
        timeDateRTC.read();
        return timeDateRTC.getTime();
    }

    @Override
    public void writeTimeDateRTC(Date date) throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(waveFlow);
        timeDateRTC.setTime(date);
        timeDateRTC.write();
    }

    public Date readModuleEndOfBatteryTimestamp() throws IOException {
        ModuleEndOfBatteryTimestamp timestamp = new ModuleEndOfBatteryTimestamp(waveFlow);
        timestamp.read();
        return timestamp.getTimeStamp();
    }

    public Date readProbeEndOfBatteryTimestamp() throws IOException {
        ProbeEndOfBatteryTimestamp timestamp = new ProbeEndOfBatteryTimestamp(waveFlow);
        timestamp.read();
        return timestamp.getTimeStamp();
    }

    public Date readTamperTimestamp() throws IOException {
        TamperTimestamp timestamp = new TamperTimestamp(waveFlow);
        timestamp.read();
        return timestamp.getTimeStamp();
    }

    /**
     * If already cached (e.g. comes in the 0x27 frame), return the timestamp.
     * If not cached yet, read it from the module (if allowed) or return new Date() in case of InitialRFCommand usage.
     */
    public Date readLeakageTimestamp() throws IOException {
        if (leakageDetectionDate != null) {
            return leakageDetectionDate;
        }
        if (waveFlow.usesInitialRFCommand()) {
            return new Date();
        } else {
            LeakageTimestamp timestamp = new LeakageTimestamp(waveFlow);
            timestamp.read();
            leakageDetectionDate = timestamp.getTimeStamp();
            return leakageDetectionDate;
        }
    }

    public void setLeakageDetectionDate(Date leakageDetectionDate) {
        this.leakageDetectionDate = leakageDetectionDate;
    }
}