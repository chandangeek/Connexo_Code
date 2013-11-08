package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.coronis.wavelog.core.radiocommand.WriteOutputs;

import java.io.IOException;
import java.util.Date;

public class ParameterFactory {

    private WaveLog waveLog;

    //Cached
    private ApplicationStatus applicationStatus = null;
    private OperatingMode operatingMode = null;

    public ParameterFactory(final WaveLog waveLog) {
        this.waveLog = waveLog;
    }

    final public ApplicationStatus readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(waveLog);
            applicationStatus.read();
        }
        return applicationStatus;
    }

    final public void writeApplicationStatus(final int status) throws IOException {
        applicationStatus = new ApplicationStatus(waveLog);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    final public OperatingMode readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(waveLog);
            operatingMode.read();
        }
        return operatingMode;
    }

    final public void writeOperatingMode(int operationMode) throws IOException {
        this.operatingMode = null; //Reset cache
        new OperatingMode(waveLog, operationMode).write();
    }

    public void writeNumberOfRepeaters(int number) throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveLog);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public void writeRepeaterAddress(String address, int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveLog, id);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public void writeRecipientAddress(String address) throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(waveLog);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public void setNumberOfRetriesAlarmFrames(int value) throws IOException {
        NumberOfRetries numberOfRetries = new NumberOfRetries(waveLog);
        numberOfRetries.setNumber(value);
        numberOfRetries.write();
    }

    public void setTimeBetweenRetries(int value) throws IOException {
        TimeBetweenRetries timeBetweenRetries = new TimeBetweenRetries(waveLog);
        timeBetweenRetries.setTime(value);
        timeBetweenRetries.write();
    }

    public Date readLowBatteryDetectionDate() throws IOException {
        BatteryLifeDateEnd date = new BatteryLifeDateEnd(waveLog);
        date.read();
        return date.getCalendar().getTime();
    }

    public void resetApplicationStatus() throws IOException {
        readApplicationStatus();
        applicationStatus.reset();
        applicationStatus.write();
    }

    public void setNumberOfRetries(int value) throws IOException {
        NumberOfRetries numberOfRetries = new NumberOfRetries(waveLog);
        numberOfRetries.setNumber(value);
        numberOfRetries.write();
    }

    public void writeInputConfigurationByte(int config, int input) throws IOException {
        InputConfigurationByte configurationByte = new InputConfigurationByte(waveLog, input);
        configurationByte.setConfig(config);
        configurationByte.write();
    }

    public void writeStabilityDuration(int duration, int input) throws IOException {
        StabilityDuration stabilityDuration = new StabilityDuration(waveLog, input);
        stabilityDuration.setDuration(duration);
        stabilityDuration.write();
    }

    public void writeImpulseDuration(int duration, int input) throws IOException {
        ImpulseDuration impulseDuration = new ImpulseDuration(waveLog, input);
        impulseDuration.setDuration(duration);
        impulseDuration.write();
    }

    public void setTimeBetweenPeriodicRetries(int value) throws IOException {
        TimeBetweenPeriodicRetries timeBetweenPeriodicRetries = new TimeBetweenPeriodicRetries(waveLog);
        timeBetweenPeriodicRetries.setTime(value);
        timeBetweenPeriodicRetries.write();
    }

    public void setTransmissionPeriodOfPeriodicFrames(int period) throws IOException {
        PeriodBetweenPeriodicFrames periodBetweenPeriodicFrames = new PeriodBetweenPeriodicFrames(waveLog);
        periodBetweenPeriodicFrames.setTime(period);
        periodBetweenPeriodicFrames.write();
    }

    public void setNumberOfPeriodicRetries(int value) throws IOException {
        NumberOfPeriodicRetries retries = new NumberOfPeriodicRetries(waveLog);
        retries.setTime(value);
        retries.write();
    }

    public void writeOutputLevel(int output, int type, int level) throws IOException {
        WriteOutputs writeOutputs = new WriteOutputs(waveLog, output, type, level);
        writeOutputs.invoke();
    }

    public int readNumberOfLoggedEvents() throws IOException {
        NumberOfLoggedEvents numberOfLoggedEvents = new NumberOfLoggedEvents(waveLog);
        numberOfLoggedEvents.read();
        return numberOfLoggedEvents.getNumber();
    }
}