package com.energyict.protocolimpl.coronis.waveflow.waveflowV1;

import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.*;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ProfileDataReaderV1 {

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;

    private int profileInterval = 0;
    private int numberOfInputs = 0;
    private WaveFlow waveFlowV1;

    public ProfileDataReaderV1(WaveFlow waveFlowV1) {
        this.waveFlowV1 = waveFlowV1;
    }

    public void setNumberOfInputs(int numberOfInputs) {
        this.numberOfInputs = numberOfInputs;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    final ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        if (toDate == null || toDate.after(new Date())) {
            toDate = new Date();
        }

        boolean monthly = false;
        if (getProfileIntervalInSeconds() >= MONTHLY) {
            monthly = true;
        }

        Date lastLoggedValueDate = new Date();
        int[] channelIndexes = new int[0];

        if (getNumberOfInputsUsed() == 1) {
            channelIndexes = new int[]{1};
        }
        if (getNumberOfInputsUsed() == 2) {
            channelIndexes = new int[]{1, 2};
        }
        if (getNumberOfInputsUsed() == 3) {
            channelIndexes = new int[]{1, 2, 3};
        }
        if (getNumberOfInputsUsed() == 4) {
            channelIndexes = new int[]{1, 2, 3, 4};
        }

        List<Long[]> rawValues = new ArrayList<Long[]>();
        if (waveFlowV1.usesInitialRFCommand()) {               //The LP entries were read out via the extended index reading.
            ExtendedIndexReading extendedIndexReading = waveFlowV1.getRadioCommandFactory().readExtendedIndexConfiguration();
            rawValues = extendedIndexReading.getLast4LoggedIndexes();
            lastLoggedValueDate = extendedIndexReading.getDateOfLastLoggedValue();
        } else {
            if (getNumberOfInputsUsed() == 1) {
                DataloggingTable dataloggingTableA = waveFlowV1.getRadioCommandFactory().readDataloggingTable(1);
                rawValues.add(dataloggingTableA.getProfileDataA());
                lastLoggedValueDate = dataloggingTableA.getLastLoggedIndexDate();
            }
            if (getNumberOfInputsUsed() == 2) {
                DataloggingTable dataloggingTableAB = waveFlowV1.getRadioCommandFactory().readDataloggingTable(12);
                rawValues.add(dataloggingTableAB.getProfileDataA());
                rawValues.add(dataloggingTableAB.getProfileDataB());
                lastLoggedValueDate = dataloggingTableAB.getLastLoggedIndexDate();
            }
            if (getNumberOfInputsUsed() == 3) {
                DataloggingTable dataloggingTableAB = waveFlowV1.getRadioCommandFactory().readDataloggingTable(12);
                rawValues.add(dataloggingTableAB.getProfileDataA());
                rawValues.add(dataloggingTableAB.getProfileDataB());
                DataloggingTable dataloggingTableC = waveFlowV1.getRadioCommandFactory().readDataloggingTable(3);
                rawValues.add(dataloggingTableC.getProfileDataC());
                lastLoggedValueDate = dataloggingTableC.getLastLoggedIndexDate();
            }
            if (getNumberOfInputsUsed() == 4) {
                DataloggingTable dataloggingTableAB = waveFlowV1.getRadioCommandFactory().readDataloggingTable(12);
                rawValues.add(dataloggingTableAB.getProfileDataA());
                rawValues.add(dataloggingTableAB.getProfileDataB());
                DataloggingTable dataloggingTableCD = waveFlowV1.getRadioCommandFactory().readDataloggingTable(34);
                rawValues.add(dataloggingTableCD.getProfileDataC());
                rawValues.add(dataloggingTableCD.getProfileDataD());
                lastLoggedValueDate = dataloggingTableCD.getLastLoggedIndexDate();
            }
        }

        int nrOfReadings = 4;     //When using the extended index reading, only 4 LP entries are available.
        if (!waveFlowV1.usesInitialRFCommand()) {
            nrOfReadings = (getNumberOfInputsUsed() == 1 ? 24 : 12);
        }

        return parseProfileData(true, getNumberOfInputsUsed(), channelIndexes, nrOfReadings, monthly, lastReading, toDate, includeEvents, rawValues, lastLoggedValueDate);
    }

    /*
    This method returns the ProfileData based on the given raw values and the time interval.
    It can be used for pushed frames and requested frames.
     */

    public ProfileData parseProfileData(boolean requestsAllowed, int numberOfInputsUsed, int[] channelIndexes, int nrOfReadings, boolean monthly, Date lastReading, Date toDate, boolean includeEvents, List<Long[]> rawValues, Date lastLoggedValueDate) throws IOException {

        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        TimeZone timeZone;
        if (requestsAllowed) {
            timeZone = waveFlowV1.getTimeZone();
        } else {
            timeZone = TimeZone.getDefault();
        }

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setLenient(true);

        for (int channelIndex : channelIndexes) {
            Unit unit = waveFlowV1.getPulseWeight(channelIndex - 1, requestsAllowed).getUnit();
            ChannelInfo channelInfo = new ChannelInfo(channelIndex - 1, String.valueOf(channelIndex), unit);
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE));
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);

        // initialize calendar
        calendar.setTime(lastLoggedValueDate);
        if (monthly || (getProfileIntervalInSeconds() == WEEKLY)) {
            calendar = roundTimeStamps(calendar, HOURLY);
        } else {
            calendar = roundTimeStamps(calendar, getProfileIntervalInSeconds());
        }

        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (int index = 0; index < nrOfReadings; index++) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();

            for (int inputId = 0; inputId < numberOfInputsUsed; inputId++) {
                int weight = waveFlowV1.getPulseWeight(inputId, requestsAllowed).getWeight();
                BigDecimal bd = new BigDecimal(weight * rawValues.get(inputId)[index]);
                intervalValues.add(new IntervalValue(bd, 0, 0));    //The module doesn't send any information about the value's status..
            }

            //Only add the received value if it fits in the requested time interval [lastReading - toDate]
            if ((calendar.getTime().before(toDate) && calendar.getTime().after(lastReading))) {
                intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
            }

            if (!monthly) {
                calendar.add(Calendar.SECOND, -1 * getProfileIntervalInSeconds());
            } else {
                calendar.add(Calendar.MONTH, -1);
            }
        }
        profileData.setIntervalDatas(intervalDatas);

        // build meter events
        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents(lastReading, toDate));
        }

        return profileData;
    }

    private Calendar roundTimeStamps(Calendar calendar, int profileIntervalInSeconds) throws IOException {
        if (waveFlowV1.isRoundDownToNearestInterval()) {
            if (!ParseUtils.isOnIntervalBoundary(calendar, profileIntervalInSeconds)) {
                ParseUtils.roundDown2nearestInterval(calendar, profileIntervalInSeconds);
            }
        }
        return calendar;
    }

    private int getNumberOfInputsUsed() throws IOException {
        if (numberOfInputs == 0) {
            numberOfInputs = waveFlowV1.getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        }
        return numberOfInputs;
    }

    private int getProfileIntervalInSeconds() throws IOException {
        if (profileInterval == 0) {
            profileInterval = waveFlowV1.getProfileInterval();
        }
        return profileInterval;
    }

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        EventStatusAndDescription translator = new EventStatusAndDescription(waveFlowV1);

        boolean useExtendedIndexReading = waveFlowV1.usesInitialRFCommand();
        if (!useExtendedIndexReading) {
            //Check the profile type. This defines the module's extra functionality, eg. backflow and reed fault detection, and thus the events to read out!
            ProfileType profileType = waveFlowV1.getParameterFactory().readProfileType();

            //Simple backflow detection for the past 12 months, for input channel A (0) and B (1)
            if (profileType.supportsSimpleBackflowDetection()) {
                for (int input = 0; input <= 1; input++) {
                    BackflowDetectionFlags backflowDetectionFlags = waveFlowV1.getParameterFactory().readSimpleBackflowDetectionFlags(input);  //0 = channel A, 1 = channel B
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
                if (waveFlowV1.getParameterFactory().readExtendedOperationMode().usingVolumeMethodForBackFlowDetection()) {
                    for (BackFlowEventByVolumeMeasuring backFlowEvent : waveFlowV1.getRadioCommandFactory().readBackFlowEventTableByVolumeMeasuring().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV1.getPulseWeight(inputIndex);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartOfDetectionDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex, true), "Backflow start, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndOfDetectionDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex, false), "Backflow end, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                    }
                }

                //Detection by flow rate
                else {
                    for (BackFlowEventByFlowRate backFlowEvent : waveFlowV1.getRadioCommandFactory().readBackFlowEventTableByFlowRate().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV1.getPulseWeight(inputIndex);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex, true), "Backflow start, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndDate(), MeterEvent.OTHER, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex, false), "Backflow end, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                    }
                }
            }

            for (LeakageEvent leakageEvent : waveFlowV1.getRadioCommandFactory().readLeakageEventTable().getLeakageEvents()) {
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

        int applicationStatus = waveFlowV1.getParameterFactory().readApplicationStatus();
        if ((applicationStatus & 0x01) == 0x01) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV1.getParameterFactory().readBatteryLifeDateEnd();
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery warning"));
        }
        if ((applicationStatus & 0x02) == 0x02) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV1.getParameterFactory().readWireCutDetectionDate(0);
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x02), translator.getProtocolCodeForStatus(0x02), translator.getEventDescription(0x02)));
        }
        if ((applicationStatus & 0x04) == 0x04) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV1.getParameterFactory().readWireCutDetectionDate(1);
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
                eventDate = waveFlowV1.getParameterFactory().readWireCutDetectionDate(2);
            }
            meterEvents.add(new MeterEvent(eventDate, translator.getEventCode(0x20), translator.getProtocolCodeForStatus(0x20), translator.getEventDescription(0x20)));
        }

        if ((applicationStatus & 0x40) == 0x40) {
            Date eventDate = new Date();
            if (!useExtendedIndexReading) {
                eventDate = waveFlowV1.getParameterFactory().readWireCutDetectionDate(3);
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_D, "Tamper (wirecut D"));
        }

        if (useExtendedIndexReading) {
            if ((applicationStatus & 0x80) == 0x80) {
                Date eventDate = new Date();
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, translator.getProtocolCodeForSimpleBackflow(0), "Backflow detected"));
            }
        }

        return checkValid(meterEvents, lastReading, toDate);
    }

    private List<MeterEvent> checkValid(List<MeterEvent> meterEvents, Date lastReading, Date toDate) {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getTime().after(lastReading) && (meterEvent.getTime().before(toDate) || waveFlowV1.usesInitialRFCommand())) {
                result.add(meterEvent);
            }
        }
        return result;
    }

    /**
     * Check the timestamps for the meter events, add a second to make the stamps different (if necessary)
     * Method is no longer used, it happens in the RTU+Server
     *
     * @param meterEvents the events
     * @return the fixed events
     */
    private List<MeterEvent> checkDates(List<MeterEvent> meterEvents) {
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