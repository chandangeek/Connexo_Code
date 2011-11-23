package com.energyict.protocolimpl.coronis.waveflow.waveflowV2;

import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.*;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ProfileDataReader {

    private WaveFlow waveFlowV2;
    private int inputsUsed = 0;
    private int interval = 0;

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;
    private static final int STEPS = 29;                   //Max number of profile data entries in 1 frame!

    public ProfileDataReader(WaveFlow waveFlowV2) {
        this.waveFlowV2 = waveFlowV2;
    }

    private Date getTimeStampOfNewestRecordMonthly(Date toDate, Date lastLoggedValue) throws IOException {
        Calendar lastLogged = Calendar.getInstance(waveFlowV2.getTimeZone());
        lastLogged.setTime(lastLoggedValue);
        lastLogged.setLenient(true);

        //Go back month by month until you have the date closest to the toDate.
        while (lastLogged.getTime().after(toDate)) {
            lastLogged.add(Calendar.MONTH, -1);
        }
        return lastLogged.getTime();
    }

    final ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        if (toDate == null || toDate.after(new Date())) {
            toDate = new Date();
        }
        boolean monthly = (getProfileIntervalInSeconds() >= MONTHLY);

        int nrOfIntervals = 4;
        if (!waveFlowV2.usesInitialRFCommand()) {
            nrOfIntervals = getNrOfIntervals(lastReading, toDate);
        }

        Date lastLoggedValue = new Date();
        List<Long[]> rawValues = new ArrayList<Long[]>();

        long startOffset = -1;
        long initialOffset = -1;
        int indexFirst = 0;
        boolean daily = false;
        DailyConsumption dailyConsumption = null;

        if (waveFlowV2.getInitialRFCommand() == 0x06) {
            ExtendedIndexReading extendedIndexReading = waveFlowV2.getRadioCommandFactory().readExtendedIndexConfiguration();
            lastLoggedValue = extendedIndexReading.getDateOfLastLoggedValue();
            rawValues = extendedIndexReading.getLast4LoggedIndexes();
            initialOffset = 0;
        } else if (waveFlowV2.getInitialRFCommand() == 0x27) {
            daily = true;
            dailyConsumption = waveFlowV2.getRadioCommandFactory().readDailyConsumption();
            lastLoggedValue = dailyConsumption.getLastLoggedReading();
            initialOffset = 0;
        } else {
            int initialNrOfIntervals = nrOfIntervals;

            //Get the profile data for all selected input channels, in case of periodic/weekly/monthly measuring.
            for (int i = 0; i < getNumberOfInputsUsed(); i++) {
                nrOfIntervals = initialNrOfIntervals;
                int counter = 0;
                Long[] values = new Long[0];
                while (nrOfIntervals > 0) {
                    if (startOffset == -1) {
                        ExtendedDataloggingTable table = waveFlowV2.getRadioCommandFactory().readExtendedDataloggingTable(i + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), toDate);
                        values = ProtocolTools.concatLongArrays(values, table.getReadingsInputs());
                        startOffset = table.getOffset();
                        initialOffset = startOffset;
                        lastLoggedValue = table.getMostRecentRecordTimeStamp();
                        indexFirst = table.getNumberOfFirstIndex();
                        if (table.getNrOfReadings()[i] < getSteps(nrOfIntervals)) {
                            break;   //To avoid invalid offsets in the next iteration
                        }
                    } else {
                        long offset;
                        if (startOffset == 0) {
                            offset = indexFirst - (startOffset + getSteps(nrOfIntervals) * counter);
                        } else {
                            offset = (startOffset - getSteps(nrOfIntervals) * counter);
                        }
                        offset = (offset == indexFirst ? 0 : offset);   //Offset = 0 represents the highest record number
                        if (offset < 0) {
                            break;
                        }
                        ExtendedDataloggingTable table = waveFlowV2.getRadioCommandFactory().readExtendedDataloggingTable(i + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), toDate, offset);
                        values = ProtocolTools.concatLongArrays(values, table.getReadingsInputs());
                        if (table.getNrOfReadings()[i] < getSteps(nrOfIntervals)) {
                            break;   //To avoid invalid offsets in the next iteration
                        }
                    }
                    counter++;
                    nrOfIntervals -= getSteps(nrOfIntervals);
                }
                rawValues.add(values);
            }
        }
        return parseProfileData(!daily, includeEvents, daily, monthly, lastLoggedValue, initialOffset, indexFirst, dailyConsumption, lastReading, toDate, rawValues);
    }

    //The parsing of the values.
    //This method can be used after a request or for a bubble up frame containing daily profile data.

    public ProfileData parseProfileData(boolean requestsAllowed, boolean includeEvents, boolean daily, boolean monthly, Date lastLoggedValue, long initialOffset, int indexFirst, DailyConsumption dailyConsumption, Date lastReading, Date toDate, List<Long[]> rawValues) throws IOException {
        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        TimeZone timeZone = waveFlowV2.getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setLenient(true);

        int channelId = 0;
        for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
            Unit unit = waveFlowV2.getPulseWeight(inputId, requestsAllowed).getUnit();
            ChannelInfo channelInfo = new ChannelInfo(channelId++, String.valueOf(inputId + 1), unit);       //Channel name: 1, 2, 3 or 4
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE)); //4 bytes long, signed value
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);

        // initialize calendar
        if (waveFlowV2.usesInitialRFCommand()) {
            calendar.setTime(lastLoggedValue);
        } else {
            if (!daily & !monthly) {
                calendar.setTime(getTimeStampOfNewestRecord(lastLoggedValue, (initialOffset == 0 ? 0 : indexFirst - initialOffset)));
            } else if (daily) {
                calendar.setTime(dailyConsumption.getLastLoggedReading());
            } else if (monthly) {
                calendar.setTime(getTimeStampOfNewestRecordMonthly(toDate, lastLoggedValue));
            }
        }

        if (monthly || (getProfileIntervalInSeconds() == WEEKLY)) {
            calendar = roundTimeStamps(calendar, HOURLY);
        } else {
            calendar = roundTimeStamps(calendar, getProfileIntervalInSeconds());
        }

        int nrOfReadings;
        if (!daily) {
            nrOfReadings = rawValues.get(0).length;
        } else {
            nrOfReadings = getNumberOfDailyValues();  //a fixed amount for the daily values table, see documentation
        }

        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (int index = 0; index < nrOfReadings; index++) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();

            if (!daily) {
                for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
                    int weight = waveFlowV2.getPulseWeight(inputId, requestsAllowed).getWeight();
                    BigDecimal bd = new BigDecimal(weight * rawValues.get(inputId)[index]);
                    intervalValues.add(new IntervalValue(bd, 0, 0));    //The module doesn't send any information about the value's status..
                }
            } else {
                for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
                    int weight = waveFlowV2.getPulseWeight(inputId, requestsAllowed).getWeight();
                    BigDecimal bd = new BigDecimal(weight * dailyConsumption.getReceivedValues()[inputId][index]);
                    intervalValues.add(new IntervalValue(bd, 0, 0));
                }
            }
            //Don't add the record if it doesn't belong in the requested interval
            if ((daily && !requestsAllowed) || (calendar.getTime().before(toDate) && calendar.getTime().after(lastReading))) {
                intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
            }

            if (!monthly) {
                calendar.add(Calendar.SECOND, -1 * getProfileIntervalInSeconds() * (daily ? getNumberOfInputsUsed() : 1));   //Go back 4 intervals in case of daily consumption & 4 ports
            } else {
                calendar.add(Calendar.MONTH, -1);
            }
        }
        profileData.setIntervalDatas(intervalDatas);

        // build meter events
        if (includeEvents && (requestsAllowed || waveFlowV2.usesInitialRFCommand())) {
            profileData.setMeterEvents(buildMeterEvents(lastReading, toDate));
        }

        return profileData;
    }

    private Calendar roundTimeStamps(Calendar calendar, int profileIntervalInSeconds) throws IOException {
        if (waveFlowV2.isRoundDownToNearestInterval()) {
            if (!ParseUtils.isOnIntervalBoundary(calendar, profileIntervalInSeconds)) {
                ParseUtils.roundDown2nearestInterval(calendar, profileIntervalInSeconds);
            }
        }
        return calendar;
    }

    /**
     * If multiFrame mode is enabled, the number of values that can be requested in one time is not limited.
     * If it's disabled (due to the use of repeaters), data has to be requested in steps.
     */
    private int getSteps(int nrOfIntervals) {
        return waveFlowV2.isMultiFrame() ? nrOfIntervals : STEPS;
    }

    /**
     * Returns the number of DAILY values stored in the module.
     * If there's only 1 input channel used, this number is 24.
     * For 2 input channels, there's only 12 values each, etc.
     */
    private int getNumberOfDailyValues() throws IOException {
        return 24 / getNumberOfInputsUsed();
    }

    private int getNumberOfInputsUsed() throws IOException {
        if (inputsUsed == 0) {
            inputsUsed = waveFlowV2.getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        }
        return inputsUsed;
    }

    public void setNumberOfInputsUsed(int value) {
        inputsUsed = value;
    }

    private int getProfileIntervalInSeconds() throws IOException {
        if (interval == 0) {
            interval = waveFlowV2.getProfileInterval();
        }
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private Date getTimeStampOfNewestRecord(Date lastLoggedValue, long offset) throws IOException {
        long timeStamp = lastLoggedValue.getTime();
        timeStamp -= 1000 * (offset * getProfileIntervalInSeconds());
        return new Date(timeStamp);
    }

    /**
     * Getter for the number of profile data entries that should be read.
     */
    private int getNrOfIntervals(Date lastReading, Date toDate) throws IOException {

        //The monthly logging doesn't have a fixed time interval.
        if (waveFlowV2.getParameterFactory().readOperatingMode().isMonthlyMeasurement()) {
            Calendar checkDate = new GregorianCalendar(waveFlowV2.getTimeZone());
            checkDate.setTime(toDate);
            checkDate.setLenient(true);
            int numberOfIntervals = 0;
            while (checkDate.getTime().after(lastReading)) {
                checkDate.add(Calendar.MONTH, -1);
                numberOfIntervals++;
            }
            return numberOfIntervals;
        }

        //In case of periodic or weekly logging, calculate the number based on the interval in seconds.
        return (int) (((toDate.getTime() - lastReading.getTime()) / 1000) / getProfileIntervalInSeconds()) + 1;
    }

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        EventStatusAndDescription translator = new EventStatusAndDescription(waveFlowV2);

        boolean useExtendedIndexReading = waveFlowV2.usesInitialRFCommand();
        if (!useExtendedIndexReading) {
            //Check the profile type. This defines the module's extra functionality, eg. backflow and reed fault detection, and thus the events to read out!
            ProfileType profileType = waveFlowV2.getParameterFactory().readProfileType();

            //Simple backflow detection for the past 12 months, for input channel A (0) and B (1)
            if (profileType.supportsSimpleBackflowDetection()) {
                for (int input = 0; input <= 1; input++) {
                    BackflowDetectionFlags backflowDetectionFlags = waveFlowV2.getParameterFactory().readSimpleBackflowDetectionFlags(input);  //0 = channel A, 1 = channel B
                    for (int i = 0; i <= 12; i++) {
                        if (backflowDetectionFlags.flagIsSet(i)) {
                            Date eventDate = backflowDetectionFlags.getEventDate(i);
                            meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, translator.getProtocolCodeForSimpleBackflow(input), "Backflow detected on input " + backflowDetectionFlags.getInputChannelName()));
                        }
                    }
                }
            }

            //Advanced backflow detection
            if (profileType.supportsAdvancedBackflowDetection()) {
                //Detection by measuring water volume
                if (waveFlowV2.getParameterFactory().readExtendedOperationMode().usingVolumeMethodForBackFlowDetection()) {
                    for (BackFlowEventByVolumeMeasuring backFlowEvent : waveFlowV2.getRadioCommandFactory().readBackFlowEventTableByVolumeMeasuring().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV2.getPulseWeight(inputIndex);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartOfDetectionDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex, true), "Backflow start, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndOfDetectionDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex, false), "Backflow end, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                    }
                }

                //Detection by flow rate
                else {
                    for (BackFlowEventByFlowRate backFlowEvent : waveFlowV2.getRadioCommandFactory().readBackFlowEventTableByFlowRate().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV2.getPulseWeight(inputIndex);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex, true), "Backflow start, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex, false), "Backflow end, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                    }
                }
            }

            for (LeakageEvent leakageEvent : waveFlowV2.getRadioCommandFactory().readLeakageEventTable().getLeakageEvents()) {
                if (leakageEvent.isValid()) {
                    String startOrEnd = leakageEvent.getStatusDescription();
                    String leakageType = leakageEvent.getLeakageType();
                    String inputChannel = leakageEvent.getCorrespondingInputChannel();
                    if (leakageEvent.getLeakageType().equals(LeakageEvent.LEAKAGETYPE_EXTREME)) {
                        meterEvents.add(new MeterEvent(leakageEvent.getDate(), MeterEvent.OTHER, translator.getProtocolCodeForLeakage(startOrEnd, leakageType, inputChannel), startOrEnd + " of " + leakageEvent.getEventDescription() + " on input " + inputChannel + ": flow-rate = " + leakageEvent.getConsumptionRate()));
                    }
                    if (leakageEvent.getLeakageType().equals(LeakageEvent.LEAKAGETYPE_RESIDUAL)) {
                        meterEvents.add(new MeterEvent(leakageEvent.getDate(), MeterEvent.OTHER, translator.getProtocolCodeForLeakage(startOrEnd, leakageType, inputChannel), startOrEnd + " of " + leakageEvent.getEventDescription() + " on input " + inputChannel + ": flow-rate = " + leakageEvent.getConsumptionRate()));
                    }
                }
            }
        }

        int applicationStatus = waveFlowV2.getParameterFactory().readApplicationStatus();
        if (!useExtendedIndexReading) {
            int valveApplicationStatus = 0;
            if (waveFlowV2.getParameterFactory().readProfileType().supportsWaterValveControl()) {
                valveApplicationStatus = waveFlowV2.getParameterFactory().readValveApplicationStatus();
            }
            if ((valveApplicationStatus & 0x01) == 0x01) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tamper (valve wirecut)"));
            }
            if ((valveApplicationStatus & 0x02) == 0x02) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.HARDWARE_ERROR, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Valve fault"));
            }
            if ((valveApplicationStatus & 0x04) == 0x04) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EventStatusAndDescription.EVENTCODE_DEFAULT, "Credit under threshold"));
            }
            if ((valveApplicationStatus & 0x08) == 0x08) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.EVENTCODE_DEFAULT, "Credit equal to zero"));
            }
        }

        if ((applicationStatus & 0x01) == 0x01) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV2.getParameterFactory().readBatteryLifeDateEnd();
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery warning"));
        }
        if ((applicationStatus & 0x02) == 0x02) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV2.getParameterFactory().readWireCutDetectionDate(0);
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x02), translator.getProtocolCodeForStatus(0x02), translator.getEventDescription(0x02)));
        }
        if ((applicationStatus & 0x04) == 0x04) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV2.getParameterFactory().readWireCutDetectionDate(1);
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_B, "Tamper (wirecut B)"));
        }

        if (useExtendedIndexReading) {
            if ((applicationStatus & 0x08) == 0x08) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, translator.getProtocolCodeForLeakage(LeakageEvent.START, LeakageEvent.LEAKAGETYPE_RESIDUAL, LeakageEvent.A), "Leak"));
            }
            if ((applicationStatus & 0x10) == 0x10) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, translator.getProtocolCodeForLeakage(LeakageEvent.START, LeakageEvent.LEAKAGETYPE_EXTREME, LeakageEvent.A), "Burst"));
            }
        }

        if ((applicationStatus & 0x20) == 0x20) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV2.getParameterFactory().readWireCutDetectionDate(2);
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x20), translator.getProtocolCodeForStatus(0x20), translator.getEventDescription(0x20)));
        }

        if ((applicationStatus & 0x40) == 0x40) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV2.getParameterFactory().readWireCutDetectionDate(3);
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_D, "Tamper (wirecut D)"));
        }

        if (useExtendedIndexReading) {
            if ((applicationStatus & 0x80) == 0x80) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, translator.getProtocolCodeForSimpleBackflow(0), "Backflow detected"));
            }
        }

        meterEvents = shiftDates(meterEvents);
        return checkValid(meterEvents, lastReading, toDate);
    }

    /**
     * Deletes events that don't fit in the interval [lastReading - toDate]
     */
    private List<MeterEvent> checkValid(List<MeterEvent> meterEvents, Date lastReading, Date toDate) {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getTime().after(lastReading) && (meterEvent.getTime().before(toDate) || waveFlowV2.usesInitialRFCommand())) {
                result.add(meterEvent);
            }
        }
        return result;
    }

    /**
     * Check the timestamps for the meter events, add a second to make the stamps different (if necessary)
     *
     * @param meterEvents the events
     * @return the fixed events
     */
    private List<MeterEvent> shiftDates(List<MeterEvent> meterEvents) {
        if (meterEvents.size() < 2) {
            return meterEvents;
        }
        List<MeterEvent> newMeterEvents = new ArrayList<MeterEvent>();
        newMeterEvents.add(meterEvents.get(0)); //add the first event
        MeterEvent newMeterEvent;

        for (int i = 1; i < meterEvents.size(); i++) {
            MeterEvent previousMeterEvent = meterEvents.get(i - 1);
            MeterEvent currentMeterEvent = meterEvents.get(i);
            if (currentMeterEvent.getTime().equals(previousMeterEvent.getTime())) {
                Date newDate = new Date(newMeterEvents.get(i - 1).getTime().getTime() + 1000); //add one second to make the timestamps different
                newMeterEvent = new MeterEvent(newDate, currentMeterEvent.getEiCode(), currentMeterEvent.getProtocolCode(), currentMeterEvent.getMessage());
            } else {
                newMeterEvent = currentMeterEvent;
            }
            newMeterEvents.add(i, newMeterEvent);
        }
        return newMeterEvents;
    }
}