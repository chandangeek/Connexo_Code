package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.TimeDateRTC;

import java.io.IOException;
import java.util.*;

public class ParameterFactory {

    private WaveFlow waveFlow;

    // cached
    private SamplingPeriod samplingPeriod = null;
    private ApplicationStatus applicationStatus = null;
    private ValveApplicationStatus valveApplicationStatus = null;
    private OperatingMode operatingMode = null;
    private ExtendedOperationMode extendedOperationMode = null;
    ProfileType profileType;
    private PulseWeight[] pulseWeights = new PulseWeight[4];

    private static final int DAILY = 60 * 60 * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;
    private static final int PERIODIC_STEPS = 1;
    private static final int WEEKLY_LOGGING = 2;
    private static final int MONTHLY_LOGGING = 3;

    public ParameterFactory(final WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public BackflowDetectionFlags readSimpleBackflowDetectionFlags(int portId) throws IOException {
        BackflowDetectionFlags backflowDetectionFlags = new BackflowDetectionFlags(waveFlow, portId);
        backflowDetectionFlags.read();
        return backflowDetectionFlags;
    }

    public int getTimeDurationRX() throws IOException {
        TimeDurationInRxAndTx timeDurationInRxAndTx = new TimeDurationInRxAndTx(waveFlow);
        timeDurationInRxAndTx.read();
        return timeDurationInRxAndTx.getTimeRx();
    }

    public int getTimeDurationTX() throws IOException {
        TimeDurationInRxAndTx timeDurationInRxAndTx = new TimeDurationInRxAndTx(waveFlow);
        timeDurationInRxAndTx.read();
        return timeDurationInRxAndTx.getTimeTx();
    }

    public int getNumberOfFramesInRx() throws IOException {
        NumberOfFrameInRxAndTx numberOfFrameInRxAndTx = new NumberOfFrameInRxAndTx(waveFlow);
        numberOfFrameInRxAndTx.read();
        return numberOfFrameInRxAndTx.getNumberOfFrameRx();
    }

    public int getNumberOfFramesInTx() throws IOException {
        NumberOfFrameInRxAndTx numberOfFrameInRxAndTx = new NumberOfFrameInRxAndTx(waveFlow);
        numberOfFrameInRxAndTx.read();
        return numberOfFrameInRxAndTx.getNumberOfFrameTx();
    }

    final public int readApplicationStatus() throws IOException {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(waveFlow);
            applicationStatus.read();
        }
        return applicationStatus.getStatus();
    }

    final public int readValveApplicationStatus() throws IOException {
        if (valveApplicationStatus == null) {
            valveApplicationStatus = new ValveApplicationStatus(waveFlow);
            valveApplicationStatus.read();
        }
        return valveApplicationStatus.getStatus();
    }

    //Advanced restart, sets the start moment in 3 minutes.

