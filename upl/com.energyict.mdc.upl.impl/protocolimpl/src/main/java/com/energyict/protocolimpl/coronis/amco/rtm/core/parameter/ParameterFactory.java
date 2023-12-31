package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.RouteConfiguration;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ParameterFactory {

    private final RTM rtm;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    //Cached
    private ApplicationStatus applicationStatus = null;
    private OperatingMode operatingMode = null;
    private ProfileType profileType = null;
    private SamplingPeriod samplingPeriod = null;
    private RtmUnit[] rtmUnits = new RtmUnit[4];
    private MeasurementPeriodMultiplier samplingIntervalMultiplier = null;
    private BatteryLifeDurationCounter batteryLifeDurationCounter = null;

    private static final int DAILY = 60 * 60 * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = DAILY * 31;

    private static final int PERIODIC_STEPS = 1;
    private static final int WEEKLY_LOGGING = 2;
    private static final int MONTHLY_LOGGING = 3;

    public ParameterFactory(final RTM rtm, PropertySpecService propertySpecService, NlsService nlsService) {
        this.rtm = rtm;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    public Date readTimeDateRTC() throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(this.propertySpecService, rtm, this.nlsService);
        timeDateRTC.read();
        return timeDateRTC.getCalendar().getTime();
    }

    public void writeTimeDateRTC(Date date) throws IOException {
        TimeDateRTC timeDateRTC = new TimeDateRTC(this.propertySpecService, rtm, this.nlsService);
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
        HourOfMeasurement hourOfMeasurement = new HourOfMeasurement(this.propertySpecService, rtm, this.nlsService);
        hourOfMeasurement.setStartHour(time);
        hourOfMeasurement.write();
    }

    public int readTimeOfMeasurement() throws IOException {
        HourOfMeasurement hourOfMeasurement = new HourOfMeasurement(this.propertySpecService, rtm, this.nlsService);
        hourOfMeasurement.read();
        return hourOfMeasurement.getStartHour();
    }

    final public void writeSamplingActivationType(final int startHour) throws IOException {
        SamplingActivationType samplingActivationType = new SamplingActivationType(this.propertySpecService, rtm, this.nlsService);
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
        DayOfWeekOrMonth dayOfWeek = new DayOfWeekOrMonth(this.propertySpecService, rtm, this.nlsService);
        dayOfWeek.setDay(day);
        dayOfWeek.write();
    }

    public int readDayOfWeek() throws IOException {
        DayOfWeekOrMonth dayOfWeek = new DayOfWeekOrMonth(this.propertySpecService, rtm, this.nlsService);
        dayOfWeek.read();
        return dayOfWeek.getDay();
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
            samplingIntervalMultiplier = new MeasurementPeriodMultiplier(this.propertySpecService, rtm, this.nlsService);
            samplingIntervalMultiplier.setMultiplier(multiplier);
            samplingIntervalMultiplier.write();
        }
    }

    public int readSamplingIntervalMultiplier() throws IOException {
        if (samplingIntervalMultiplier == null) {
            samplingIntervalMultiplier = new MeasurementPeriodMultiplier(this.propertySpecService, rtm, this.nlsService);
            samplingIntervalMultiplier.read();
        }
        return samplingIntervalMultiplier.getMultiplier();
    }

    public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
        if (batteryLifeDurationCounter == null) {
            batteryLifeDurationCounter = new BatteryLifeDurationCounter(this.propertySpecService, rtm, this.nlsService);
            batteryLifeDurationCounter.read();
    }
        return batteryLifeDurationCounter;
    }

    public void setBatteryLifeDurationCounter(BatteryLifeDurationCounter batteryLifeDurationCounter) {
        this.batteryLifeDurationCounter = batteryLifeDurationCounter;
    }

    public BackflowDetectionFlags readSimpleBackflowDetectionFlags(int portId) throws IOException {
        BackflowDetectionFlags backflowDetectionFlags = new BackflowDetectionFlags(this.propertySpecService, rtm, portId, this.nlsService);
        backflowDetectionFlags.read();
        return backflowDetectionFlags;
    }

    public Date readTamperDetectionDate(int port) throws IOException {
        TamperDetectionDate detectionDate = new TamperDetectionDate(this.propertySpecService, rtm, this.nlsService);
        detectionDate.setPort(port);
        detectionDate.read();
        return detectionDate.getDate();
    }

    final public ApplicationStatus readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(this.propertySpecService, rtm, this.nlsService);
            applicationStatus.read();
        }
        return applicationStatus;
    }

    final public void writeApplicationStatus(final int status) throws IOException {
        applicationStatus = new ApplicationStatus(this.propertySpecService, rtm, this.nlsService);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public OperatingMode readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(this.propertySpecService, rtm, this.nlsService);
            operatingMode.read();
        }
        return operatingMode;
    }

    public void setOperatingMode(OperatingMode operatingMode) {
        this.operatingMode = operatingMode;
    }

    public void setBubbleUpManagement(int enable) throws IOException {
        getNewOperationMode().setBubbleUpManagement(enable);
        operatingMode.write();
    }

    public void writeOperatingMode(int mode) throws IOException {
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
            samplingPeriod = new SamplingPeriod(this.propertySpecService, rtm, this.nlsService);
            samplingPeriod.read();
            rtm.getLogger().info("Received profile data interval: [" + samplingPeriod.getSamplingPeriodInSeconds() + " seconds] ");
            writeSamplingIntervalMultiplier(1);
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    public void writeSamplingIntervalInSeconds(int seconds) throws IOException {
        samplingPeriod = new SamplingPeriod(this.propertySpecService, rtm, this.nlsService);
        samplingPeriod.setSamplingPeriodInSeconds(seconds);
        samplingPeriod.write();
        writeSamplingIntervalMultiplier(1);     //So the sampling interval is equal to the profile data interval
    }

    public void setLeakageDetectionPeriod(int residualOrExtreme, int inputChannel, int period) throws IOException {
        LeakageDetectionPeriod leakageDetectionPeriod = new LeakageDetectionPeriod(this.propertySpecService, rtm, residualOrExtreme, inputChannel, this.nlsService);
        leakageDetectionPeriod.setDetectionPeriod(period);
        leakageDetectionPeriod.write();
    }

    public void setLeakageThreshold(int residualOrExtreme, int inputChannel, int threshold) throws IOException {
        LeakageFlowThreshold leakageFlowThreshold = new LeakageFlowThreshold(this.propertySpecService, rtm, residualOrExtreme, inputChannel, this.nlsService);
        leakageFlowThreshold.setThresholdValue(threshold);
        leakageFlowThreshold.write();
    }

    public void writeBackflowThreshold(int threshold, int inputChannel) throws IOException {
        BackflowThreshold backflowThreshold = new BackflowThreshold(this.propertySpecService, rtm, inputChannel, this.nlsService);
        backflowThreshold.setThreshold(threshold);
        backflowThreshold.write();
    }

    public void writeBackflowDetectionPeriod(int period, int inputChannel) throws IOException {
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(this.propertySpecService, rtm, inputChannel, this.nlsService);
        detectionPeriod.setDetectionPeriod(period);
        detectionPeriod.write();
    }

    public void writeNumberOfBackflowsBeforeIndication(int number, int inputChannel) throws IOException {
        BackflowBeforeIndication backflowBeforeIndication = new BackflowBeforeIndication(this.propertySpecService, rtm, inputChannel, this.nlsService);
        backflowBeforeIndication.setNumber(number);
        backflowBeforeIndication.write();
    }

    public ProfileType readProfileType() throws IOException {
        if (profileType == null) {
            profileType = new ProfileType(this.propertySpecService, rtm, this.nlsService);
            profileType.read();
        }
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public AlarmConfiguration readAlarmConfiguration() throws IOException {
        AlarmConfiguration configuration = new AlarmConfiguration(this.propertySpecService, rtm, this.nlsService);
        configuration.read();
        return configuration;
    }

    public void writeAlarmConfiguration(int value) throws IOException {
        AlarmConfiguration configuration = new AlarmConfiguration(this.propertySpecService, rtm, this.nlsService);
        configuration.setConfig(value);
        configuration.write();
    }

    public void setAlarmOnBackFlow(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnBackFlow(enable);
        getNewOperationMode().setBackFlowDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void setAlarmOnCutCable(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnCutCable(enable);
        getNewOperationMode().setTamperDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void setAlarmOnCutRegisterCable(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnCutRegisterCable(enable);
        getNewOperationMode().setTamperDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void setAlarmOnDefaultValve(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnDefaultValve(enable);
        configuration.write();
    }

    public void setAlarmOnEncoderCommunicationFailure(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnEncoderCommunicationFailure(enable);
        getNewOperationMode().setEncoderCommunicationFaultDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void setAlarmOnEncoderMisread(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnEncoderMisread(enable);
        getNewOperationMode().setEncoderMisReadDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void setAlarmOnHighThreshold(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnHighThreshold(enable);
        getNewOperationMode().setExtremeLeakDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public void setAlarmOnLowBattery(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnLowBattery(enable);
        configuration.write();
    }

    public void setAlarmOnLowThreshold(int enable) throws IOException {
        AlarmConfiguration configuration = readAlarmConfiguration();
        configuration.setAlarmOnLowThreshold(enable);
        getNewOperationMode().setResidualLeakDetection(enable);
        configuration.setOperatingMode(operatingMode);
        configuration.write();
    }

    public PulseWeight readPulseWeight(int port) throws IOException {
        PulseWeight weight = new PulseWeight(this.propertySpecService, rtm, port, this.nlsService);
        weight.read();
        return weight;
    }

    public EncoderUnit readEncoderUnit(int port) throws IOException {
        EncoderUnit unit = new EncoderUnit(this.propertySpecService, rtm, port, this.nlsService);
        unit.read();
        return unit;
    }

    public LeakageDetectionStatus readLeakageDetectionStatus() throws IOException {
        LeakageDetectionStatus status = new LeakageDetectionStatus(this.propertySpecService, rtm, this.nlsService);
        status.read();
        return status;
    }

    /**
     * Depending on the module type, returns the pulse weight or the encoder unit.
     * This method is no longer used, only the units in the generic header are used.
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
                rtmUnits[port - 1] = new RtmUnit(this.propertySpecService, rtm, this.nlsService);    //Unitless
            }
        }
        return rtmUnits[port - 1];
    }

    public void writeWakeUpChannel(int value) throws IOException {
        DriveByOrWalkByWakeUpChannel wakeUpChannel = new DriveByOrWalkByWakeUpChannel(this.propertySpecService, rtm, this.nlsService);
        wakeUpChannel.setChannel(value);
        wakeUpChannel.write();
    }

    public void setInterAnswerDelay(int hour, int minute, int second) throws IOException {
        DriveByInterAnswerDelay interAnswerDelay = new DriveByInterAnswerDelay(this.propertySpecService, rtm, this.nlsService);
        interAnswerDelay.setHours(hour);
        interAnswerDelay.setMinutes(minute);
        interAnswerDelay.setSeconds(second);
        interAnswerDelay.write();
    }

    public void writePulseWeight(int port, int scale, int multiplier, int unit) throws IOException {
        if (readProfileType().isPulse()) {
            PulseWeight pulseWeight = new PulseWeight(this.propertySpecService, rtm, port, this.nlsService);
            pulseWeight.setMultiplier(multiplier);
            pulseWeight.setScale(scale);
            pulseWeight.setUnitNumber(unit);
            pulseWeight.write();
        }
    }

    public void setMeterModel(int value, int port) throws IOException {
        if (readProfileType().isPulse()) {
            MeterModelParameter meterModel = new MeterModelParameter(this.propertySpecService, rtm, this.nlsService);
            meterModel.setPort(port);
            meterModel.setMeterModel(value);
            meterModel.write();
        }
    }

    public void writeEncoderUnit(int port, int numberOfDecimals, int unitNumber) throws IOException {
        EncoderUnit encoderUnit = new EncoderUnit(this.propertySpecService, rtm, port, this.nlsService);
        encoderUnit.setScale(numberOfDecimals - 6);
        encoderUnit.setUnitNumber(unitNumber);
        encoderUnit.write();
    }

    public void writeTOUBucketStartHour(int length, int[] startHours) throws IOException {
        TouBuckets touBuckets = new TouBuckets(this.propertySpecService, rtm, this.nlsService);
        touBuckets.setNumberOfTouBuckets(length);
        touBuckets.setStartHours(startHours);
        touBuckets.write();
    }

    public void writeStartOfPushFrameMechanism(int hour, int minute, int second) throws IOException {
        PseudoBubbleUpMechanismStartHour startHour = new PseudoBubbleUpMechanismStartHour(this.propertySpecService, rtm, this.nlsService);
        startHour.setHour(hour);
        startHour.setMinute(minute);
        startHour.setSecond(second);
        startHour.write();
    }

    public String readStartOfPushFrameMechanism() throws IOException {
        PseudoBubbleUpMechanismStartHour startHour = new PseudoBubbleUpMechanismStartHour(this.propertySpecService, rtm, this.nlsService);
        startHour.read();
        return startHour.getHour() + ":" + startHour.getMinute() + ":" + startHour.getSecond();
    }

    public void writeEndOfPushFrameMechanism(int hour) throws IOException {
        PseudoBubbleUpPeriodEndHour end = new PseudoBubbleUpPeriodEndHour(this.propertySpecService, rtm, this.nlsService);
        end.setHour(hour);
        end.write();
    }

    public void writeTransmissionPeriod(int minutes) throws IOException {
        PseudoBubbleUpTransmissionPeriod period = new PseudoBubbleUpTransmissionPeriod(this.propertySpecService, rtm, this.nlsService);
        period.setTransmissionPeriodInMinutes(minutes);
        period.write();
    }

    public PseudoBubbleUpCommandBuffer readPushCommandBuffer() throws IOException {
        PseudoBubbleUpCommandBuffer commandBuffer = new PseudoBubbleUpCommandBuffer(this.propertySpecService, rtm, this.nlsService);
        commandBuffer.read();
        return commandBuffer;
    }

    public void writeMaxCancelTimeout(int value) throws IOException {
        PseudoBubbleUpMaxCancellationTimeout timeout = new PseudoBubbleUpMaxCancellationTimeout(this.propertySpecService, rtm, this.nlsService);
        timeout.setSeconds(value);
        timeout.write();
    }

    public void replaceCommandInBuffer(int value, int portMask, int numberOfReadings, int offset) throws IOException {
        PseudoBubbleUpCommandBuffer commandBuffer = readPushCommandBuffer();
        commandBuffer.replaceCommand(value, portMask, numberOfReadings, offset);
        commandBuffer.write();
    }

    public void writeBubbleUpConfiguration(int command, int portMask, int numberOfReadings, int offset, int transmissionPeriod) throws IOException {
        PseudoBubbleUpCommandBuffer config = new PseudoBubbleUpCommandBuffer(this.propertySpecService, rtm, this.nlsService);
        config.writeBubbleUpConfiguration(command, portMask, numberOfReadings, offset, transmissionPeriod);            //A special command that writes all parameters
    }

    public void clearCommandBuffer() throws IOException {
        PseudoBubbleUpCommandBuffer commandBuffer = new PseudoBubbleUpCommandBuffer(this.propertySpecService, rtm, this.nlsService);
        commandBuffer.clearBuffer();
        commandBuffer.write();
    }

    public Date readBackflowDate(int port) throws IOException {
        BackflowDetectionDate detectionDate = new BackflowDetectionDate(this.propertySpecService, rtm, this.nlsService);
        detectionDate.setPort(port);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readEncoderCommFaultDate(int port) throws IOException {
        CommunicationErrorDetectionDate detectionDate = new CommunicationErrorDetectionDate(this.propertySpecService, rtm, port, this.nlsService);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readEncoderReadingErrorDate(int port) throws IOException {
        ReadingErrorDetectionDate detectionDate = new ReadingErrorDetectionDate(this.propertySpecService, rtm, port, this.nlsService);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readValveErrorDetectionDate() throws IOException {
        ValveCommunicationErrorDetectionDate detectionDate = new ValveCommunicationErrorDetectionDate(this.propertySpecService, rtm, this.nlsService);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public Date readLowBatteryDetectionDate() throws IOException {
        BatteryLowDetectionDate detectionDate = new BatteryLowDetectionDate(this.propertySpecService, rtm, this.nlsService);
        detectionDate.read();
        return detectionDate.getDate();
    }

    public void setAlarmWindowConfiguration(int duration, boolean activation, int granularity) throws IOException {
        AlarmWindowConfiguration configuration = new AlarmWindowConfiguration(this.propertySpecService, rtm, duration, activation, granularity, this.nlsService);
        configuration.write();
    }

    public int autoConfigAlarmRoute() throws IOException {
        RouteConfiguration configuration = new RouteConfiguration(this.propertySpecService, rtm, this.nlsService);
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
        NumberOfRecords numberOfRecords = new NumberOfRecords(this.propertySpecService, rtm, this.nlsService);
        numberOfRecords.read();
        return numberOfRecords.getNumber();
    }

    public void writeNumberOfRepeaters(int number) throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(this.propertySpecService, rtm, this.nlsService);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public void writeRepeaterAddress(String address, int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(this.propertySpecService, rtm, id, this.nlsService);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public void writeRecipientAddress(String address) throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(this.propertySpecService, rtm, this.nlsService);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public OperatingMode getNewOperationMode() {
        operatingMode = new OperatingMode(this.propertySpecService, rtm, 0, this.nlsService);
        return operatingMode;
    }

    public void setTOUBuckets(int enable) throws IOException {
        getNewOperationMode().setTouBucketsManagement(enable);
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