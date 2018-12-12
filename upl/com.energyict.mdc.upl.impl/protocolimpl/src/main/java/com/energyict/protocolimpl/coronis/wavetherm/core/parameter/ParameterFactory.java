package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ParameterFactory {

    private WaveTherm waveTherm;

    //Cached
    private ApplicationStatus applicationStatus = null;
    private OperatingMode operatingMode = null;
    private MeasurementPeriod samplingPeriod = null;

    private static final int DAILY = 60 * 60 * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;

    private static final int PERIODIC_LOGGING = 1;
    private static final int WEEKLY_LOGGING = 2;
    private static final int MONTHLY_LOGGING = 3;

    public ParameterFactory(final WaveTherm waveTherm) {
        this.waveTherm = waveTherm;
    }

    public final ApplicationStatus readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(waveTherm);
            applicationStatus.read();
        }
        return applicationStatus;
    }

    public final void writeApplicationStatus(final int status) throws IOException {
        applicationStatus = new ApplicationStatus(waveTherm);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    public final OperatingMode readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(waveTherm);
            operatingMode.read();
        }
        return operatingMode;
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

    public final void writeOperatingMode(int operationMode) throws IOException {
        this.operatingMode = null; //Reset cache
        new OperatingMode(waveTherm, operationMode).write();
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
            samplingPeriod = new MeasurementPeriod(waveTherm);
            samplingPeriod.read();
            waveTherm.getLogger().info("Received profile data interval: [" + samplingPeriod.getSamplingPeriodInSeconds() + " seconds]");
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    public void writeSamplingPeriod(int profileInterval) throws IOException {
        samplingPeriod = null; //Reset cache
        MeasurementPeriod period = new MeasurementPeriod(waveTherm);
        period.setSamplingPeriodInSeconds(profileInterval);
        period.write();
    }

    public void setStartHourOfMeasurement(int time) throws IOException {
        if (readOperatingMode().isPeriodicMeasurement()) {
            writeStartHourForPeriodicLogging(time);
        } else if (readOperatingMode().isMonthlyMeasurement() || readOperatingMode().isWeeklyMeasurement()) {
            writeTimeOfMeasurement(time);
        }
    }

    public void setStartHourOfMeasurement(int nextHour, int mode) throws IOException {
        if (mode == PERIODIC_LOGGING) {
            writeStartHourForPeriodicLogging(nextHour);
        } else if (mode == MONTHLY_LOGGING || mode == WEEKLY_LOGGING) {
            writeTimeOfMeasurement(nextHour);
        }
    }

    //Write start hour for periodic data logging

    public final void writeStartHourForPeriodicLogging(final int startHour) throws IOException {
        DataLoggingStartHourForPeriodicSteps startHourForPeriodicSteps = new DataLoggingStartHourForPeriodicSteps(waveTherm);
        startHourForPeriodicSteps.setStartHour(startHour);
        startHourForPeriodicSteps.write();
    }

    //Write start hour for weekly / monthly data logging

    public void writeTimeOfMeasurement(int time) throws IOException {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveTherm);
        timeOfMeasurement.setTimeOfMeasurement(time);
        timeOfMeasurement.write();
    }

    public void writeDayOfWeek(int day) throws IOException {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveTherm);
        dayOfWeek.setDayOfWeek(day);
        dayOfWeek.write();
    }

    public void restartDataLogging(int mode) throws IOException {
        readOperatingMode();
        operatingMode.stopDataLogging();
        operatingMode.setDataLoggingToStopMode();
        operatingMode.write();
        setSamplingPeriodNextHour(mode);    //Also checks if the logging is periodic, or weekly or monthly.
        setDayOfWeekToday(mode);            //Set the day of week (to start the logging on) to today, in case of weekly/monthly logging.

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

    /**
     * Writes the start hour of the data logging.
     * In case of logging in period time steps, this is parameter 0x81
     * In case of weekly / monthly logging, this is parameter 0x83.
     *
     * @throws IOException
     */
    private void setSamplingPeriodNextHour(int mode) throws IOException {
        Calendar now = new GregorianCalendar(waveTherm.getTimeZone());
        int nextHour = (now.get(Calendar.HOUR_OF_DAY) == 23 ? 0 : now.get(Calendar.HOUR_OF_DAY) + 1);
        setStartHourOfMeasurement(nextHour, mode);
    }

    private void setDayOfWeekToday(int mode) throws IOException {
        if (mode == WEEKLY_LOGGING) {
            Calendar now = new GregorianCalendar(waveTherm.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;  //Sunday = 0, Monday = 1, ...
            writeDayOfWeek(dayOfWeek);
        }
        if (mode == MONTHLY_LOGGING) {
            Calendar now = new GregorianCalendar(waveTherm.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_MONTH);
            writeDayOfWeek(dayOfWeek > 28 ? 1 : dayOfWeek);
        }
    }

    public AlarmConfig readAlarmConfiguration() throws IOException {
        AlarmConfig config = new AlarmConfig(waveTherm);
        config.read();
        return config;
    }

    public void sendAlarmOnLowThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnLowThresholdOnSensor1();
        alarmConfig.write();
    }

    public void sendAlarmOnHighThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnHighThresholdOnSensor1();
        alarmConfig.write();
    }

    public void sendAlarmOnBatteryEnd() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnEndOfBattery();
        alarmConfig.write();
    }

    public void sendAllAlarms() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAllAlarms();
        alarmConfig.write();
    }

    public void disableAlarmOnLowThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnLowThresholdOnSensor1();
        alarmConfig.write();
    }

    public void disableAlarmOnHighThreshold() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnHighThresholdOnSensor1();
        alarmConfig.write();
    }

    public void disableAlarmOnBatteryEnd() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnEndOfBattery();
        alarmConfig.write();
    }

    public void disableAllAlarms() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAllAlarms();
        alarmConfig.write();
    }

    public void writeNumberOfRepeaters(int number) throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveTherm);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public void writeRepeaterAddress(String address, int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveTherm, id);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public void writeRecipientAddress(String address) throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(waveTherm);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public void writeAlarmConfigurationByte(int alarm) throws IOException {
        AlarmConfig config = new AlarmConfig(waveTherm);
        config.setAlarmConfig(alarm);
        config.write();
    }

    public void setNumberOfRetries(int value) throws IOException {
        NumberOfRetries numberOfRetries = new NumberOfRetries(waveTherm);
        numberOfRetries.setNumber(value);
        numberOfRetries.write();
    }

    public void setTimeBetweenRetries(int value) throws IOException {
        TimeBetweenRetries timeBetweenRetries = new TimeBetweenRetries(waveTherm);
        timeBetweenRetries.setTime(value);
        timeBetweenRetries.write();
    }

    public Date readLowBatteryDetectionDate() throws IOException {
        BatteryLifeDateEnd date = new BatteryLifeDateEnd(waveTherm);
        date.read();
        return date.getCalendar().getTime();
    }

    public void writeLowThresholdAlarmDuration(int duration, int sensor) throws IOException {
        LowThresholdAlarmDuration alarmDuration = new LowThresholdAlarmDuration(waveTherm);
        alarmDuration.setDuration(duration);
        alarmDuration.setSensor(sensor);
        alarmDuration.write();
    }

    public void writeHighThresholdAlarmDuration(int duration, int sensor) throws IOException {
        HighThresholdAlarmDuration alarmDuration = new HighThresholdAlarmDuration(waveTherm);
        alarmDuration.setDuration(duration);
        alarmDuration.setSensor(sensor);
        alarmDuration.write();
    }

    public void writeLowThreshold(double threshold, int sensor) throws IOException {
        LowThreshold lowThreshold = new LowThreshold(waveTherm);
        lowThreshold.setThreshold(threshold);
        lowThreshold.setSensor(sensor);
        lowThreshold.write();
    }

    public void writeHighThreshold(double threshold, int sensor) throws IOException {
        HighThreshold highThreshold = new HighThreshold(waveTherm);
        highThreshold.setThreshold(threshold);
        highThreshold.setSensor(sensor);
        highThreshold.write();
    }

    public void writeCumulativeDetectionMode() throws IOException {
        readOperatingMode();
        operatingMode.setCumulativeThresholdDetectionMode();
        operatingMode.write();
    }

    public void writeSuccessiveDetectionMode() throws IOException {
        readOperatingMode();
        operatingMode.setSuccessiveThresholdDetectionMode();
        operatingMode.write();
    }

    public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
        BatteryLifeDurationCounter durationCounter = new BatteryLifeDurationCounter(waveTherm);
        durationCounter.read();
        return durationCounter;
    }

    public void writeMeasurementPeriod(int value) throws IOException {
        DetectionMeasurementPeriod period = new DetectionMeasurementPeriod(waveTherm);
        period.setMinutes(value);
        period.write();
    }

    public int readNumberOfLoggedValues() throws IOException {
        NumberOfLoggedValues number = new NumberOfLoggedValues(waveTherm);
        number.read();
        return number.getNumber();
    }

    public void resetApplicationStatus() throws IOException {
        readApplicationStatus();
        applicationStatus.reset();
        applicationStatus.write();
    }

    public void stopDataLogging() throws IOException {
        readOperatingMode();
        operatingMode.stopDataLogging();
        operatingMode.write();
    }

    public double readLowThreshold(int sensorId) throws IOException {
        LowThreshold lowThreshold = new LowThreshold(waveTherm);
        lowThreshold.setSensor(sensorId);
        lowThreshold.read();
        return lowThreshold.getThreshold();
    }

    public double readHighThreshold(int sensorId) throws IOException {
        HighThreshold highThreshold = new HighThreshold(waveTherm);
        highThreshold.setSensor(sensorId);
        highThreshold.read();
        return highThreshold.getThreshold();
    }

    public int readHighThresholdDuration(int sensorId) throws IOException {
        HighThresholdAlarmDuration duration = new HighThresholdAlarmDuration(waveTherm);
        duration.setSensor(sensorId);
        duration.read();
        return duration.getDuration();
    }

    public int readLowThresholdDuration(int sensorId) throws IOException {
        LowThresholdAlarmDuration duration = new LowThresholdAlarmDuration(waveTherm);
        duration.setSensor(sensorId);
        duration.read();
        return duration.getDuration();
    }

    public int readDetectionMeasurementPeriod() throws IOException {
        DetectionMeasurementPeriod period= new DetectionMeasurementPeriod(waveTherm);
        period.read();
        return period.getMinutes();
    }

    public void setHighThresholdDetection(int enabled) throws IOException {
        readOperatingMode();
        operatingMode.setHighThresholdDetection(enabled);
        operatingMode.write();
    }

    public void setLowThresholdDetection(int enabled) throws IOException {
        readOperatingMode();
        operatingMode.setLowThresholdDetection(enabled);
        operatingMode.write();
    }
}