    final public void restartDataLogging(int mode) throws IOException {
        stopDataLogging();
        setNumberOfInputsUsed(readOperatingMode().getNumberOfInputsUsed());
        if (waveFlow.isV1()) {
            writeSamplingActivationNextHour(mode);

        } else {
            writeSamplingActivationIn3Minutes(mode);    //Checks if the logging is periodic, or weekly or monthly.
        }
        setDayOfWeekToday(mode);                            //Set the day of week (to start the logging on) to today, in case of weekly/monthly logging.

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

    //Simple restart, uses the time parameters

    final public void simpleRestartDataLogging(int mode) throws IOException {
        stopDataLogging();
        setNumberOfInputsUsed(readOperatingMode().getNumberOfInputsUsed());

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

    public int readElapsedDays() throws IOException {
        ElapsedDays days = new ElapsedDays(waveFlow);
        days.read();
        return days.getDays();
    }

    public int readNumberOfSentFrames() throws IOException {
        NumberOfSentFrames sentFrames = new NumberOfSentFrames(waveFlow);
        sentFrames.read();
        return sentFrames.getNumber();
    }

    public int readNumberOfReceivedFrames() throws IOException {
        NumberOfReceivedFrames receivedFrames = new NumberOfReceivedFrames(waveFlow);
        receivedFrames.read();
        return receivedFrames.getNumber();
    }

    private void setDayOfWeekToday(int mode) throws IOException {
        if (mode == WEEKLY_LOGGING) {
            Calendar now = new GregorianCalendar(waveFlow.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;  //Sunday = 0, Monday = 1, ...
            writeDayOfWeek(dayOfWeek);
        }
        if (mode == MONTHLY_LOGGING) {
            Calendar now = new GregorianCalendar(waveFlow.getTimeZone());
            int dayOfWeek = now.get(Calendar.DAY_OF_MONTH);
            writeDayOfWeek(dayOfWeek > 28 ? 1 : dayOfWeek);
        }
    }

    public int readDayOfWeek() throws IOException {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveFlow);
        dayOfWeek.read();
        return dayOfWeek.getDayOfWeek();
    }

    public final ProfileType readProfileType() throws IOException {
        if (profileType == null) {
            profileType = new ProfileType(waveFlow);
            profileType.read();
        }
        return profileType;
    }

    public Date readWireCutDetectionDate(int inputChannel) throws IOException {
        WireCutDetectionDate wireCutDetectionDate = new WireCutDetectionDate(waveFlow, inputChannel);
        wireCutDetectionDate.read();
        Date eventDate = wireCutDetectionDate.getEventDate();
        return (eventDate.after(new Date()) ? new Date() : eventDate);
    }

    public Date readReedFaultDetectionDate(int inputChannel) throws IOException {
        ReedFaultDetectionDate reedFaultDetectionDate = new ReedFaultDetectionDate(waveFlow, inputChannel);
        reedFaultDetectionDate.read();
        Date eventDate = reedFaultDetectionDate.getEventDate();
        return (eventDate.after(new Date()) ? new Date() : eventDate);
    }

    final public void writeApplicationStatus(final int status) throws IOException {
        applicationStatus = new ApplicationStatus(waveFlow);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    final public void writeValveApplicationStatus(final int status) throws IOException {
        valveApplicationStatus = new ValveApplicationStatus(waveFlow);
        valveApplicationStatus.setStatus(status);
        valveApplicationStatus.write();
    }

    final public OperatingMode readOperatingMode() throws IOException {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(waveFlow);
            operatingMode.read();
        }
        return operatingMode;
    }

    final public void enableDataLoggingPeriodic() throws IOException {
        readOperatingMode();
        operatingMode.setDataLoggingStepsToPeriodic();
        operatingMode.write();
    }

    final public void writeOperatingMode(final int operatingModeVal) throws IOException {
        operatingMode = new OperatingMode(waveFlow, operatingModeVal);
        operatingMode.write();
    }

    final public Date readTimeDateRTC() throws IOException {
        TimeDateRTC o = new TimeDateRTC(waveFlow);
        o.set();
        return o.getCalendar().getTime();
    }

    final public void writeTimeDateRTC(final Date date) throws IOException {
        TimeDateRTC o = new TimeDateRTC(waveFlow);
        Calendar calendar = Calendar.getInstance(waveFlow.getTimeZone());
        calendar.setTime(date);
        o.setCalendar(calendar);
        o.set();
    }

    /**
     * The queried sampling period is only valid for periodic measurements, otherwise, it's a weekly / monthly interval.
     */
    final public int readSamplingPeriod() throws IOException {
        readOperatingMode();
        if (operatingMode.isMonthlyMeasurement()) {
            return MONTHLY;
        }
        if (operatingMode.isWeeklyMeasurement()) {
            return WEEKLY;
        }
        if (samplingPeriod == null) {
            samplingPeriod = new SamplingPeriod(waveFlow);
            samplingPeriod.read();
            waveFlow.getLogger().info("Received profile data interval: [" + samplingPeriod.getSamplingPeriodInSeconds() + " seconds] ");
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    /**
     * Return the sampling period for the periodic time step logging, even if that mode is not enabled.
     */
    final public int readRawSamplingPeriod() throws IOException {
        if (samplingPeriod == null) {
            samplingPeriod = new SamplingPeriod(waveFlow);
            samplingPeriod.read();
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    /**
     * This byte contains a flag indicating the back flow detection method.
     *
     * @return byte containing the flags
     * @throws IOException
     */
    final public ExtendedOperationMode readExtendedOperationMode() throws IOException {
        if (extendedOperationMode == null) {
            extendedOperationMode = new ExtendedOperationMode(waveFlow);
            extendedOperationMode.read();
        }
        return extendedOperationMode;
    }

    final public void writeSamplingPeriod(final int samplingPeriodInSeconds) throws IOException {
        samplingPeriod = new SamplingPeriod(waveFlow);
        samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
        samplingPeriod.write();
    }

    final public void writeSamplingActivationIn3Minutes(int mode) throws IOException {
        Calendar cal = Calendar.getInstance(waveFlow.getTimeZone());
        int minute = (cal.get(Calendar.MINUTE) + 3);
        int hour = cal.get(Calendar.HOUR_OF_DAY) + (minute >= 60 ? 1 : 0);
        hour = hour % 24;
        writeStartHourOfMeasurement(hour, mode);        //Also checks if it's periodic or weekly/monthly
        writeStartMinuteOfMeasurement(minute % 60);     //Set start in 3 minutes
    }

    final public void writeSamplingActivationNextHour(int mode) throws IOException {
        Calendar cal = Calendar.getInstance(waveFlow.getTimeZone());
        int hour = cal.get(Calendar.HOUR_OF_DAY) + 1;
        hour = hour % 24;
        writeStartHourOfMeasurement(hour, mode);        //Also checks if it's periodic or weekly/monthly
    }

    public int readSamplingActivationType() throws IOException {
        SamplingActivationType samplingActivationType = new SamplingActivationType(waveFlow);
        samplingActivationType.read();
        return samplingActivationType.getStartHour();
    }

    //This is the start hour for the data logging in periodic time steps

    final public void writeSamplingActivationType(final int startHour) throws IOException {
        SamplingActivationType samplingActivationType = new SamplingActivationType(waveFlow);
        samplingActivationType.setStartHour(startHour);
        samplingActivationType.write();
    }

    final public int getProfileIntervalInSeconds() throws IOException {
        return readSamplingPeriod();
    }

    final public BatteryLifeDurationCounter readBatteryLifeDurationCounter() throws IOException {
        BatteryLifeDurationCounter o = new BatteryLifeDurationCounter(waveFlow);
        o.read();
        return o;
    }

    final public Date readBatteryLifeDateEnd() throws IOException {
        BatteryLifeDateEnd o = new BatteryLifeDateEnd(waveFlow);
        o.read();
        return (o.getCalendar().getTime());
    }

    public void writeDayOfWeek(int day) throws IOException {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveFlow);
        dayOfWeek.setDayOfWeek(day);
        dayOfWeek.write();
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

    public void writeTimeOfMeasurement(int time) throws IOException {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveFlow);
        timeOfMeasurement.setTimeOfMeasurement(time);
        timeOfMeasurement.write();
    }

    public int readTimeOfMeasurement() throws IOException {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveFlow);
        timeOfMeasurement.read();
        return timeOfMeasurement.getTimeOfMeasurement();
    }

    public boolean writePulseWeight(int inputChannelIndex, int unitNumber, int weight) throws IOException {
        if (!waveFlow.isV1()) {
            ProfileType profileType = readProfileType();
            if (!profileType.isOfType4Iputs() && (inputChannelIndex > 2)) {
                return false;   //Only the "4 inputs" Waveflow module supports more than 2 input channels.
            }
        }
        if (inputChannelIndex < 1 || inputChannelIndex > 4) {
            return false;
        }
        if (unitNumber < -3 || unitNumber > 3) {
            return false;
        }
        if (weight < 1 || weight > 15) {
            return false;
        }
        PulseWeight pulseWeight = new PulseWeight(waveFlow, inputChannelIndex);
        pulseWeight.setUnitScaler(unitNumber);
        pulseWeight.setWeight(weight);
        pulseWeight.write();
        return true;
    }

    public PulseWeight readPulseWeight(int inputChannelIndex) throws IOException {
        if (inputChannelIndex == 0) {
            inputChannelIndex = 1;
        }
        if (inputChannelIndex > readOperatingMode().getNumberOfInputsUsed()) {
            throw new WaveFlowException("Requested channel index [" + inputChannelIndex + "] is not supported by the module.");
        }
        if (pulseWeights[inputChannelIndex - 1] == null) {
            PulseWeight pulseWeight = new PulseWeight(waveFlow, inputChannelIndex);
            pulseWeight.read();
            pulseWeights[inputChannelIndex - 1] = pulseWeight;
        }
        return pulseWeights[inputChannelIndex - 1];
    }

    public void writeStartHourOfMeasurement(int time) throws IOException {
        if (readOperatingMode().isPeriodicMeasurement()) {
            writeSamplingActivationType(time);
        } else if (readOperatingMode().isMonthlyMeasurement() || readOperatingMode().isWeeklyMeasurement()) {
            writeTimeOfMeasurement(time);
        }
    }

    public int readStartHourOfMeasurement() throws IOException {
        if (readOperatingMode().isMonthlyMeasurement() || readOperatingMode().isWeeklyMeasurement()) {
            return readTimeOfMeasurement();
        } else {
            return readSamplingActivationType();
        }
    }

    public void writeStartHourOfMeasurement(int time, int mode) throws IOException {
        if (mode == PERIODIC_STEPS) {
            writeSamplingActivationType(time);
        } else if (mode == WEEKLY_LOGGING || mode == MONTHLY_LOGGING) {
            writeTimeOfMeasurement(time);
        }
    }

    public int readStartMinuteOfMeasurement() throws IOException {
        DataLoggingMinuteOfMeasurement minuteOfMeasurement = new DataLoggingMinuteOfMeasurement(waveFlow);
        minuteOfMeasurement.read();
        return minuteOfMeasurement.getMinuteOfMeasurement();
    }

    public void writeStartMinuteOfMeasurement(int minute) throws IOException {
        DataLoggingMinuteOfMeasurement minuteOfMeasurement = new DataLoggingMinuteOfMeasurement(waveFlow);
        minuteOfMeasurement.setMinuteOfMeasurement(minute);
        minuteOfMeasurement.write();
    }

    public int readHourOfDailyIndexStorage() throws IOException {
        HourOfDailyIndexStorage hourOfDailyIndexStorage = new HourOfDailyIndexStorage(waveFlow);
        hourOfDailyIndexStorage.read();
        return hourOfDailyIndexStorage.getHour();
    }

    public void setHourOfDailyIndexStorage(int hour) throws IOException {
        HourOfDailyIndexStorage hourOfDailyIndexStorage = new HourOfDailyIndexStorage(waveFlow);
        hourOfDailyIndexStorage.setHour(hour);
        hourOfDailyIndexStorage.write();
    }

    public int readLeakageDetectionPeriod(int residualOrExtreme, int inputChannel) throws IOException {
        LeakageDetectionPeriod leakageDetectionPeriod = new LeakageDetectionPeriod(waveFlow, residualOrExtreme, inputChannel);
        leakageDetectionPeriod.read();
        return leakageDetectionPeriod.getDetectionPeriod();
    }

    public int readResidualLeakageDetectionPeriod(int input) throws IOException {
        return readLeakageDetectionPeriod(0, input);
    }

    public int readExtremeLeakageDetectionPeriod(int input) throws IOException {
        return readLeakageDetectionPeriod(1, input);
    }

    public void setLeakageDetectionPeriod(int residualOrExtreme, int inputChannel, int period) throws IOException {
        LeakageDetectionPeriod leakageDetectionPeriod = new LeakageDetectionPeriod(waveFlow, residualOrExtreme, inputChannel);
        leakageDetectionPeriod.setDetectionPeriod(period);
        leakageDetectionPeriod.write();
    }

    public int readLeakageThreshold(int residualOrExtreme, int inputChannel) throws IOException {
        LeakageFlowThreshold leakageFlowThreshold = new LeakageFlowThreshold(waveFlow, residualOrExtreme, inputChannel);
        leakageFlowThreshold.read();
        return leakageFlowThreshold.getThresholdValue();
    }

    public int readResidualLeakageThreshold(int inputChannel) throws IOException {
        return readLeakageThreshold(0, inputChannel);
    }

    public int readExtremeLeakageThreshold(int inputChannel) throws IOException {
        return readLeakageThreshold(1, inputChannel);
    }

    public void setLeakageThreshold(int residualOrExtreme, int inputChannel, int threshold) throws IOException {
        LeakageFlowThreshold leakageFlowThreshold = new LeakageFlowThreshold(waveFlow, residualOrExtreme, inputChannel);
        leakageFlowThreshold.setThresholdValue(threshold);
        leakageFlowThreshold.write();
    }

    public int readMeasurementStep() throws IOException {
        LeakageDetectionMeasurementStep measurementStep = new LeakageDetectionMeasurementStep(waveFlow);
        measurementStep.read();
        return measurementStep.getMeasurementStep();
    }

    public void writeMeasurementStep(int step) throws IOException {
        LeakageDetectionMeasurementStep measurementStep = new LeakageDetectionMeasurementStep(waveFlow);
        measurementStep.setMeasurementStep(step);
        measurementStep.write();
    }

    public int readAdvancedBackflowThreshold(int inputChannel) throws IOException {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            throw new WaveFlowException("The module doesn't support advanced back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, false, advanced);
        backflowThreshold.read();
        return backflowThreshold.getThreshold();
    }

    public void writeAdvancedBackflowThreshold(int threshold, int inputChannel) throws IOException {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            throw new WaveFlowException("The module doesn't support advanced back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, false, advanced);
        backflowThreshold.setThreshold(threshold);
        backflowThreshold.write();
    }

    public int readSimpleBackflowThreshold(int inputChannel) throws IOException {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            throw new WaveFlowException("The module doesn't support simple back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, simple, false);
        backflowThreshold.read();
        return backflowThreshold.getThreshold();
    }

    public void writeSimpleBackflowThreshold(int threshold, int inputChannel) throws IOException {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            throw new WaveFlowException("The module doesn't support simple back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, simple, false);
        backflowThreshold.setThreshold(threshold);
        backflowThreshold.write();
    }

    public int readSimpleBackflowDetectionPeriod(int inputChannel) throws IOException {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            throw new WaveFlowException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, simple, false);
        detectionPeriod.read();
        return detectionPeriod.getDetectionPeriod();
    }


    public void writeSimpleBackflowDetectionPeriod(int period, int inputChannel) throws IOException {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            throw new WaveFlowException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, simple, false);
        detectionPeriod.setDetectionPeriod(period);
        detectionPeriod.write();
    }

    public int readAdvancedBackflowDetectionPeriod(int inputChannel) throws IOException {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            throw new WaveFlowException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, false, advanced);
        detectionPeriod.read();
        return detectionPeriod.getDetectionPeriod();
    }

    public void writeAdvancedBackflowDetectionPeriod(int period, int inputChannel) throws IOException {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            throw new WaveFlowException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, false, advanced);
        detectionPeriod.setDetectionPeriod(period);
        detectionPeriod.write();
    }

    public void enablePushFrames() throws IOException {
        extendedOperationMode = readExtendedOperationMode();
        extendedOperationMode.enablePushFrames();
        extendedOperationMode.write();
    }

    public void disablePushFrames() throws IOException {
        extendedOperationMode = readExtendedOperationMode();
        extendedOperationMode.disablePushFrames();
        extendedOperationMode.write();
    }

    public String readStartOfPushFrameMechanism() throws IOException {
        StartOfPushMechanism startOfPushMechanism = new StartOfPushMechanism(waveFlow);
        startOfPushMechanism.read();
        return startOfPushMechanism.getHour() + ":" + startOfPushMechanism.getMinute() + ":" + startOfPushMechanism.getSecond();
    }

    public void writeStartOfPushFrameMechanism(int hour, int minute, int second) throws IOException {
        StartOfPushMechanism startOfPushMechanism = new StartOfPushMechanism(waveFlow);
        startOfPushMechanism.setHour(hour);
        startOfPushMechanism.setMinute(minute);
        startOfPushMechanism.setSecond(second);
        startOfPushMechanism.write();
    }

    public int readTransmissionPeriod() throws IOException {
        TransmissionPeriod period = new TransmissionPeriod(waveFlow);
        period.read();
        return period.getTransmissionPeriodInMinutes();
    }

    public void writeTransmissionPeriod(int minutes) throws IOException {
        TransmissionPeriod period = new TransmissionPeriod(waveFlow);
        period.setTransmissionPeriodInMinutes(minutes);
        period.write();
    }

    public int readMaxCancelTimeout(int value) throws IOException {
        MaxCancellationTimeout timeout = new MaxCancellationTimeout(waveFlow);
        timeout.read();
        return timeout.getSeconds();
    }

    public void writeMaxCancelTimeout(int value) throws IOException {
        MaxCancellationTimeout timeout = new MaxCancellationTimeout(waveFlow);
        timeout.setSeconds(value);
        timeout.write();
    }

    public PushCommandBuffer readPushCommandBuffer() throws IOException {
        PushCommandBuffer commandBuffer = new PushCommandBuffer(waveFlow);
        commandBuffer.read();
        return commandBuffer;
    }

    public void addCommandToBuffer(int value) throws IOException {
        PushCommandBuffer commandBuffer = readPushCommandBuffer();
        commandBuffer.replaceCommand(value);
        commandBuffer.write();
    }

    public void clearCommandBuffer() throws IOException {
        PushCommandBuffer commandBuffer = new PushCommandBuffer(waveFlow);
        commandBuffer.clearBuffer();
        commandBuffer.write();
    }

    public int readNumberOfRepeaters() throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveFlow);
        numberOfRepeaters.read();
        return numberOfRepeaters.getNumber();
    }

    public void setNumberOfInputsUsed(int number) throws IOException {
        readOperatingMode();
        operatingMode.setNumberOfInputsUsed(number);
        operatingMode.write();
    }

    public void writeNumberOfRepeaters(int number) throws IOException {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveFlow);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public String readRepeaterAddress(int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveFlow, id);
        repeaterAddress.read();
        return repeaterAddress.getAddress();
    }

    public void writeRepeaterAddress(String address, int id) throws IOException {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveFlow, id);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public String readRecipientAddress() throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(waveFlow);
        recipientAddress.read();
        return recipientAddress.getAddress();
    }

