package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.RouteConfiguration;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;
import java.util.*;

public class ParameterFactory {

    private RTM rtm;

    //Cached
    private ApplicationStatus applicationStatus = null;
    private OperatingMode operatingMode = null;
    private ProfileType profileType = null;
    private SamplingPeriod samplingPeriod = null;
    private RtmUnit[] rtmUnits = new RtmUnit[4];
    private MeasurementPeriodMultiplier samplingIntervalMultiplier = null;

    private static final int DAILY = 60 * 60 * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;

    private static final int PERIODIC_STEPS = 1;
    private static final int WEEKLY_LOGGING = 2;
    private static final int MONTHLY_LOGGING = 3;

    public ParameterFactory(final RTM rtm) {
        this.rtm = rtm;
    }

    public Date readTimeDateRTC() throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(rtm);
        timeDateRTC.read();
        return timeDateRTC.getCalendar().getTime();
    }

    public void writeTimeDateRTC(Date date) throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(rtm);
        Calendar calendar = Calendar.getInstance(rtm.getTimeZone());
        calendar.setTime(date);
        timeDateRTC.setCalendar(calendar);
        timeDateRTC.write();
    }

    public void stopDataLogging() throws IOException {
        getNewOperationMode().setDataLoggingMode(0);
        operatingMode.write();
    }

    public void setDataLoggingToPeriodic() throws IOException {
        getNewOperationMode().setDataLoggingMode(1);
        operatingMode.write();
    }

    public void setDataLoggingToWeekly() throws IOException {
        getNewOperationMode().setDataLoggingMode(2);
        operatingMode.write();
    }

    public void setDataLoggingToMonthly() throws IOException {
        getNewOperationMode().setDataLoggingMode(3);
        operatingMode.write();
    }

    final public void writeSamplingActivationNextHour(int mode) throws IOException {
        Calendar cal = Calendar.getInstance(rtm.getTimeZone());
        int hour = cal.get(Calendar.HOUR_OF_DAY) + 1;
        hour = hour % 24;
        writeStartHourOfMeasurement(hour, mode);        //Also checks if it's periodic or weekly/monthly
    }

    public void writeStartHourOfMeasurement(int time, int mode) throws IOException {
        if (mode == WEEKLY_LOGGING || mode == MONTHLY_LOGGING) {
            writeTimeOfMeasurement(time);
        }
    }

    public void writeStartHourOfMeasurement(int time) throws IOException {
        if (readOperatingMode().isMonthlyLogging() || readOperatingMode().isWeeklyLogging()) {
            writeTimeOfMeasurement(time);
        }
    }

    public void writeTimeOfMeasurement(int time) throws IOException {
        HourOfMeasurement hourOfMeasurement = new HourOfMeasurement(rtm);
        hourOfMeasurement.setStartHour(time);
        hourOfMeasurement.write();
    }

    final public void writeSamplingActivationType(final int startHour) throws IOException {
        SamplingActivationType samplingActivationType = new SamplingActivationType(rtm);
        samplingActivationType.setStartHour(startHour);
        samplingActivationType.write();
    }

    private void setDayOfWeekToday(int mode) throws IOException {
        if (mode == WEEKLY_LOGGING) {
            Calendar now = new GregorianCalendar(rtm.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;  //Sunday = 0, Monday = 1, ...
            writeDayOfWeek(dayOfWeek);
        }
        if (mode == MONTHLY_LOGGING) {
            Calendar now = new GregorianCalendar(rtm.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_MONTH);
            writeDayOfWeek(dayOfWeek > 28 ? 1 : dayOfWeek);
        }
    }

    public void writeDayOfWeek(int day) throws IOException {
        DayOfWeekOrMonth dayOfWeek = new DayOfWeekOrMonth(rtm);
        dayOfWeek.setDay(day);
        dayOfWeek.write();
    }

    public void restartDataLogging(int mode) throws IOException {
        stopDataLogging();
        if (mode == PERIODIC_STEPS) {
            writeSamplingIntervalMultiplier(1);     //So the sampling interval is equal to the profile data interval
        }
        writeSamplingActivationNextHour(mode);
        setDayOfWeekToday(mode);                    //Set the day of week (to start the logging on) to today, in case of weekly/monthly logging.

        //Now restart it
        getNewOperationMode().setDataLoggingMode(mode);     //1 = periodic, 2 = weekly, 3 = monthly
        operatingMode.write();
    }

    public void writeSamplingIntervalMultiplier(int multiplier) throws IOException {
        if ((samplingIntervalMultiplier == null) || (samplingIntervalMultiplier.getMultiplier() != multiplier)) {
            samplingIntervalMultiplier = new MeasurementPeriodMultiplier(rtm);
            samplingIntervalMultiplier.setMultiplier(multiplier);
            samplingIntervalMultiplier.write();
        }
    }

    public int readSamplingIntervalMultiplier() throws IOException {
        if (samplingIntervalMultiplier == null) {
            samplingIntervalMultiplier = new MeasurementPeriodMultiplier(rtm);
            samplingIntervalMultiplier.read();
        }
        return samplingIntervalMultiplier.getMultiplier();
    }

    public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
        BatteryLifeDurationCounter counter = new BatteryLifeDurationCounter(rtm);
        counter.read();
        return counter;
    }

    public BackflowDetectionFlags readSimpleBackflowDetectionFlags(int portId) throws IOException {
        BackflowDetectionFlags backflowDetectionFlags = new BackflowDetectionFlags(rtm, portId);
        backflowDetectionFlags.read();
        return backflowDetectionFlags;
    }

    public Date readTamperDetectionDate(int port) throws IOException {
        TamperDetectionDate detectionDate = new TamperDetectionDate(rtm);
        detectionDate.setPort(port);
        detectionDate.read();
        return detectionDate.getDate();
    }

    final public ApplicationStatus readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(rtm);
            applicationStatus.read();
        }
        return applicationStatus;
    }

    final public void writeApplicationStatus(final int status) throws IOException {
        applicationStatus = new ApplicationStatus(rtm);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    public OperatingMode readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(rtm);
            operatingMode.read();
        }
        return operatingMode;
    }

    public void setBubbleUpManagement(int enable) throws IOException {
        getNewOperationMode().setBubbleUpManagement(enable);
        operatingMode.write();
    }

    public void writeOperatingMode(int mode) throws IOException {
        operatingMode = new OperatingMode(rtm);
        operatingMode.setOperationMode(mode);
        operatingMode.write();
    }

    public int getProfileIntervalInSeconds() throws IOException {
        readOperatingMode();
        if (operatingMode.isMonthlyLogging()) {
            return MONTHLY;
        }
        if (operatingMode.isWeeklyLogging()) {
            return WEEKLY;
        }
        return readSamplingIntervalInSeconds();
    }

    private int readSamplingIntervalInSeconds() throws IOException {
        if (samplingPeriod == null) {
            samplingPeriod = new SamplingPeriod(rtm);
            samplingPeriod.read();
        }
        writeSamplingIntervalMultiplier(1);
        rtm.getLogger().info("Received profile data interval: [" + samplingPeriod.getSamplingPeriodInSeconds() + " seconds] ");
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    public void writeSamplingIntervalInSeconds(int seconds) throws IOException {
        samplingPeriod = new SamplingPeriod(rtm);
        samplingPeriod.setSamplingPeriodInSeconds(seconds);
        samplingPeriod.write();
        writeSamplingIntervalMultiplier(1);     //So the sampling interval is equal to the profile data interval        
    }

    public void setLeakageDetectionPeriod(int residualOrExtreme, int inputChannel, int period) throws IOException {
        LeakageDetectionPeriod leakageDetectionPeriod = new LeakageDetectionPeriod(rtm, residualOrExtreme, inputChannel);
        leakageDetectionPeriod.setDetectionPeriod(period);
        leakageDetectionPeriod.write();
    }

    public void setLeakageThreshold(int residualOrExtreme, int inputChannel, int threshold) throws IOException {
        LeakageFlowThreshold leakageFlowThreshold = new LeakageFlowThreshold(rtm, residualOrExtreme, inputChannel);
        leakageFlowThreshold.setThresholdValue(threshold);
        leakageFlowThreshold.write();
    }

    public void writeBackflowThreshold(int threshold, int inputChannel) throws IOException {
        BackflowThreshold backflowThreshold = new BackflowThreshold(rtm, inputChannel);
        backflowThreshold.setThreshold(threshold);
        backflowThreshold.write();
    }

    public void writeBackflowDetectionPeriod(int period, int inputChannel) throws IOException {
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(rtm, inputChannel);
        detectionPeriod.setDetectionPeriod(period);
        detectionPeriod.write();
    }

    public void writeNumberOfBackflowsBeforeIndication(int number, int inputChannel) throws IOException {
        BackflowBeforeIndication backflowBeforeIndication = new BackflowBeforeIndication(rtm, inputChannel);
        backflowBeforeIndication.setNumber(number);
        backflowBeforeIndication.write();
    }

    public ProfileType readProfileType() throws IOException {
        if (profileType == null) {
            profileType = new ProfileType(rtm);
            profileType.read();
        }
        return profileType;
    }

    public void enableBackFlowDetection() throws IOException {
        getNewOperationMode().setBackFlowDetection(1);
        operatingMode.write();
    }

    public void enableEncoderCommunicationFaultDetection() throws IOException {
        getNewOperationMode().setEncoderCommunicationFaultDetection(1);
        operatingMode.write();
    }

    public void enableValveCommunicationFaultDetection() throws IOException {
        getNewOperationMode().setValveCommunicationFaultDetection(1);
        operatingMode.write();
    }

    public void enableResidualLeakDetection() throws IOException {
        getNewOperationMode().setResidualLeakDetection(1);
        operatingMode.write();
    }

    public void enableTamperDetection() throws IOException {
        getNewOperationMode().setTamperDetection(1);
        operatingMode.write();
    }

    public void enableEncoderMisreadDetection() throws IOException {
        getNewOperationMode().setEncoderMisReadDetection(1);
        operatingMode.write();
    }

    public void enableExtremeLeakDetection() throws IOException {
        getNewOperationMode().setExtremeLeakDetection(1);
        operatingMode.write();
    }

    public AlarmConfiguration readAlarmConfiguration() throws IOException {
        AlarmConfiguration configuration = new AlarmConfiguration(rtm);
        configuration.read();
        return configuration;
    }

    public void writeAlarmConfiguration(int value) throws IOException {
        AlarmConfiguration configuration = new AlarmConfiguration(rtm);
        configuration.setConfig(value);
        configuration.write();
    }

    public void setAlarmOnBackFlow() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnBackFlow(1);
        configuration.write();
    }

    public void setAlarmOnCutCable() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnCutCable(1);
        configuration.write();
    }

    public void setAlarmOnCutRegisterCable() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnCutRegisterCable(1);
        configuration.write();
    }

    public void setAlarmOnDefaultValve() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnDefaultValve(1);
        configuration.write();
    }

    public void setAlarmOnEncoderCommunicationFailure() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnEncoderCommunicationFailure(1);
        configuration.write();
    }

    public void setAlarmOnEncoderMisread() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnEncoderMisread(1);
        configuration.write();
    }

    public void setAlarmOnHighThreshold() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnHighThreshold(1);
        configuration.write();
    }

    public void setAlarmOnLowBattery() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnLowBattery(1);
        configuration.write();
    }

    public void setAlarmOnLowThreshold() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnLowThreshold(1);
        configuration.write();
    }

    public void disableAlarmOnBackFlow() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnBackFlow(0);
        getNewOperationMode().setBackFlowDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAlarmOnCutCable() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnCutCable(0);
        getNewOperationMode().setTamperDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAlarmOnCutRegisterCable() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnCutRegisterCable(0);
        getNewOperationMode().setTamperDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAlarmOnDefaultValve() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnDefaultValve(0);
        configuration.write();
    }

    public void disableAlarmOnEncoderCommunicationFailure() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnEncoderCommunicationFailure(0);
        getNewOperationMode().setEncoderCommunicationFaultDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAlarmOnEncoderMisread() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnEncoderMisread(0);
        getNewOperationMode().setEncoderMisReadDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAlarmOnHighThreshold() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnHighThreshold(0);
        getNewOperationMode().setExtremeLeakDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAlarmOnLowBattery() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnLowBattery(0);
        configuration.write();
    }

    public void disableAlarmOnLowThreshold() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnLowThreshold(0);
        getNewOperationMode().setResidualLeakDetection(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public PulseWeight readPulseWeight(int port) throws IOException {
        PulseWeight weight = new PulseWeight(rtm, port);
        weight.read();
        return weight;
    }

    public EncoderUnit readEncoderUnit(int port) throws IOException {
        EncoderUnit unit = new EncoderUnit(rtm, port);
        unit.read();
        return unit;
    }

    public LeakageDetectionStatus readLeakageDetectionStatus() throws IOException {
        LeakageDetectionStatus status = new LeakageDetectionStatus(rtm);
        status.read();
        return status;
    }

    /**
     * Depending on the module type, returns the pulse weight or the encoder unit.
     *
     * @param port: the port number, 1-based
     */
    public RtmUnit readUnit(int port) throws IOException {
        if (port > readOperatingMode().readNumberOfPorts()) {
            throw new WaveFlowException("Requested channel index [" + port + "] is not supported by the module.");
        }
        if (rtmUnits[port - 1] == null) {
            if (readProfileType().isPulse()) {
                rtmUnits[port - 1] = readPulseWeight(port);
            } else if (readProfileType().isEncoder()) {
                rtmUnits[port - 1] = readEncoderUnit(port);
            } else {
                rtmUnits[port - 1] = new RtmUnit(rtm);    //Unitless
            }
        }
        return rtmUnits[port - 1];
    }

    public void writeWakeUpChannel(int value) throws IOException {
        DriveByOrWalkByWakeUpChannel wakeUpChannel = new DriveByOrWalkByWakeUpChannel(rtm);
        wakeUpChannel.setChannel(value);
        wakeUpChannel.write();
    }

    public void setInterAnswerDelay(int hour, int minute, int second) throws IOException {
        DriveByInterAnswerDelay interAnswerDelay = new DriveByInterAnswerDelay(rtm);
        interAnswerDelay.setHours(hour);
        interAnswerDelay.setMinutes(minute);
        interAnswerDelay.setSeconds(second);
        interAnswerDelay.write();
    }

    public void writePulseWeight(int port, int scale, int multiplier, int unit) throws IOException {
        if (readProfileType().isPulse()) {
            PulseWeight pulseWeight = new PulseWeight(rtm, port);
            pulseWeight.setMultiplier(multiplier);
            pulseWeight.setScale(scale);
            pulseWeight.setUnitNumber(unit);
            pulseWeight.write();
        }
    }

    public void setMeterModel(int value, int port) throws IOException {
        if (readProfileType().isPulse()) {
            MeterModelParameter meterModel = new MeterModelParameter(rtm);
            meterModel.setPort(port);
            meterModel.setMeterModel(value);
            meterModel.write();
        }
    }

    public void writeEncoderUnit(int port, int numberOfDecimals, int unitNumber) throws IOException {
        EncoderUnit encoderUnit = new EncoderUnit(rtm, port);
        encoderUnit.setScale(numberOfDecimals - 6);
        encoderUnit.setUnitNumber(unitNumber);
        encoderUnit.write();
    }

    public void writeTOUBucketStartHour(int length, int[] startHours) throws IOException {
        TouBuckets touBuckets = new TouBuckets(rtm);
        touBuckets.setNumberOfTouBuckets(length);
        touBuckets.setStartHours(startHours);
        touBuckets.write();
    }

    public void writeStartOfPushFrameMechanism(int hour, int minute, int second) throws IOException {
        PseudoBubbleUpMechanismStartHour startHour = new PseudoBubbleUpMechanismStartHour(rtm);
        startHour.setHour(hour);
        startHour.setMinute(minute);
        startHour.setSecond(second);
        startHour.write();
    }

    public void writeEndOfPushFrameMechanism(int hour) throws IOException {
        PseudoBubbleUpPeriodEndHour end = new PseudoBubbleUpPeriodEndHour(rtm);
        end.setHour(hour);
        end.write();
    }

    public void writeTransmissionPeriod(int minutes) throws IOException {
        PseudoBubbleUpTransmissionPeriod period = new PseudoBubbleUpTransmissionPeriod(rtm);
        period.setTransmissionPeriodInMinutes(minutes);
        period.write();
    }

    public PseudoBubbleUpCommandBuffer readPushCommandBuffer() throws IOException {
        PseudoBubbleUpCommandBuffer commandBuffer = new PseudoBubbleUpCommandBuffer(rtm);
        commandBuffer.read();
        return commandBuffer;
    }

    public void writeMaxCancelTimeout(int value) throws IOException {
        PseudoBubbleUpMaxCancellationTimeout timeout = new PseudoBubbleUpMaxCancellationTimeout(rtm);
        timeout.setSeconds(value);
        timeout.write();
    }

    public void replaceCommandInBuffer(int value, int portMask, int numberOfReadings, int offset) throws IOException {
        PseudoBubbleUpCommandBuffer commandBuffer = readPushCommandBuffer();
        commandBuffer.replaceCommand(value, portMask, numberOfReadings, offset);
        commandBuffer.write();
    }

    public void writeBubbleUpConfiguration(int command, int portMask, int numberOfReadings, int offset) throws IOException {
        PseudoBubbleUpCommandBuffer config = new PseudoBubbleUpCommandBuffer(rtm);
        config.writeBubbleUpConfiguration(command, portMask, numberOfReadings, offset);            //A special command that writes all parameters
    }

    public void clearCommandBuffer() throws IOException {
        PseudoBubbleUpCommandBuffer commandBuffer = new PseudoBubbleUpCommandBuffer(rtm);
        commandBuffer.clearBuffer();
        commandBuffer.write();
    }

    public Date readBackflowDate(int port) throws IOException {
        BackflowDetectionDate detectionDate = new BackflowDetectionDate(rtm);
        detectionDate.setPort(port);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readEncoderCommFaultDate(int port) throws IOException {
        CommunicationErrorDetectionDate detectionDate = new CommunicationErrorDetectionDate(rtm, port);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readEncoderReadingErrorDate(int port) throws IOException {
        ReadingErrorDetectionDate detectionDate = new ReadingErrorDetectionDate(rtm, port);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readValveErrorDetectionDate() throws IOException {
        ValveCommunicationErrorDetectionDate detectionDate = new ValveCommunicationErrorDetectionDate(rtm);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readLowBatteryDetectionDate() throws IOException {
        BatteryLowDetectionDate detectionDate = new BatteryLowDetectionDate(rtm);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public void setAlarmWindowConfiguration(int duration, boolean activation, int granularity) throws IOException {
        AlarmWindowConfiguration configuration = new AlarmWindowConfiguration(rtm, duration, activation, granularity);
        configuration.write();
    }

    public int autoConfigAlarmRoute() throws IOException {
        RouteConfiguration configuration = new RouteConfiguration(rtm);
        configuration.set();
        return configuration.getResponse();
    }

    public void clearBackFlowFlags() throws IOException {
        //Disable back flow detection
        getNewOperationMode().setBackFlowDetection(0);
        operatingMode.write();

        //Enable it again
        operatingMode.setBackFlowDetection(1);
        operatingMode.write();
    }

    public int readNumberOfLoggedEntries() throws IOException {
        NumberOfRecords numberOfRecords = new NumberOfRecords(rtm);
        numberOfRecords.read();
        return numberOfRecords.getNumber();
    }

    public void writeNumberOfRepeaters(int number) throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(rtm);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public void writeRepeaterAddress(String address, int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(rtm, id);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public void writeRecipientAddress(String address) throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(rtm);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public OperatingMode getNewOperationMode() {
        operatingMode = new OperatingMode(rtm, 0);
        return operatingMode;
    }

    public void enableTOUBuckets() throws IOException {
        getNewOperationMode().setTouBucketsManagement(1);
        operatingMode.write();
    }

    public void disableTOUBuckets() throws IOException {
        getNewOperationMode().setTouBucketsManagement(0);
        operatingMode.write();
    }

    public void enableAllAlarms() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.enableAllAlarms();
        getNewOperationMode().setAllDetections(1);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void disableAllAlarms() throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setConfig(0);
        getNewOperationMode().setAllDetections(0);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }
}