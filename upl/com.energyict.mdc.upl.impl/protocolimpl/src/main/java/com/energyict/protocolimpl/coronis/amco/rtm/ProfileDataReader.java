package com.energyict.protocolimpl.coronis.amco.rtm;

import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.amco.rtm.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.*;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.ExtendedDataloggingTable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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

    private Date getTimeStampOfNewestRecordMonthly(Date toDate, Date lastLoggedValue) throws IOException {
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
        List<List<Integer>> rawValues = new ArrayList<List<Integer>>();

        int startOffset = -1;
        long initialOffset = -1;
        GenericHeader genericHeader = null;

        //Get the profile data for all selected input channels, in case of periodic/weekly/monthly measuring.
        for (int i = 0; i < getNumberOfInputsUsed(); i++) {
            nrOfIntervals = initialNrOfIntervals;
            int counter = 0;
            List<Integer> values = new ArrayList<Integer>();
            while (nrOfIntervals > 0) {
                if (startOffset == -1) {
                    ExtendedDataloggingTable table = rtm.getRadioCommandFactory().readExtendedDataloggingTable(i + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), toDate);
                    if (genericHeader == null) {
                        genericHeader = table.getGenericHeader();
                    }
                    values.addAll(table.getProfileData());
                    startOffset = table.getOffset();
                    initialOffset = startOffset;
                    lastLoggedValue = table.getLastLoggedTimeStamp();
                    if (table.getProfileData().size() < getSteps(nrOfIntervals)) {
                        break;   //To avoid invalid offsets in the next iteration
                    }
                } else {
                    int offset = (startOffset + getSteps(nrOfIntervals) * counter);
                    ExtendedDataloggingTable table = rtm.getRadioCommandFactory().readExtendedDataloggingTable(i + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), offset);
                    values.addAll(table.getProfileData());
                    if (table.getProfileData().size() < getSteps(nrOfIntervals)) {
                        break;   //To avoid invalid offsets in the next iteration
                    }
                }
                counter++;
                nrOfIntervals -= getSteps(nrOfIntervals);
            }
            if (values.get(0) == Integer.MAX_VALUE) {
                rtm.getLogger().info("Port " + i + " has no meter connected, no profile data available for this port");
            } else {
                rawValues.add(values);
            }
        }
        if (rawValues.size() == 0) {
            return profileData;
        }

        return parseProfileData(genericHeader, true, rawValues, profileData, monthly, false, toDate, lastReading, lastLoggedValue, initialOffset);
    }

    public ProfileData parseProfileData(GenericHeader genericHeader, boolean requestsAllowed, List<List<Integer>> rawValues, ProfileData profileData, boolean monthly, boolean daily, Date toDate, Date lastReading, Date lastLoggedValue, long initialOffset) throws IOException {

        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
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
            ChannelInfo channelInfo = new ChannelInfo(channelId++, "Input channel " + (inputId + 1), unit);
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE)); //4 bytes long, signed value
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);

        // initialize calendar
        if (!monthly) {
            calendar.setTime(getTimeStampOfNewestRecord(lastLoggedValue, initialOffset));
            if (!ParseUtils.isOnIntervalBoundary(calendar, getProfileIntervalInSeconds())) {
                ParseUtils.roundDown2nearestInterval(calendar, getProfileIntervalInSeconds());
            }
        } else if (monthly) {
            calendar.setTime(getTimeStampOfNewestRecordMonthly(toDate, lastLoggedValue));
        }

        if (daily) {
            calendar.add(Calendar.SECOND, -1 * (getProfileIntervalInSeconds() * 4));          //Daily consumption contains every 4th value of the table  
        }

        int nrOfReadings = rawValues.get(0).size();
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (int index = 0; index < nrOfReadings; index++) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();

            for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
                int multiplier = genericHeader.getRtmUnit(inputId).getMultiplier();
                int status = 0;
                Integer value = rawValues.get(inputId).get(index);
                if (value == Integer.MAX_VALUE) {
                    status = IntervalStateBits.CORRUPTED;
                }
                BigDecimal bd = new BigDecimal(multiplier * value);
                intervalValues.add(new IntervalValue(bd, 0, status));    //The module doesn't send any information about the value's status..
            }

            //Don't add the record if it doesn't belong in the requested interval, except for pushed daily consumption data
            if ((daily && !requestsAllowed) || (calendar.getTime().before(toDate) && calendar.getTime().after(lastReading))) {
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


    /**
     * If multiframe mode is enabled, there's no need to request data in steps.
     */
    private int getSteps(int nrOfIntervals) {
        return rtm.isMultiFrame() ? nrOfIntervals : STEPS;             //TODO TEST
    }

    private List<MeterEvent> checkValid(List<MeterEvent> meterEvents, Date lastReading, Date toDate) {
        List<MeterEvent> result = new ArrayList<MeterEvent>();
        for (MeterEvent meterEvent : meterEvents) {
            if (meterEvent.getTime().after(lastReading) && meterEvent.getTime().before(toDate)) {
                result.add(meterEvent);
            }
        }
        return result;
    }

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        ApplicationStatus status = rtm.getParameterFactory().readApplicationStatus();
        int numberOfPorts = rtm.getParameterFactory().readOperatingMode().readNumberOfPorts();

        if (status.isLowBatteryWarning()) {
            Date eventDate = rtm.getParameterFactory().readLowBatteryDetectionDate();
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.BATTERY_VOLTAGE_LOW, EventStatusAndDescription.EVENTCODE_BATTERY_LOW, "Low battery level detected"));
            if (profileType.isEvoHop()) {
                return meterEvents;         //Low battery is the only event available in the evoHop module
            }
        }

        meterEvents.addAll(rtm.getRadioCommandFactory().readLeakageEventTable().getMeterEvents());

        for (int input = 0; input < numberOfPorts; input++) {
            meterEvents.addAll(rtm.getParameterFactory().readSimpleBackflowDetectionFlags(input + 1).getMeterEvents());
        }

        if (profileType.isPulse()) {
            if (status.isTamperDetectionOnPortA()) {
                Date eventDate = rtm.getParameterFactory().readTamperDetectionDate(1);
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_A, "Tamper detection on port A"));
            }
            if (status.isTamperDetectionOnPortB()) {
                Date eventDate = rtm.getParameterFactory().readTamperDetectionDate(2);
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_B, "Tamper detection on port B"));
            }
            if (status.isTamperDetectionOnPortC()) {
                Date eventDate = rtm.getParameterFactory().readTamperDetectionDate(3);
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_C, "Tamper detection on port C"));
            }
            if (status.isTamperDetectionOnPortD()) {
                Date eventDate = rtm.getParameterFactory().readTamperDetectionDate(4);
                meterEvents.add(new MeterEvent(eventDate, MeterEvent.TAMPER, EventStatusAndDescription.EVENTCODE_WIRECUT_TAMPER_D, "Tamper detection on port D"));
            }
        }

        if (profileType.isValve()) {
            meterEvents.addAll(rtm.getParameterFactory().readLeakageDetectionStatus().getMeterEvents());
            if (status.isValveFault()) {
                Date eventDate = rtm.getParameterFactory().readValveErrorDetectionDate();
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_VALVE_FAULT, "Valve communication error detected"));
            }
        }

        if (profileType.isEncoder()) {
            if (status.isBackFlowOnPortA()) {
                Date eventDate = rtm.getParameterFactory().readBackflowDate(1);
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_BACKFLOW_END_A, "Backflow detected on port A"));
            }
            if (status.isBackFlowOnPortB()) {
                Date eventDate = rtm.getParameterFactory().readBackflowDate(2);
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_BACKFLOW_END_B, "Backflow detected on port B"));
            }
            if (status.isEncoderCommFaultOnPortA()) {
                Date eventDate = rtm.getParameterFactory().readEncoderCommFaultDate(1);
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_COMMUNICATION_FAULT_A, "Encoder communication fault on port A"));
            }
            if (status.isEncoderCommFaultOnPortB()) {
                Date eventDate = rtm.getParameterFactory().readEncoderCommFaultDate(2);
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_COMMUNICATION_FAULT_B, "Encoder communication fault on port B"));
            }
            if (status.isEncoderMisreadOnPortA()) {
                Date eventDate = rtm.getParameterFactory().readEncoderReadingErrorDate(1);
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_MISREAD_A, "Encoder misread on port A"));
            }
            if (status.isEncoderMisreadOnPortB()) {
                Date eventDate = rtm.getParameterFactory().readEncoderReadingErrorDate(2);
                meterEvents.add(new MeterEvent(eventDate, 0, EventStatusAndDescription.EVENTCODE_ENCODER_MISREAD_B, "Encoder misread on port B"));
            }
        }
        return checkValid(meterEvents, lastReading, toDate);
    }
}