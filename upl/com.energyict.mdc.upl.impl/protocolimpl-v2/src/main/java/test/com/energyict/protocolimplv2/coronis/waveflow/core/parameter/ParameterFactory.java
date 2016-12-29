package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import test.com.energyict.protocolimplv2.coronis.common.WaveFlowException;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.TimeDateRTC;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ParameterFactory {

    protected WaveFlow waveFlow;

    // cached
    private SamplingPeriod samplingPeriod = null;
    private ApplicationStatus applicationStatus = null;
    private ValveApplicationStatus valveApplicationStatus = null;
    private OperatingMode operatingMode = null;
    private BatteryLifeDurationCounter batteryLifeDurationCounter = null;
    private ExtendedOperationMode extendedOperationMode = null;
    private ProfileType profileType = null;
    private PulseWeight[] pulseWeights = new PulseWeight[4];

    private static final int DAILY = 60 * 60 * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = DAILY * 31;
    private static final int PERIODIC_STEPS = 1;
    private static final int WEEKLY_LOGGING = 2;
    private static final int MONTHLY_LOGGING = 3;

    public ParameterFactory(final WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public BackflowDetectionFlags readSimpleBackflowDetectionFlags(int portId) {
        BackflowDetectionFlags backflowDetectionFlags = new BackflowDetectionFlags(waveFlow, portId);
        backflowDetectionFlags.read();
        return backflowDetectionFlags;
    }

    public int getTimeDurationRX() {
        TimeDurationInRxAndTx timeDurationInRxAndTx = new TimeDurationInRxAndTx(waveFlow);
        timeDurationInRxAndTx.read();
        return timeDurationInRxAndTx.getTimeRx();
    }

    public int getTimeDurationTX() {
        TimeDurationInRxAndTx timeDurationInRxAndTx = new TimeDurationInRxAndTx(waveFlow);
        timeDurationInRxAndTx.read();
        return timeDurationInRxAndTx.getTimeTx();
    }

    public int getNumberOfFramesInRx() {
        NumberOfFrameInRxAndTx numberOfFrameInRxAndTx = new NumberOfFrameInRxAndTx(waveFlow);
        numberOfFrameInRxAndTx.read();
        return numberOfFrameInRxAndTx.getNumberOfFrameRx();
    }

    public int getNumberOfFramesInTx() {
        NumberOfFrameInRxAndTx numberOfFrameInRxAndTx = new NumberOfFrameInRxAndTx(waveFlow);
        numberOfFrameInRxAndTx.read();
        return numberOfFrameInRxAndTx.getNumberOfFrameTx();
    }

    public int getWakeUpSystemStatusWord() {
        WakeUpSystemStatusWord wakeUpSystemStatusWord = new WakeUpSystemStatusWord(waveFlow);
        wakeUpSystemStatusWord.read();
        return wakeUpSystemStatusWord.getValue();
    }

    public int getDefaultWakeUpPeriod() {
        DefaultWakeUpPeriod parameter = new DefaultWakeUpPeriod(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public int getStartTimeForTimeWindow1() {
        StartTimeForTimeWindow1 parameter = new StartTimeForTimeWindow1(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public int getWakeUpPeriodForTimeWindow1() {
        WakeUpPeriodForTimeWindow1 parameter = new WakeUpPeriodForTimeWindow1(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public int getStartTimeForTimeWindow2() {
        StartTimeForTimeWindow2 parameter = new StartTimeForTimeWindow2(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public int getWakeUpPeriodForTimeWindow2() {
        WakeUpPeriodForTimeWindow2 parameter = new WakeUpPeriodForTimeWindow2(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public int getEnableTimeWindowsByDayOfWeek() {
        EnableTimeWindowsByDayOfWeek parameter = new EnableTimeWindowsByDayOfWeek(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public int getEnableWakeUpPeriodsByDayOfWeek() {
        EnableWakeUpPeriodsByDayOfWeek parameter = new EnableWakeUpPeriodsByDayOfWeek(waveFlow);
        parameter.read();
        return parameter.getValue();
    }

    public void setEnableWakeUpPeriodsByDayOfWeek(int value) {
        EnableWakeUpPeriodsByDayOfWeek parameter = new EnableWakeUpPeriodsByDayOfWeek(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setEnableTimeWindowsByDayOfWeek(int value) {
        EnableTimeWindowsByDayOfWeek parameter = new EnableTimeWindowsByDayOfWeek(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setWakeUpPeriodForTimeWindow2(int value) {
        WakeUpPeriodForTimeWindow2 parameter = new WakeUpPeriodForTimeWindow2(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setStartTimeForTimeWindow2(int value) {
        StartTimeForTimeWindow2 parameter = new StartTimeForTimeWindow2(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setWakeUpPeriodForTimeWindow1(int value) {
        WakeUpPeriodForTimeWindow1 parameter = new WakeUpPeriodForTimeWindow1(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setStartTimeForTimeWindow1(int value) {
        StartTimeForTimeWindow1 parameter = new StartTimeForTimeWindow1(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setDefaultWakeUpPeriod(int value) {
        DefaultWakeUpPeriod parameter = new DefaultWakeUpPeriod(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public void setWakeUpSystemStatusWord(int value) {
        WakeUpSystemStatusWord parameter = new WakeUpSystemStatusWord(waveFlow);
        parameter.setValue(value);
        parameter.write();
    }

    public final int readApplicationStatus() {
        if (applicationStatus == null) {
            applicationStatus = new ApplicationStatus(waveFlow);
            applicationStatus.read();
        }
        return applicationStatus.getStatus();
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public void setApplicationStatus(int applicationStatus) {
        this.applicationStatus = new ApplicationStatus(waveFlow);
        this.applicationStatus.setStatus(applicationStatus);
    }

    public final int readValveApplicationStatus() {
        if (valveApplicationStatus == null) {
            valveApplicationStatus = new ValveApplicationStatus(waveFlow);
            valveApplicationStatus.read();
        }
        return valveApplicationStatus.getStatus();
    }

    //Advanced restart, sets the start moment in 3 minutes.

    public final void restartDataLogging(int mode) {
        stopDataLogging();
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

    public final void simpleRestartDataLogging(int mode) {
        stopDataLogging();

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

    public int readElapsedDays() {
        ElapsedDays days = new ElapsedDays(waveFlow);
        days.read();
        return days.getDays();
    }

    public int readNumberOfSentFrames() {
        NumberOfSentFrames sentFrames = new NumberOfSentFrames(waveFlow);
        sentFrames.read();
        return sentFrames.getNumber();
    }

    public int readNumberOfReceivedFrames() {
        NumberOfReceivedFrames receivedFrames = new NumberOfReceivedFrames(waveFlow);
        receivedFrames.read();
        return receivedFrames.getNumber();
    }

    public int readNumberOfRelayedFramesV1() {
        NumberOfRelayedFramesV1 sentFrames = new NumberOfRelayedFramesV1(waveFlow);
        sentFrames.read();
        return sentFrames.getNumber();
    }

    private void setDayOfWeekToday(int mode) {
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

    public int readDayOfWeek() {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveFlow);
        dayOfWeek.read();
        return dayOfWeek.getDayOfWeek();
    }

    public final ProfileType readProfileType() {
        if (profileType == null) {
            profileType = new ProfileType(waveFlow);
            profileType.read();
        }
        return profileType;
    }

    public Date readWireCutDetectionDate(int inputChannel) {
        WireCutDetectionDate wireCutDetectionDate = new WireCutDetectionDate(waveFlow, inputChannel);
        wireCutDetectionDate.read();
        Date eventDate = wireCutDetectionDate.getEventDate();
        return (eventDate.after(new Date()) ? new Date() : eventDate);
    }

    public Date readReedFaultDetectionDate(int inputChannel) {
        ReedFaultDetectionDate reedFaultDetectionDate = new ReedFaultDetectionDate(waveFlow, inputChannel);
        reedFaultDetectionDate.read();
        Date eventDate = reedFaultDetectionDate.getEventDate();
        return (eventDate.after(new Date()) ? new Date() : eventDate);
    }

    public final void writeApplicationStatus(final int status) {
        applicationStatus = new ApplicationStatus(waveFlow);
        applicationStatus.setStatus(status);
        applicationStatus.write();
    }

    public void writeApplicationStatusBit(int bit) {
        readApplicationStatus();
        applicationStatus.resetBit(bit);
        applicationStatus.write();
    }

    public void writeValveApplicationStatusBit(int bit) {
        readValveApplicationStatus();
        valveApplicationStatus.resetBit(bit);
        valveApplicationStatus.write();
    }

    public final void writeValveApplicationStatus(final int status) {
        valveApplicationStatus = new ValveApplicationStatus(waveFlow);
        valveApplicationStatus.setStatus(status);
        valveApplicationStatus.write();
    }

    public OperatingMode readOperatingMode() {
        if (operatingMode == null) {
            operatingMode = new OperatingMode(waveFlow);
            operatingMode.read();
        }
        return operatingMode;
    }

    public void setOperatingMode(OperatingMode operatingMode) {
        this.operatingMode = operatingMode;
    }

    public void setOperatingMode(int operatingMode) {
        this.operatingMode = new OperatingMode(waveFlow, operatingMode);
    }

    public void setExtendedOperationMode(int extendedOperationMode) {
        this.extendedOperationMode = new ExtendedOperationMode(waveFlow, extendedOperationMode);
    }

    public final void enableDataLoggingPeriodic() {
        readOperatingMode();
        operatingMode.setDataLoggingStepsToPeriodic();
        operatingMode.write();
    }

    public void writeOperatingMode(final int operatingModeVal) {
        operatingMode = new OperatingMode(waveFlow, operatingModeVal);
        operatingMode.write();
    }

    public void writeWorkingMode(final int workingMode, int mask) {
        operatingMode = new OperatingMode(waveFlow, workingMode & 0xFF);
        operatingMode.setWorkingMode(workingMode);
        operatingMode.setParameterId(null);
        operatingMode.setMask(mask);
        operatingMode.write();
        operatingMode = null;   //Read it out again next time
    }

    public Date readTimeDateRTC() {
        TimeDateRTC o = new TimeDateRTC(waveFlow);
        o.set();
        return o.getCalendar().getTime();
    }

    public void writeTimeDateRTC(final Date date) {
        TimeDateRTC o = new TimeDateRTC(waveFlow);
        Calendar calendar = Calendar.getInstance(waveFlow.getTimeZone());
        calendar.setTime(date);
        o.setCalendar(calendar);
        o.set();
    }

    /**
     * The queried sampling period is only valid for periodic measurements, otherwise, it's a weekly / monthly interval.
     */
    public final int readSamplingPeriod() {
        int interval = 0;
        if (!waveFlow.isV1()) {
            interval = readRawSamplingPeriod();           //In case of Waveflow V2, the operation mode is also returned and cached
        } else {
            readOperatingMode();                          //In case of V1, read it out
        }
        if (operatingMode.isMonthlyMeasurement()) {
            return MONTHLY;
        }
        if (operatingMode.isWeeklyMeasurement()) {
            return WEEKLY;
        }
        if (waveFlow.isV1()) {
            interval = readRawSamplingPeriod();
        }
        return interval;
    }

    /**
     * Return the sampling period for the periodic time step logging, even if that mode is not enabled.
     */
    public final int readRawSamplingPeriod() {
        if (samplingPeriod == null) {
            samplingPeriod = new SamplingPeriod(waveFlow);
            samplingPeriod.read();
        }
        return samplingPeriod.getSamplingPeriodInSeconds();
    }

    public void setSamplingPeriod(SamplingPeriod dataloggingMeasurementPeriod) {
        this.samplingPeriod = dataloggingMeasurementPeriod;
    }

    /**
     * This byte contains a flag indicating the back flow detection method.
     *
     * @return byte containing the flags
     * @throws java.io.IOException
     */
    public final ExtendedOperationMode readExtendedOperationMode() {
        if (extendedOperationMode == null) {
            extendedOperationMode = new ExtendedOperationMode(waveFlow);
            extendedOperationMode.read();
        }
        return extendedOperationMode;
    }

    public final void writeSamplingPeriod(final int samplingPeriodInSeconds) {
        samplingPeriod = new SamplingPeriod(waveFlow);
        samplingPeriod.setSamplingPeriodInSeconds(samplingPeriodInSeconds);
        samplingPeriod.write();
    }

    public final void writeSamplingActivationIn3Minutes(int mode) {
        Calendar cal = Calendar.getInstance(waveFlow.getTimeZone());
        int minute = (cal.get(Calendar.MINUTE) + 3);
        int hour = cal.get(Calendar.HOUR_OF_DAY) + (minute >= 60 ? 1 : 0);
        hour = hour % 24;
        writeStartHourOfMeasurement(hour, mode);        //Also checks if it's periodic or weekly/monthly
        writeStartMinuteOfMeasurement(minute % 60);     //Set start in 3 minutes
    }

    public final void writeSamplingActivationNextHour(int mode) {
        Calendar cal = Calendar.getInstance(waveFlow.getTimeZone());
        int hour = cal.get(Calendar.HOUR_OF_DAY) + 1;
        hour = hour % 24;
        writeStartHourOfMeasurement(hour, mode);        //Also checks if it's periodic or weekly/monthly
    }

    public int readSamplingActivationType() {
        SamplingActivationType samplingActivationType = new SamplingActivationType(waveFlow);
        samplingActivationType.read();
        return samplingActivationType.getStartHour();
    }

    //This is the start hour for the data logging in periodic time steps

    public final void writeSamplingActivationType(final int startHour) {
        SamplingActivationType samplingActivationType = new SamplingActivationType(waveFlow);
        samplingActivationType.setStartHour(startHour);
        samplingActivationType.write();
    }

    public final int getProfileIntervalInSeconds() {
        return readSamplingPeriod();
    }

    public double readBatteryLifeDurationCounter() {
        if (batteryLifeDurationCounter == null) {
            batteryLifeDurationCounter = new BatteryLifeDurationCounter(waveFlow);
            batteryLifeDurationCounter.read();
        }
        return batteryLifeDurationCounter.remainingBatteryLife();
    }

    public final void setBatteryLifeDurationCounter(int shortLifeCounter) {
        batteryLifeDurationCounter = new BatteryLifeDurationCounter(waveFlow, shortLifeCounter);
    }

    public final Date readBatteryLifeDateEnd() {
        BatteryLifeDateEnd o = new BatteryLifeDateEnd(waveFlow);
        o.read();
        return (o.getCalendar().getTime());
    }

    public void writeDayOfWeek(int day) {
        DataLoggingDayOfWeek dayOfWeek = new DataLoggingDayOfWeek(waveFlow);
        dayOfWeek.setDayOfWeek(day);
        dayOfWeek.write();
    }

    public void writeWeeklyDataLogging() {
        stopDataLogging();
        operatingMode.setDataLoggingToOnceAWeek();
        operatingMode.write();
    }

    public void writeMonthlyDataLogging() {
        stopDataLogging();
        operatingMode.setDataLoggingToOnceAMonth();
        operatingMode.write();
    }

    public void writePeriodicTimeStepDataLogging() {
        stopDataLogging();
        operatingMode.setDataLoggingStepsToPeriodic();
        operatingMode.write();
    }

    public void writeTimeOfMeasurement(int time) {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveFlow);
        timeOfMeasurement.setTimeOfMeasurement(time);
        timeOfMeasurement.write();
    }

    public int readTimeOfMeasurement() {
        DataLoggingTimeOfMeasurement timeOfMeasurement = new DataLoggingTimeOfMeasurement(waveFlow);
        timeOfMeasurement.read();
        return timeOfMeasurement.getTimeOfMeasurement();
    }

    public boolean writePulseWeight(int inputChannelIndex, int unitNumber, int weight) {
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
        pulseWeights[inputChannelIndex - 1] = pulseWeight;
        return true;
    }

    /**
     * Read out the pulse weight for a certain port
     * The value is cached for further use
     *
     * @param inputChannelIndex indicates which port (one based!)
     * @return the pulse weight for that port
     * @throws java.io.IOException
     */
    public PulseWeight readPulseWeight(int inputChannelIndex) {
        if (inputChannelIndex == 0) {
            inputChannelIndex = 1;
        }
        if (pulseWeights[inputChannelIndex - 1] == null) {
            PulseWeight pulseWeight = new PulseWeight(waveFlow, inputChannelIndex);
            pulseWeight.read();
            pulseWeights[inputChannelIndex - 1] = pulseWeight;
        }
        return pulseWeights[inputChannelIndex - 1];
    }

    public int readStartHourOfMeasurement() {
        if (readOperatingMode().isMonthlyMeasurement() || readOperatingMode().isWeeklyMeasurement()) {
            return readTimeOfMeasurement();
        } else {
            return readSamplingActivationType();
        }
    }

    public void writeStartHourOfMeasurement(int time, int mode) {
        if (mode == PERIODIC_STEPS) {
            writeSamplingActivationType(time);
        } else if (mode == WEEKLY_LOGGING || mode == MONTHLY_LOGGING) {
            writeTimeOfMeasurement(time);
        }
    }

    public int readStartMinuteOfMeasurement() {
        DataLoggingMinuteOfMeasurement minuteOfMeasurement = new DataLoggingMinuteOfMeasurement(waveFlow);
        minuteOfMeasurement.read();
        return minuteOfMeasurement.getMinuteOfMeasurement();
    }

    public void writeStartMinuteOfMeasurement(int minute) {
        DataLoggingMinuteOfMeasurement minuteOfMeasurement = new DataLoggingMinuteOfMeasurement(waveFlow);
        minuteOfMeasurement.setMinuteOfMeasurement(minute);
        minuteOfMeasurement.write();
    }

    public int readHourOfDailyIndexStorage() {
        HourOfDailyIndexStorage hourOfDailyIndexStorage = new HourOfDailyIndexStorage(waveFlow);
        hourOfDailyIndexStorage.read();
        return hourOfDailyIndexStorage.getHour();
    }

    public void setHourOfDailyIndexStorage(int hour) {
        HourOfDailyIndexStorage hourOfDailyIndexStorage = new HourOfDailyIndexStorage(waveFlow);
        hourOfDailyIndexStorage.setHour(hour);
        hourOfDailyIndexStorage.write();
    }

    public int readLeakageDetectionPeriod(int residualOrExtreme, int inputChannel) {
        LeakageDetectionPeriod leakageDetectionPeriod = new LeakageDetectionPeriod(waveFlow, residualOrExtreme, inputChannel);
        leakageDetectionPeriod.read();
        return leakageDetectionPeriod.getDetectionPeriod();
    }

    public int readResidualLeakageDetectionPeriod(int input) {
        return readLeakageDetectionPeriod(0, input);
    }

    public int readExtremeLeakageDetectionPeriod(int input) {
        return readLeakageDetectionPeriod(1, input);
    }

    public void setLeakageDetectionPeriod(int residualOrExtreme, int inputChannel, int period) {
        LeakageDetectionPeriod leakageDetectionPeriod = new LeakageDetectionPeriod(waveFlow, residualOrExtreme, inputChannel);
        leakageDetectionPeriod.setDetectionPeriod(period);
        leakageDetectionPeriod.write();
    }

    public int readLeakageThreshold(int residualOrExtreme, int inputChannel) {
        LeakageFlowThreshold leakageFlowThreshold = new LeakageFlowThreshold(waveFlow, residualOrExtreme, inputChannel);
        leakageFlowThreshold.read();
        return leakageFlowThreshold.getThresholdValue();
    }

    public int readResidualLeakageThreshold(int inputChannel) {
        return readLeakageThreshold(0, inputChannel);
    }

    public int readExtremeLeakageThreshold(int inputChannel) {
        return readLeakageThreshold(1, inputChannel);
    }

    public void setLeakageThreshold(int residualOrExtreme, int inputChannel, int threshold) {
        LeakageFlowThreshold leakageFlowThreshold = new LeakageFlowThreshold(waveFlow, residualOrExtreme, inputChannel);
        leakageFlowThreshold.setThresholdValue(threshold);
        leakageFlowThreshold.write();
    }

    public int readMeasurementStep() {
        LeakageDetectionMeasurementStep measurementStep = new LeakageDetectionMeasurementStep(waveFlow);
        measurementStep.read();
        return measurementStep.getMeasurementStep();
    }

    public void writeMeasurementStep(int step) {
        LeakageDetectionMeasurementStep measurementStep = new LeakageDetectionMeasurementStep(waveFlow);
        measurementStep.setMeasurementStep(step);
        measurementStep.write();
    }

    public int readAdvancedBackflowThreshold(int inputChannel) {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            // throw new NoSuchRegisterException("The module doesn't support advanced back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, false, advanced);
        backflowThreshold.read();
        return backflowThreshold.getThreshold();
    }

    public void writeAdvancedBackflowThreshold(int threshold, int inputChannel) {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            throw createWaveFlowException("The module doesn't support advanced back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, false, advanced);
        backflowThreshold.setThreshold(threshold);
        backflowThreshold.write();
    }

    public int readSimpleBackflowThreshold(int inputChannel) {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            // throw new NoSuchRegisterException("The module doesn't support simple back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, simple, false);
        backflowThreshold.read();
        return backflowThreshold.getThreshold();
    }

    public void writeSimpleBackflowThreshold(int threshold, int inputChannel) {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            throw createWaveFlowException("The module doesn't support simple back flow detection.");
        }
        BackflowThreshold backflowThreshold = new BackflowThreshold(waveFlow, inputChannel, simple, false);
        backflowThreshold.setThreshold(threshold);
        backflowThreshold.write();
    }

    public int readSimpleBackflowDetectionPeriod(int inputChannel) {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            // throw new NoSuchRegisterException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, simple, false);
        detectionPeriod.read();
        return detectionPeriod.getDetectionPeriod();
    }


    public void writeSimpleBackflowDetectionPeriod(int period, int inputChannel) {
        boolean simple = waveFlow.getParameterFactory().readProfileType().supportsSimpleBackflowDetection();
        if (!simple) {
            throw createWaveFlowException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, simple, false);
        detectionPeriod.setDetectionPeriod(period);
        detectionPeriod.write();
    }

    public int readAdvancedBackflowDetectionPeriod(int inputChannel) {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            // throw new NoSuchRegisterException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, false, advanced);
        detectionPeriod.read();
        return detectionPeriod.getDetectionPeriod();
    }

    public void writeAdvancedBackflowDetectionPeriod(int period, int inputChannel) {
        boolean advanced = waveFlow.getParameterFactory().readProfileType().supportsAdvancedBackflowDetection();
        if (!advanced) {
            throw createWaveFlowException("The module doesn't support back flow detection.");
        }
        BackflowDetectionPeriod detectionPeriod = new BackflowDetectionPeriod(waveFlow, inputChannel, false, advanced);
        detectionPeriod.setDetectionPeriod(period);
        detectionPeriod.write();
    }

    protected ProtocolRuntimeException createWaveFlowException(String description) {
        WaveFlowException e = new WaveFlowException(description);
        return CommunicationException.unexpectedResponse(e);
    }

    public void enablePushFrames() {
        extendedOperationMode = readExtendedOperationMode();
        extendedOperationMode.enablePushFrames();
        extendedOperationMode.write();
    }

    public void disablePushFrames() {
        extendedOperationMode = readExtendedOperationMode();
        extendedOperationMode.disablePushFrames();
        extendedOperationMode.write();
    }

    public String readStartOfPushFrameMechanism() {
        StartOfPushMechanism startOfPushMechanism = new StartOfPushMechanism(waveFlow);
        startOfPushMechanism.read();
        return startOfPushMechanism.getHour() + ":" + startOfPushMechanism.getMinute() + ":" + startOfPushMechanism.getSecond();
    }

    public void writeStartOfPushFrameMechanism(int hour, int minute, int second) {
        StartOfPushMechanism startOfPushMechanism = new StartOfPushMechanism(waveFlow);
        startOfPushMechanism.setHour(hour);
        startOfPushMechanism.setMinute(minute);
        startOfPushMechanism.setSecond(second);
        startOfPushMechanism.write();
    }

    public int readTransmissionPeriod() {
        TransmissionPeriod period = new TransmissionPeriod(waveFlow);
        period.read();
        return period.getTransmissionPeriodInMinutes();
    }

    public void writeTransmissionPeriod(int minutes) {
        TransmissionPeriod period = new TransmissionPeriod(waveFlow);
        period.setTransmissionPeriodInMinutes(minutes);
        period.write();
    }

    public int readMaxCancelTimeout(int value) {
        MaxCancellationTimeout timeout = new MaxCancellationTimeout(waveFlow);
        timeout.read();
        return timeout.getSeconds();
    }

    public void writeMaxCancelTimeout(int value) {
        MaxCancellationTimeout timeout = new MaxCancellationTimeout(waveFlow);
        timeout.setSeconds(value);
        timeout.write();
    }

    public PushCommandBuffer readPushCommandBuffer() {
        PushCommandBuffer commandBuffer = new PushCommandBuffer(waveFlow);
        commandBuffer.read();
        return commandBuffer;
    }

    public void addCommandToBuffer(int value) {
        PushCommandBuffer commandBuffer = readPushCommandBuffer();
        commandBuffer.replaceCommand(value);
        commandBuffer.write();
    }

    public void clearCommandBuffer() {
        PushCommandBuffer commandBuffer = new PushCommandBuffer(waveFlow);
        commandBuffer.clearBuffer();
        commandBuffer.write();
    }

    public int readNumberOfRepeaters() {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveFlow);
        numberOfRepeaters.read();
        return numberOfRepeaters.getNumber();
    }

    public void setNumberOfInputsUsed(int number) {
        readOperatingMode();
        operatingMode.setNumberOfInputsUsed(number);
        operatingMode.write();
    }

    public void writeNumberOfRepeaters(int number) {
        NumberOfRepeaters numberOfRepeaters = new NumberOfRepeaters(waveFlow);
        numberOfRepeaters.setNumber(number);
        numberOfRepeaters.write();
    }

    public String readRepeaterAddress(int id) {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveFlow, id);
        repeaterAddress.read();
        return repeaterAddress.getAddress();
    }

    public void writeRepeaterAddress(String address, int id) {
        RepeaterAddress repeaterAddress = new RepeaterAddress(waveFlow, id);
        repeaterAddress.setAddress(address);
        repeaterAddress.write();
    }

    public String readRecipientAddress() {
        RecipientAddress recipientAddress = new RecipientAddress(waveFlow);
        recipientAddress.read();
        return recipientAddress.getAddress();
    }

    public void writeRecipientAddress(String address) {
        RecipientAddress recipientAddress = new RecipientAddress(waveFlow);
        recipientAddress.setAddress(address);
        recipientAddress.write();
    }

    public void writeAlarmConfigurationByte(int alarm) {
        AlarmConfig config = new AlarmConfig(waveFlow);
        config.setAlarmConfig(alarm);
        config.write();
    }

    public int readAlarmConfigurationValue() {
        return readAlarmConfiguration().getAlarmConfig();
    }

    public AlarmConfig readAlarmConfiguration() {
        AlarmConfig config = new AlarmConfig(waveFlow);
        config.read();
        return config;
    }

    public void sendAlarmOnWirecutDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnWirecutDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnBatteryEnd() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnBatteryEnd();
        alarmConfig.write();
    }

    public void sendAlarmOnLowLeakDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnLeakLowDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnHighLeakDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnLeakHighDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnBackflowDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnBackflowDetection();
        alarmConfig.write();
    }

    public void sendAlarmOnValveWirecut() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnValveWirecut();
        alarmConfig.write();
    }

    public void sendAlarmOnValveCloseFault() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnCloseFaultOfValve();
        alarmConfig.write();
    }

    public void sendAlarmOnThresholdDetectionOfCreditAmount() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.sendAlarmOnCreditDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnWirecutDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnWirecutDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnBatteryEnd() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnBatteryEnd();
        alarmConfig.write();
    }

    public void disableAlarmOnLowLeakDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnLeakLowDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnHighLeakDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnLeakHighDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnBackflowDetection() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnBackflowDetection();
        alarmConfig.write();
    }

    public void disableAlarmOnValveWirecut() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnValveWirecut();
        alarmConfig.write();
    }

    public void disableAlarmOnValveCloseFault() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnCloseFaultOfValve();
        alarmConfig.write();
    }

    public void disableAlarmOnThresholdDetectionOfCreditAmount() {
        AlarmConfig alarmConfig = readAlarmConfiguration();
        alarmConfig.disableAlarmOnCreditDetection();
        alarmConfig.write();
    }

    public void stopDataLogging() {
        readOperatingMode();
        operatingMode.stopDataLogging();
        operatingMode.write();
    }

    public int readBackflowDetectionMethod(int mode) {
        readExtendedOperationMode();
        return extendedOperationMode.usingFlowRateMethodForBackFlowDetection() ? 1 : 0;
    }

    public void writeBackflowDetectionMethod(int mode) {
        readExtendedOperationMode();
        if (mode == 0) {
            extendedOperationMode.enableVolumeMethod();
        } else if (mode == 1) {
            extendedOperationMode.enableFlowRateMethod();
        }
        extendedOperationMode.write();
    }

    public AlarmFramesTimeAssignement readAlarmFramesTimeAssignement() {
        AlarmFramesTimeAssignement assignement = new AlarmFramesTimeAssignement(waveFlow);
        assignement.read();
        return assignement;
    }

    public int readTimeSlotGranularity() {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.read();
        return assignement.getTimeSlotGranularity();
    }

    public void setTimeSlotGranularity(int minutes) {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.setTimeSlotGranularity(minutes);
        assignement.write();
    }


    public int readTimeSlotDuration() {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.read();
        return assignement.getTimeSlotDuration();
    }

    public void setTimeSlotDuration(int duration) {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.setTimeSlotDuration(duration);
        assignement.write();
    }

    public int readTimeSlotMechanismActivation() {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.read();
        return assignement.getTimeSlotActivation();
    }

    public void enableTimeSlotMechanism() {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.enableTimeSlotMechanism();
        assignement.write();
    }

    public void disableTimeSlotMechanism() {
        AlarmFramesTimeAssignement assignement = readAlarmFramesTimeAssignement();
        assignement.disableTimeSlotMechanism();
        assignement.write();
    }

    public void writeBubbleUpConfiguration(int value, int transmissionPeriod) {
        PushCommandBuffer buffer = new PushCommandBuffer(waveFlow);
        buffer.writeBubbleUpConfiguration(value, transmissionPeriod);
    }
}