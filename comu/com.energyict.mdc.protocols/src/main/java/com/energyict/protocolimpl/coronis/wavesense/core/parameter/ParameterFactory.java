/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ParameterFactory {

    private WaveSense waveSense;

    //Cached
    private ApplicationStatus applicationStatus = null;
    private OperatingMode operatingMode = null;
    private SamplingPeriod samplingPeriod;

    private static final int PERIODIC_LOGGING = 1;
    private static final int WEEKLY_LOGGING = 2;
    private static final int MONTHLY_LOGGING = 3;

    private static final int DAILY = 60 * 60 * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;

    public ParameterFactory(final WaveSense waveSense) {
        this.waveSense = waveSense;
    }

    final public ApplicationStatus readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(waveSense);
            applicationStatus.read();
        }
        return applicationStatus;
    }

    final public void writeApplicationStatus(final int status) throws IOException {
        applicationStatus = new ApplicationStatus(waveSense);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    final public OperatingMode readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(waveSense);
            operatingMode.read();
        }
        return operatingMode;
    }

    private int readDayOfWeek(int mode) throws IOException {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveSense);
        dayOfWeek.read();
        return dayOfWeek.getDayOfWeek();
    }

    public void writeDayOfWeek(int day) throws IOException {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveSense);
        dayOfWeek.setDayOfWeek(day);
        dayOfWeek.write();
    }

    //1 = periodic, 2 = weekly, 3 = monthly

    public int readDataLoggingMode() throws IOException {
        readOperatingMode();
        return operatingMode.dataLoggingSteps();
    }

    public void writeWeeklyDataLogging() throws IOException {
        stopDataLogging();
        operatingMode.setDataLoggingToOnceAWeek();
        operatingMode.write();
    }

    public void writeMonthlyDataLogging() throws IOException {
        stopDataLogging();
        operatingMode.setDataLoggingToOnceAMonth();
        operatingMode.write();
    }

    public void writePeriodicTimeStepDataLogging() throws IOException {
        stopDataLogging();
        operatingMode.setDataLoggingStepsToPeriodic();
        operatingMode.write();
    }

    public int readTimeOfMeasurement() throws IOException {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveSense);
        timeOfMeasurement.read();
        return timeOfMeasurement.getTimeOfMeasurement();
    }

    public void writeTimeOfMeasurement(int time) throws IOException {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveSense);
        timeOfMeasurement.setTimeOfMeasurement(time);
        timeOfMeasurement.write();
    }

    final public void writeOperatingMode(int operationMode) throws IOException {
        this.operatingMode = null; //Reset cache
        new OperatingMode(waveSense, operationMode).write();
    }

    public int getProfileIntervalInSeconds() throws IOException {
        readOperatingMode();
        if (operatingMode.isMonthlyMeasurement()) {
            return MONTHLY;
        }
        if (operatingMode.isWeeklyMeasurement()) {
            return WEEKLY;
        }
        if (samplingPeriod == null) {
            samplingPeriod = new SamplingPeriod(waveSense);
            samplingPeriod.read();
            waveSense.getLogger().info("Received profile data interval: [" + samplingPeriod.getSamplingPeriodInSeconds() + " seconds]");
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
        BatteryLifeDurationCounter batteryLifeDurationCounter = new BatteryLifeDurationCounter(waveSense);
        batteryLifeDurationCounter.read();
        return batteryLifeDurationCounter;
    }

    final public void writeSamplingPeriod(final int samplingPeriodInSeconds) throws IOException {
        samplingPeriod = new SamplingPeriod(waveSense);
        samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
        samplingPeriod.write();
    }

    public Date readLowBatteryDetectionDate() throws IOException {
        BatteryLowDetectionDate batteryLowDetectionDate = new BatteryLowDetectionDate(waveSense);
        batteryLowDetectionDate.read();
        return batteryLowDetectionDate.getEventDate();
    }

    public Date readSensorFaultDetectionDate() throws IOException {
        SensorFaultDetectionDate sensorFaultDetectionDate = new SensorFaultDetectionDate(waveSense);
        sensorFaultDetectionDate.read();
        return sensorFaultDetectionDate.getEventDate();
    }

    public void restartDataLogging(int mode) throws IOException {
        readOperatingMode();
        operatingMode.stopDataLogging();
        operatingMode.setDataLoggingToStopMode();
        operatingMode.write();
        setSamplingPeriodNextHour(mode);    //Checks if the logging is periodic, or weekly or monthly.
        setDayOfWeekToday(mode);                //Set the day of week (to start the logging on) to today, in case of weekly/monthly logging.

        //Now restart it
        switch (mode) {
            case 1:
                operatingMode.setDataLoggingStepsToPeriodic();
                break;
            case 2:
                operatingMode.setDataLoggingToOnceAWeek();
                break;
            case 3:
                operatingMode.setDataLoggingToOnceAMonth();
                break;
        }
        operatingMode.write();
    }

    private void setDayOfWeekToday(int mode) throws IOException {
        if (mode == WEEKLY_LOGGING) {
            Calendar now = new GregorianCalendar(waveSense.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;  //Sunday = 0, Monday = 1, ...
            writeDayOfWeek(dayOfWeek);
        }
        if (mode == MONTHLY_LOGGING) {
            Calendar now = new GregorianCalendar(waveSense.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_MONTH);
            writeDayOfWeek(dayOfWeek > 28 ? 1 : dayOfWeek);
        }
    }

    /**
     * Writes the start hour of the data logging.
     * In case of logging in period time steps, this is parameter 0x81
     * In case of weekly / monthly logging, this is parameter 0x83.
     *
     * @throws IOException
     */
    private void setSamplingPeriodNextHour(int mode) throws IOException {
        Calendar now = new GregorianCalendar(waveSense.getTimeZone());
        int nextHour = (now.get(Calendar.HOUR_OF_DAY) == 23 ? 0 : now.get(Calendar.HOUR_OF_DAY) + 1);
        setStartHourOfMeasurement(nextHour, mode);
    }

    public void setStartHourOfMeasurement(int nextHour) throws IOException {
        if (readOperatingMode().isPeriodicMeasurement()) {
            writeStartHourForPeriodicLogging(nextHour);
        } else if (readOperatingMode().isMonthlyMeasurement() || readOperatingMode().isWeeklyMeasurement()) {
            writeTimeOfMeasurement(nextHour);
        }
    }

    public void setStartHourOfMeasurement(int nextHour, int mode) throws IOException {
        if (mode == PERIODIC_LOGGING) {
            writeStartHourForPeriodicLogging(nextHour);
        } else if (mode == MONTHLY_LOGGING || mode == WEEKLY_LOGGING) {
            writeTimeOfMeasurement(nextHour);
        }
    }

    public int readStartHourOfMeasurement() throws IOException {
        if (readOperatingMode().isMonthlyMeasurement() || readOperatingMode().isWeeklyMeasurement()) {
            return readTimeOfMeasurement();
        } else {
            return readStartHourForPeriodicLogging();
        }
    }

    public int readStartHourForPeriodicLogging() throws IOException {
        DataLoggingStartHourForPeriodicSteps hour = new DataLoggingStartHourForPeriodicSteps(waveSense);
        hour.read();
        return hour.getStartHour();
    }

    final public void writeStartHourForPeriodicLogging(final int startHour) throws IOException {
        DataLoggingStartHourForPeriodicSteps startHourForPeriodicSteps = new DataLoggingStartHourForPeriodicSteps(waveSense);
        startHourForPeriodicSteps.setStartHour(startHour);
        startHourForPeriodicSteps.write();
    }

    public int readNumberOfStoredValues() throws IOException {
        NumberOfStoredValues numberOfStoredValues = new NumberOfStoredValues(waveSense);
        numberOfStoredValues.read();
        return numberOfStoredValues.getNumberOfValues();
    }

    public int readMeasurementPeriod() throws IOException {
        DetectionMeasurementPeriod measurementStep = new DetectionMeasurementPeriod(waveSense);
        measurementStep.read();
        return measurementStep.getMinutes();
    }

    public void setMeasurementPeriod(int period) throws IOException {
        DetectionMeasurementPeriod measurementPeriod = new DetectionMeasurementPeriod(waveSense);
        measurementPeriod.setMinutes(period);
        measurementPeriod.write();
    }

    public void setHighThreshold(int threshold) throws IOException {
        HighThresholdAlarm highThresholdAlarm = new HighThresholdAlarm(waveSense);
        highThresholdAlarm.setThreshold(threshold);
        highThresholdAlarm.write();
    }

    public int readLowThreshold() throws IOException {
        LowThresholdAlarm lowThresholdAlarm = new LowThresholdAlarm(waveSense);
        lowThresholdAlarm.read();
        return lowThresholdAlarm.getThreshold();
    }

    public void setLowThreshold(int threshold) throws IOException {
        LowThresholdAlarm lowThresholdAlarm = new LowThresholdAlarm(waveSense);
        lowThresholdAlarm.setThreshold(threshold);
        lowThresholdAlarm.write();
    }

    public int readHighThresholdExcessTime() throws IOException {
        HighThresholdExcessTime excessTime = new HighThresholdExcessTime(waveSense);
        excessTime.read();
        return excessTime.getTime();
    }

    public void setHighThresholdExcessTime(int time) throws IOException {
        HighThresholdExcessTime excessTime = new HighThresholdExcessTime(waveSense);
        excessTime.setTime(time);
        excessTime.write();
    }

    public int setLowThresholdExcessTime() throws IOException {
        LowThresholdExcessTime excessTime = new LowThresholdExcessTime(waveSense);
        excessTime.read();
        return excessTime.getTime();
    }

    public void setLowThresholdExcessTime(int time) throws IOException {
        LowThresholdExcessTime excessTime = new LowThresholdExcessTime(waveSense);
        excessTime.setTime(time);
        excessTime.write();
    }

    public AlarmConfig readAlarmConfiguration() throws IOException {
        AlarmConfig config = new AlarmConfig(waveSense);
        config.read();
        return config;
    }

    public void sendAlarmOnLowThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnLowThreshold();
        alarmConfig.write();
    }

    public void sendAlarmOnHighThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnHighThreshold();
        alarmConfig.write();
    }

    public void sendAlarmOnBatteryEnd() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnEndOfBattery();
        alarmConfig.write();
    }

    public void sendAlarmOnSensorFault() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnSensorFault();
        alarmConfig.write();
    }

    public void sendAllAlarms() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAllAlarms();
        alarmConfig.write();
    }

    public void disableAlarmOnLowThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnLowThreshold();
        alarmConfig.write();
    }

    public void disableAlarmOnHighThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnHighThreshold();
        alarmConfig.write();
    }

    public void disableAlarmOnBatteryEnd() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnEndOfBattery();
        alarmConfig.write();
    }

    public void disableAlarmOnSensorFault() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnSensorFault();
        alarmConfig.write();
    }

    public void disableAllAlarms() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAllAlarms();
        alarmConfig.write();
    }

    public int readNumberOfRepeaters() throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveSense);
        numberOfRepeaters.read();
        return numberOfRepeaters.getNumber();
    }

    public void writeNumberOfRepeaters(int number) throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveSense);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public String readRepeaterAddress(int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveSense, id);
        repeaterAddress.read();
        return repeaterAddress.getAddress();
    }

    public void writeRepeaterAddress(String address, int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveSense, id);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public String readRecipientAddress() throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(waveSense);
        recipientAddress.read();
        return recipientAddress.getAddress();
    }

    public void writeRecipientAddress(String address) throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(waveSense);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public void writeAlarmConfigurationByte(int alarm) throws IOException {
        AlarmConfig config = new AlarmConfig(waveSense);
        config.setAlarmConfig(alarm);
        config.write();
    }

    public int readNumberOfRetries() throws IOException {
        NumberOfRetries numberOfRetries = new NumberOfRetries(waveSense);
        numberOfRetries.read();
        return numberOfRetries.getNumber();
    }

    public void setNumberOfRetries(int value) throws IOException {
        NumberOfRetries numberOfRetries = new NumberOfRetries(waveSense);
        numberOfRetries.setNumber(value);
        numberOfRetries.write();
    }

    public int readTimeBetweenRetries() throws IOException {
        TimeBetweenRetries timeBetweenRetries = new TimeBetweenRetries(waveSense);
        timeBetweenRetries.read();
        return timeBetweenRetries.getTime();
    }

    public void setTimeBetweenRetries(int value) throws IOException {
        TimeBetweenRetries timeBetweenRetries = new TimeBetweenRetries(waveSense);
        timeBetweenRetries.setTime(value);
        timeBetweenRetries.write();
    }

    public void stopDataLogging() throws IOException {
        readOperatingMode();
        operatingMode.stopDataLogging();
        operatingMode.write();
    }
}