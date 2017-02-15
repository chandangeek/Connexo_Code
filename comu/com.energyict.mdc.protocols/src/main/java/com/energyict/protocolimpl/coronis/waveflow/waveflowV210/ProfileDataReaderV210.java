/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.waveflowV210;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.ApplicationStatusParser;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.BackflowDetectionFlags;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.BackFlowEventByFlowRate;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.BackFlowEventByVolumeMeasuring;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.DataloggingTable;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.ExtendedIndexReading;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.LeakageEvent;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.ReadDataFeature;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210.ReadOverspeedAlarmInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProfileDataReaderV210 {

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;

    private int profileInterval = 0;
    private int numberOfInputs = 0;
    private WaveFlow waveFlowV1;

    public ProfileDataReaderV210(WaveFlow waveFlowV1) {
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

        boolean monthly = getProfileIntervalInSeconds() >= MONTHLY;
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
            setProfileInterval(extendedIndexReading.getDataloggingMeasurementPeriod().getSamplingPeriodInSeconds());
        } else {
            DataloggingTable dataloggingTable = null;
            if (getNumberOfInputsUsed() == 1) {
                dataloggingTable = waveFlowV1.getRadioCommandFactory().readDataloggingTable(1);
                rawValues.add(dataloggingTable.getProfileDataA());
                lastLoggedValueDate = dataloggingTable.getLastLoggedIndexDate();
            }
            if (getNumberOfInputsUsed() == 2) {
                dataloggingTable = waveFlowV1.getRadioCommandFactory().readDataloggingTable(12);
                rawValues.add(dataloggingTable.getProfileDataA());
                rawValues.add(dataloggingTable.getProfileDataB());
                lastLoggedValueDate = dataloggingTable.getLastLoggedIndexDate();
            }
            if (getNumberOfInputsUsed() == 3) {
                dataloggingTable = waveFlowV1.getRadioCommandFactory().readDataloggingTable(12);
                rawValues.add(dataloggingTable.getProfileDataA());
                rawValues.add(dataloggingTable.getProfileDataB());
                DataloggingTable dataloggingTableC = waveFlowV1.getRadioCommandFactory().readDataloggingTable(3);
                rawValues.add(dataloggingTableC.getProfileDataC());
                lastLoggedValueDate = dataloggingTableC.getLastLoggedIndexDate();
            }
            if (getNumberOfInputsUsed() == 4) {
                dataloggingTable = waveFlowV1.getRadioCommandFactory().readDataloggingTable(12);
                rawValues.add(dataloggingTable.getProfileDataA());
                rawValues.add(dataloggingTable.getProfileDataB());
                DataloggingTable dataloggingTableCD = waveFlowV1.getRadioCommandFactory().readDataloggingTable(34);
                rawValues.add(dataloggingTableCD.getProfileDataC());
                rawValues.add(dataloggingTableCD.getProfileDataD());
                lastLoggedValueDate = dataloggingTableCD.getLastLoggedIndexDate();
            }
            if (dataloggingTable != null) {
                setProfileInterval(dataloggingTable.getDataloggingMeasurementPeriod().getSamplingPeriodInSeconds());
            }
        }

        return parseProfileData(false, true, channelIndexes, monthly, lastReading, toDate, includeEvents, rawValues, lastLoggedValueDate);
    }

    /*
    This method returns the ProfileData based on the given raw values and the time interval.
    It can be used for pushed frames and requested frames.
     */

    public ProfileData parseProfileData(boolean bubbleUpOrigin, boolean requestsAllowed, int[] channelIndexes, boolean monthly, Date lastReading, Date toDate, boolean includeEvents, List<Long[]> rawValues, Date lastLoggedValueDate) throws IOException {

        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        Calendar calendar = Calendar.getInstance(waveFlowV1.getTimeZone());
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
        calendar = roundTimeStamps(monthly, calendar, getProfileIntervalInSeconds());
        int flags = checkBadTime(calendar);

        int nrOfReadings = 4;     //When using the extended index reading, only 4 LP entries are available.
        if (!waveFlowV1.usesInitialRFCommand()) {
            nrOfReadings = (getNumberOfInputsUsed() == 1 ? 24 : 12);
        }

        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (int index = 0; index < nrOfReadings; index++) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();

            for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
                int weight = waveFlowV1.getPulseWeight(inputId, requestsAllowed).getWeight();
                Long value = rawValues.get(inputId)[index];
                if (value != -1) {
                    BigDecimal bd = new BigDecimal(weight * value);
                    intervalValues.add(new IntervalValue(bd, 0, flags));    //The module doesn't send any information about the value's status..
                }
            }

            //Only add the received value if it fits in the requested time interval [lastReading - toDate]
            if ((calendar.getTime().before(toDate) && calendar.getTime().after(lastReading))) {
                if (!intervalValues.isEmpty()) {
                    intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
                }
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
            List<MeterEvent> meterEvents = buildMeterEvents(lastReading, toDate, bubbleUpOrigin);
            if (flags != IntervalStateBits.OK) {
                meterEvents.add(new MeterEvent(
                        new Date(),
                        MeterEvent.CLOCK_INVALID,
                        EventStatusAndDescription.EVENTCODE_BADTIME_DATALOGGING,
                        "Received LP entries but their timestamps deviate more than " + getProfileIntervalInSeconds() + " seconds from the expected to date"));
        }
            profileData.setMeterEvents(meterEvents);
        }

        return profileData;
    }

    /**
     * If the timestamp of the newest LP interval deviates more than X minutes (X = profile interval) from the current time, indicate the LP entries as 'bad time'.
     * Also add an event in this case.
     */
    private int checkBadTime(Calendar calendar) throws IOException {
        Calendar now = Calendar.getInstance(calendar.getTimeZone());
        if (Math.abs(calendar.getTimeInMillis() - now.getTimeInMillis()) > (getProfileIntervalInSeconds() * 1000)) {
            return IntervalStateBits.BADTIME;
        }
        return IntervalStateBits.OK;
    }

    private Calendar roundTimeStamps(boolean monthly, Calendar calendar, int profileIntervalInSeconds) throws IOException {
        if (waveFlowV1.isRoundDownToNearestInterval()) {
            if (monthly || profileIntervalInSeconds == WEEKLY || profileIntervalInSeconds == DAILY) {
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
            } else {
                if (!ParseUtils.isOnIntervalBoundary(calendar, profileIntervalInSeconds)) {
                    ParseUtils.roundDown2nearestInterval(calendar, profileIntervalInSeconds);
                }
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

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate, boolean bubbleUpOrigin) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        EventStatusAndDescription translator = new EventStatusAndDescription(waveFlowV1.getDeviceType());

        boolean usesInitialRFCommand = waveFlowV1.usesInitialRFCommand();
        if (!usesInitialRFCommand) {
            //Check the profile type. This defines the module's extra functionality, eg. backflow and reed fault detection, and thus the events to read out!
            ProfileType profileType = waveFlowV1.getParameterFactory().readProfileType();

            //Simple backflow detection for the past 12 months, for input channel A (0) and B (1)
            if (profileType.supportsSimpleBackflowDetection()) {
                for (int input = 0; input <= 1; input++) {
                    BackflowDetectionFlags backflowDetectionFlags = waveFlowV1.getParameterFactory().readSimpleBackflowDetectionFlags(input);  //0 = channel A, 1 = channel B
                    for (int i = 0; i <= 12; i++) {
                        if (backflowDetectionFlags.flagIsSet(i)) {
                            Date eventDate = backflowDetectionFlags.getEventDate(i);
                            meterEvents.add(new MeterEvent(eventDate, MeterEvent.METER_ALARM, translator.getProtocolCodeForSimpleBackflow(input), "Backflow detected on input " + backflowDetectionFlags.getInputChannelName()));
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
                        PulseWeight pulseWeight = waveFlowV1.getPulseWeight(inputIndex, true);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartOfDetectionDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex + 1, true), "Backflow start, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndOfDetectionDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowVolumeMeasuring(inputIndex + 1, false), "Backflow end, input channel = " + (inputIndex + 1) + ", volume = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString()));
                    }
                }

                //Detection by flow rate
                else {
                    for (BackFlowEventByFlowRate backFlowEvent : waveFlowV1.getRadioCommandFactory().readBackFlowEventTableByFlowRate().getEvents()) {
                        int inputIndex = backFlowEvent.getInputIndex();
                        PulseWeight pulseWeight = waveFlowV1.getPulseWeight(inputIndex, true);
                        meterEvents.add(new MeterEvent(backFlowEvent.getStartDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex + 1, true), "Backflow start, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                        meterEvents.add(new MeterEvent(backFlowEvent.getEndDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForAdvancedBackflowFlowRate(inputIndex + 1, false), "Backflow end, input channel = " + (inputIndex + 1) + ", maximum flow rate = " + backFlowEvent.getVolume() * pulseWeight.getWeight() + " " + pulseWeight.getUnit().toString() + ", detection duration = " + backFlowEvent.getDetectionDuration() + " minutes, water backflow duration = " + backFlowEvent.getBackflowDuration() + " minutes."));
                    }
                }
            }

            for (LeakageEvent leakageEvent : waveFlowV1.getRadioCommandFactory().readLeakageEventTable().getLeakageEvents()) {
                if (leakageEvent.isValid()) {
                    String startOrEnd = leakageEvent.getStatusDescription();
                    String leakageType = leakageEvent.getLeakageType();
                    String inputChannel = leakageEvent.getCorrespondingInputChannel();
                    if (leakageEvent.getLeakageType().equals(LeakageEvent.LEAKAGETYPE_EXTREME)) {
                        meterEvents.add(new MeterEvent(leakageEvent.getDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(startOrEnd, leakageType, inputChannel), startOrEnd + " of " + leakageEvent.getEventDescription() + " on input " + inputChannel + ": flow-rate = " + leakageEvent.getConsumptionRate()));
                    }
                    if (leakageEvent.getLeakageType().equals(LeakageEvent.LEAKAGETYPE_RESIDUAL)) {
                        meterEvents.add(new MeterEvent(leakageEvent.getDate(), MeterEvent.METER_ALARM, translator.getProtocolCodeForLeakage(startOrEnd, leakageType, inputChannel), startOrEnd + " of " + leakageEvent.getEventDescription() + " on input " + inputChannel + ": flow-rate = " + leakageEvent.getConsumptionRate()));
                    }
                }
            }

            //In case of the ER200H device, read out the over speed events
            if (waveFlowV1.isV210()) {
                ReadOverspeedAlarmInfo alarmInfo = waveFlowV1.getRadioCommandFactory().readOverSpeedAlarmInfo();
                meterEvents.addAll(alarmInfo.getMeterEvents());
            }

            ReadDataFeature alarmDisplayStatus = waveFlowV1.getRadioCommandFactory().readFeatureData();
            if (alarmDisplayStatus.isNoFlow()) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_NO_FLOW, "No flow"));
            }
            if (alarmDisplayStatus.isBurst()) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, EventStatusAndDescription.EVENTCODE_BURST, "Burst"));
            }
        }

        int applicationStatus = waveFlowV1.getParameterFactory().readApplicationStatus();
        ApplicationStatusParser parser = new ApplicationStatusParser(waveFlowV1, bubbleUpOrigin);
        meterEvents.addAll(parser.getMeterEvents(usesInitialRFCommand, applicationStatus, false));

        return meterEvents;
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