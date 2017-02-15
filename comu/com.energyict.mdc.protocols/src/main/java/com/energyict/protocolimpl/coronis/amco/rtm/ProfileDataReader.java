/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.amco.rtm.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ApplicationStatus;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.GenericHeader;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ProfileType;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.ExtendedDataloggingTable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ProfileDataReader {

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;
    private static final int STEPS = 24;                   //Max number of profile data entries in 1 frame!

    private RTM rtm;
    private ProfileType profileType;
    private int numberOfInputs = 0;
    private int profileInterval = 0;

    public ProfileDataReader(RTM rtm) {
        this.rtm = rtm;
    }

    public void setNumberOfInputs(int numberOfInputs) {
        this.numberOfInputs = numberOfInputs;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    private int getProfileIntervalInSeconds() throws IOException {
        if (profileInterval == 0) {
            profileInterval = rtm.getProfileInterval();
        }
        return profileInterval;
    }

    private int getNrOfIntervals(Date lastReading, Date toDate) throws IOException {

        //The monthly logging doesn't have a fixed time interval.
        if (rtm.getParameterFactory().readOperatingMode().isMonthlyLogging()) {
            Calendar checkDate = new GregorianCalendar(rtm.getTimeZone());
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

    private int getNumberOfInputsUsed() throws IOException {
        if (numberOfInputs == 0) {
            numberOfInputs = rtm.getParameterFactory().readOperatingMode().readNumberOfPorts();
        }
        return numberOfInputs;
    }

    private Date getTimeStampOfNewestRecord(Date lastLoggedValue, long offset) throws IOException {
        long timeStamp = lastLoggedValue.getTime();
        timeStamp -= 1000 * (offset * getProfileIntervalInSeconds());
        return new Date(timeStamp);
    }

    private Date getTimeStampOfNewestRecordMonthly(Date toDate, Date lastLoggedValue) {
        Calendar lastLogged = Calendar.getInstance(rtm.getTimeZone());
        lastLogged.setTime(lastLoggedValue);
        lastLogged.setLenient(true);

        //Go back month by month until you have the date closest to the toDate.
        while (lastLogged.getTime().after(toDate)) {
            lastLogged.add(Calendar.MONTH, -1);
        }
        return lastLogged.getTime();
    }

    final ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        profileType = rtm.getParameterFactory().readProfileType();
        ProfileData profileData = new ProfileData();

        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents(lastReading, toDate));
        }
        if (profileType.isEvoHop()) {
            return profileData;           //The evoHop module doesn't support data logging
        }

        if (toDate == null || toDate.after(new Date())) {
            toDate = new Date();
        }

        boolean monthly = (getProfileIntervalInSeconds() >= MONTHLY);
        int nrOfIntervals = getNrOfIntervals(lastReading, toDate);
        int initialNrOfIntervals = nrOfIntervals;
        Date lastLoggedValue = new Date();
        List<List<Integer[]>> rawValues = new ArrayList<>();

        int startOffset = -1;
        long initialOffset = -1;
        GenericHeader genericHeader = null;

        if (rtm.usesInitialRFCommand()) {
            ExtendedDataloggingTable table = rtm.getRadioCommandFactory().getCachedExtendedDataloggingTable();
            genericHeader = table.getGenericHeader();
            profileInterval = table.getProfileInterval();
            rawValues = table.getProfileDataForAllPorts();
            setProfileInterval(table.getProfileInterval());
            return parseProfileData(genericHeader, false, rawValues, new ProfileData(), monthly, false, new Date(), new Date(0), table.getLastLoggedTimeStamp(), 0);
        }

        //Get the profile data for all selected input channels, in case of periodic/weekly/monthly measuring.
        for (int port = 0; port < getNumberOfInputsUsed(); port++) {
            nrOfIntervals = initialNrOfIntervals;
            int counter = 0;
            List<Integer[]> values = new ArrayList<>();
            while (nrOfIntervals > 0) {
                if (startOffset == -1) {
                    ExtendedDataloggingTable table = rtm.getRadioCommandFactory().readExtendedDataloggingTable(port + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), toDate);
                    genericHeader = table.getGenericHeader();
                    for (Integer integer : table.getProfileData()) {
                        values.add(new Integer[]{integer, genericHeader.getIntervalStatus(port), genericHeader.getApplicationStatus().getStatus()});        //Add interval value and status bit!
                    }
                    startOffset = table.getOffset();
                    initialOffset = startOffset;
                    lastLoggedValue = table.getLastLoggedTimeStamp();
                    if (table.getProfileData().size() < getSteps(nrOfIntervals)) {
                        break;   //To avoid invalid offsets in the next iteration
                    }
                } else {
                    int offset = (startOffset + getSteps(nrOfIntervals) * counter);
                    ExtendedDataloggingTable table = rtm.getRadioCommandFactory().readExtendedDataloggingTable(port + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), offset);
                    genericHeader = table.getGenericHeader();
                    for (Integer integer : table.getProfileData()) {
                        values.add(new Integer[]{integer, genericHeader.getIntervalStatus(port), genericHeader.getApplicationStatus().getStatus()});        //Add interval value and status bit!
                    }
                    if (table.getProfileData().size() < getSteps(nrOfIntervals)) {
                        break;   //To avoid invalid offsets in the next iteration
                    }
                }
                counter++;
                nrOfIntervals -= getSteps(nrOfIntervals);
            }
            if (values.get(0)[0] == Integer.MAX_VALUE) {
                rtm.getLogger().info("Port " + port + " has no meter connected, no profile data available for this port");
            } else {
                rawValues.add(values);
            }
        }
        if (rawValues.isEmpty()) {
            return profileData;
        }

        return parseProfileData(genericHeader, true, rawValues, profileData, monthly, false, toDate, lastReading, lastLoggedValue, initialOffset);
    }

    public ProfileData parseProfileData(GenericHeader genericHeader, boolean requestsAllowed, List<List<Integer[]>> rawValues, ProfileData profileData, boolean monthly, boolean daily, Date toDate, Date lastReading, Date lastLoggedValue, long initialOffset) throws IOException {

        List<ChannelInfo> channelInfos = new ArrayList<>();
        Calendar calendar;
        if (requestsAllowed) {
            calendar = Calendar.getInstance(rtm.getTimeZone());
        } else {
            calendar = Calendar.getInstance();
        }

        calendar.setLenient(true);

        int channelId = 0;
        for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
            Unit unit = genericHeader.getUnit(inputId);
            ChannelInfo channelInfo = new ChannelInfo(channelId++, String.valueOf(inputId + 1), unit);
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE)); //4 bytes long, signed value
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);

        // initialize calendar
        if (rtm.usesInitialRFCommand()) {
            calendar.setTime(lastLoggedValue);
        } else {
            if (!monthly) {
                calendar.setTime(getTimeStampOfNewestRecord(lastLoggedValue, initialOffset));
            } else {
                calendar.setTime(getTimeStampOfNewestRecordMonthly(toDate, lastLoggedValue));
            }
        }
        calendar = roundTimeStamps(monthly, calendar, getProfileIntervalInSeconds());


        if (daily) {
            calendar.add(Calendar.SECOND, -1 * (getProfileIntervalInSeconds() * 4));          //Daily consumption contains every 4th value of the table
        }

        int nrOfReadings = rawValues.get(0).size();
        List<IntervalData> intervalDatas = new ArrayList<>();
        for (int index = 0; index < nrOfReadings; index++) {
            List<IntervalValue> intervalValues = new ArrayList<>();

            for (int inputId = 0; inputId < rawValues.size(); inputId++) {
                int multiplier = genericHeader.getRtmUnit(inputId, this.rtm).getMultiplier();
                Integer value = rawValues.get(inputId).get(index)[0];
                int status = rawValues.get(inputId).get(index)[1];
                int protocolStatus = rawValues.get(inputId).get(index)[2];
                if (value == Integer.MAX_VALUE) {
                    status = status | IntervalStateBits.CORRUPTED;
                }
                BigDecimal bd = new BigDecimal(multiplier * value);
                intervalValues.add(new IntervalValue(bd, protocolStatus, status));
            }

            //Don't add the record if it doesn't belong in the requested interval, except for pushed daily consumption data
            if (rtm.usesInitialRFCommand() || ((daily && !requestsAllowed) || (calendar.getTime().before(toDate) && calendar.getTime().after(lastReading)))) {
                intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
            }

            if (!monthly) {
                calendar.add(Calendar.SECOND, -1 * getProfileIntervalInSeconds() * (daily ? 4 : 1));   //Go back 4 intervals in case of daily consumption
            } else {
                calendar.add(Calendar.MONTH, -1);
            }
        }
        profileData.setIntervalDatas(intervalDatas);
        return profileData;
    }

    private Calendar roundTimeStamps(boolean monthly, Calendar calendar, int profileIntervalInSeconds) {
        if (rtm.isRoundDownToNearestInterval()) {
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

    /**
     * If multiframe mode is enabled, there's no need to request data in steps.
     */
    private int getSteps(int nrOfIntervals) {
        return rtm.isMultiFrame() ? nrOfIntervals : STEPS;             //TODO TEST
    }

    private List<MeterEvent> checkValid(List<MeterEvent> meterEvents, Date lastReading, Date toDate) {
        List<MeterEvent> result = new ArrayList<>();
        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getTime().after(lastReading) && meterEvent.getTime().before(toDate)) {
                result.add(meterEvent);
            }
        }
        return result;
    }

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {

        boolean usesInitialRFCommand = rtm.usesInitialRFCommand();

        List<MeterEvent> meterEvents = new ArrayList<>();
        ApplicationStatus status = rtm.getParameterFactory().readApplicationStatus();
        int numberOfPorts = rtm.getParameterFactory().readOperatingMode().readNumberOfPorts();

        if (status.isLowBatteryWarning()) {
            Date eventDate = new Date();
            if (!usesInitialRFCommand) {
                eventDate = rtm.getParameterFactory().readLowBatteryDetectionDate();
            }
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery level detected"));
            if (profileType.isEvoHop()) {
                return meterEvents;         //Low battery is the only event available in the evoHop module
            }
        }

        if (!usesInitialRFCommand) {
            meterEvents.addAll(rtm.getRadioCommandFactory().readLeakageEventTable().getMeterEvents());
        }

        if (!usesInitialRFCommand) {
            for (int input = 0; input < numberOfPorts; input++) {
                meterEvents.addAll(rtm.getParameterFactory().readSimpleBackflowDetectionFlags(input + 1).getMeterEvents());
            }
        }

        if (profileType.isPulse()) {
            if (status.isTamperDetectionOnPortA()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readTamperDetectionDate(1);
                }
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tamper detection on port A"));
            }
            if (status.isTamperDetectionOnPortB()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readTamperDetectionDate(2);
                }
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_B, "Tamper detection on port B"));
            }
            if (status.isTamperDetectionOnPortC()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readTamperDetectionDate(3);
                }
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_C, "Tamper detection on port C"));
            }
            if (status.isTamperDetectionOnPortD()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readTamperDetectionDate(4);
                }
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_D, "Tamper detection on port D"));
            }
        }

        if (profileType.isValve()) {
            if (!usesInitialRFCommand) {
                meterEvents.addAll(rtm.getParameterFactory().readLeakageDetectionStatus().getMeterEvents());
            }
            if (status.isValveFault()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readValveErrorDetectionDate();
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Valve communication error detected"));
            }
        }

        if (profileType.isEncoder()) {
            if (status.isBackFlowOnPortA()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readBackflowDate(1);
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_BACKFLOW_END_A, "Backflow detected on port A"));
            }
            if (status.isBackFlowOnPortB()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readBackflowDate(2);
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_BACKFLOW_END_B, "Backflow detected on port B"));
            }
            if (status.isEncoderCommFaultOnPortA()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readEncoderCommFaultDate(1);
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_COMMUNICATION_FAULT_A, "Encoder communication fault on port A"));
            }
            if (status.isEncoderCommFaultOnPortB()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readEncoderCommFaultDate(2);
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_COMMUNICATION_FAULT_B, "Encoder communication fault on port B"));
            }
            if (status.isEncoderMisreadOnPortA()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readEncoderReadingErrorDate(1);
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_MISREAD_A, "Encoder misread on port A"));
            }
            if (status.isEncoderMisreadOnPortB()) {
                Date eventDate = new Date();
                if (!usesInitialRFCommand) {
                    eventDate = rtm.getParameterFactory().readEncoderReadingErrorDate(2);
                }
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_MISREAD_B, "Encoder misread on port B"));
            }
        }
        return checkValid(meterEvents, lastReading, toDate);
    }
}