    public void writeRecipientAddress(String address) throws IOException {
        RecipientAddress recipientAddress = new RecipientAddress(waveFlow);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public void writeAlarmConfigurationByte(int alarm) throws IOException {
        AlarmConfig config = new AlarmConfig(waveFlow);
        config.setAlarmConfig(alarm);
        config.write();
    }

    public AlarmConfig readAlarmConfiguration() throws IOException {
        AlarmConfig config = new AlarmConfig(waveFlow);
        config.read();
        return config;
    }

    public void sendAlarmOnWirecutDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnWirecutDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnBatteryEnd() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnBatteryEnd();
        alarmConfig.write();
    }

    public void sendAlarmOnLowLeakDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnLeakLowDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnHighLeakDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnLeakHighDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnBackflowDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnBackflowDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnValveWirecut() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnValveWirecut();
        alarmConfig.write();
    }

    public void sendAlarmOnValveCloseFault() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnCloseFaultOfValve();
        alarmConfig.write();
    }

    public void sendAlarmOnThresholdDetectionOfCreditAmount() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnCreditDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnWirecutDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnWirecutDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnBatteryEnd() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnBatteryEnd();
        alarmConfig.write();
    }

    public void disableAlarmOnLowLeakDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnLeakLowDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnHighLeakDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnLeakHighDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnBackflowDetection() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnBackflowDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnValveWirecut() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnValveWirecut();
        alarmConfig.write();
    }

    public void disableAlarmOnValveCloseFault() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnCloseFaultOfValve();
        alarmConfig.write();
    }

    public void disableAlarmOnThresholdDetectionOfCreditAmount() throws IOException {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnCreditDetection();
        alarmConfig.write();
    }

    public void disableAllAlarms() throws IOException {
        AlarmConfig config = new AlarmConfig(waveFlow);
        config.setAlarmConfig(0x00);
        config.write();
    }

    public void sendAllAlarms() throws IOException {
        AlarmConfig config = new AlarmConfig(waveFlow);
        config.setAlarmConfig(0xFF);
        config.write();
    }

    public void stopDataLogging() throws IOException {
        readOperatingMode();
        operatingMode.stopDataLogging();
        operatingMode.write();
    }

    public int readBackflowDetectionMethod(int mode) throws IOException {
        readExtendedOperationMode();
        return extendedOperationMode.usingFlowRateMethodForBackFlowDetection() ? 1 : 0;
    }

    public void writeBackflowDetectionMethod(int mode) throws IOException {
        readExtendedOperationMode();
        if (mode == 0) {
            extendedOperationMode.enableVolumeMethod();
        } else if (mode == 1) {
            extendedOperationMode.enableFlowRateMethod();
        }
        extendedOperationMode.write();
    }

    public AlarmFramesTimeAssignement readAlarmFramesTimeAssignement() throws IOException {
        AlarmFramesTimeAssignement assignement = new AlarmFramesTimeAssignement(waveFlow);
        assignement.read();
        return assignement;
    }

    public int readTimeSlotGranularity() throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.read();
        return assignement.getTimeSlotGranularity();
    }

    public void setTimeSlotGranularity(int minutes) throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.setTimeSlotGranularity(minutes);
        assignement.write();
    }


    public int readTimeSlotDuration() throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.read();
        return assignement.getTimeSlotDuration();
    }

    public void setTimeSlotDuration(int duration) throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.setTimeSlotDuration(duration);
        assignement.write();
    }

    public int readTimeSlotMechanismActivation() throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.read();
        return assignement.getTimeSlotActivation();
    }

    public void enableTimeSlotMechanism() throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.enableTimeSlotMechanism();
        assignement.write();
    }

    public void disableTimeSlotMechanism() throws IOException {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.disableTimeSlotMechanism();
        assignement.write();
    }

    public void writeBubbleUpConfiguration(int value) throws IOException {
        PushCommandBuffer buffer = new PushCommandBuffer(waveFlow);
        buffer.writeBubbleUpConfiguration(value);
    }
}