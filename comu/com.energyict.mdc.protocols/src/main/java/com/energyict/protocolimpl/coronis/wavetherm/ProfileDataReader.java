/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.wavetherm.core.parameter.ApplicationStatus;
import com.energyict.protocolimpl.coronis.wavetherm.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.AlarmEvent;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.AlarmTable;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.ExtendedDataloggingTable;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.ProfileDataValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ProfileDataReader {

    /**
     * reference to the implementation class of the Wavesense protocol
     */
    private WaveTherm waveTherm;
    private static final int EVENTCODE_HIGH_THRESHOLD = 0;
    private static final int EVENTCODE_LOW_THRESHOLD = 1;
    private static final int EVENTCODE_LOW_BATTERY = 2;
    private static final int EVENTCODE_RESET = 3;
    private static final int MONTHLY = 60 * 60 * 24 * 7 * 4;
    private static final int STEPS1 = 59;
    private static final int STEPS2 = 29;

    ProfileDataReader(WaveTherm waveTherm) {
        this.waveTherm = waveTherm;
    }

    private int getNrOfIntervals(Date lastReading, Date toDate) throws IOException {

        //The monthly logging doesn't have a fixed time interval.
        if (waveTherm.getParameterFactory().readOperatingMode().isMonthlyMeasurement()) {
            Calendar checkDate = new GregorianCalendar(waveTherm.getTimeZone());
            checkDate.setTime(toDate);
            checkDate.setLenient(true);
            int numberOfIntervals = 0;
            while (checkDate.getTime().after(lastReading)) {
                checkDate.add(Calendar.MONTH, -1);
                numberOfIntervals++;
            }
            return checkMax(numberOfIntervals);
        }
        return checkMax((int) (((toDate.getTime() - lastReading.getTime()) / 1000) / getProfileIntervalInSeconds()) + 1);
    }

    /**
     * The module fails if you request more entries than available
     */
    private int checkMax(int number) throws IOException {
        int numberOfLoggedValues = waveTherm.getParameterFactory().readNumberOfLoggedValues();
        return (number <= numberOfLoggedValues) ? number : numberOfLoggedValues;

    }

    private int getProfileIntervalInSeconds() throws IOException {
        return waveTherm.getParameterFactory().getProfileIntervalInSeconds();
    }

    final ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        if (toDate == null || toDate.after(new Date())) {
            toDate = new Date();
        }

        boolean monthly = false;
        if (getProfileIntervalInSeconds() >= MONTHLY) {
            monthly = true;
        }

        ProfileData profileData = new ProfileData();
        int nrOfIntervals = getNrOfIntervals(lastReading, toDate);
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        Calendar calendar = Calendar.getInstance(waveTherm.getTimeZone());
        calendar.setLenient(true);
        Date lastLoggedValue = new Date();
        List<ProfileDataValue> rawIntervalDataSensor1 = new ArrayList<ProfileDataValue>();
        List<ProfileDataValue> rawIntervalDataSensor2 = new ArrayList<ProfileDataValue>();

        long startOffset = -1;
        long initialOffset = -1;
        int counter = 0;
        int indexFirst = 0;

        while (nrOfIntervals > 0) {
            if (startOffset == -1) {
                ExtendedDataloggingTable table = waveTherm.getRadioCommandFactory().readProfileData((nrOfIntervals < getSteps() ? nrOfIntervals : getSteps()), toDate);
                List<ProfileDataValue> profileDataSensor1 = table.getProfileDataSensor1();
                rawIntervalDataSensor1.addAll(profileDataSensor1);

                if (getNumberOfChannels() > 1) {
                    List<ProfileDataValue> profileDataSensor2 = table.getProfileDataSensor2();
                    rawIntervalDataSensor2.addAll(profileDataSensor2);
                }
                startOffset = table.getOffset();
                initialOffset = startOffset;
                lastLoggedValue = table.getMostRecentRecordTimeStamp();
                indexFirst = table.getNumberOfFirstIndex();
                if (table.getProfileDataSensor1().size() < getSteps()) {
                    break;   //To avoid invalid offsets in the next iteration
                }
            } else {
                long offset;
                if (startOffset == 0) {
                    offset = indexFirst - (startOffset + getSteps() * counter);
                } else {
                    offset = (startOffset - getSteps() * counter);
                }
                offset = (offset == indexFirst ? 0 : offset);   //Offset = 0 represents the highest record number
                if (offset < 0) {
                    break;
                }
                ExtendedDataloggingTable table = waveTherm.getRadioCommandFactory().readProfileData((nrOfIntervals < getSteps() ? nrOfIntervals : getSteps()), offset);

                List<ProfileDataValue> profileDataSensor1 = table.getProfileDataSensor1();
                rawIntervalDataSensor1.addAll(profileDataSensor1);

                if (getNumberOfChannels() > 1) {
                    List<ProfileDataValue> profileDataSensor2 = table.getProfileDataSensor2();
                    rawIntervalDataSensor2.addAll(profileDataSensor2);
                }

                if (table.getProfileDataSensor1().size() < getSteps()) {
                    break;   //To avoid invalid offsets in the next iteration
                }
            }
            counter++;
            nrOfIntervals -= getSteps();
        }


        //Set up the channel and its info
        for (int index = 0; index < getNumberOfChannels(); index++) {
            Unit unit = Unit.get(BaseUnit.DEGREE_CELSIUS);
            ChannelInfo channelInfo = new ChannelInfo(0, "Sensor " + index, unit);
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(0xFFFF));
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);

        //Initialize calendar
        if (!monthly) {
            calendar.setTime(getTimeStampOfNewestRecord(lastLoggedValue, (initialOffset == 0 ? 0 : indexFirst - initialOffset)));
            if (!ParseUtils.isOnIntervalBoundary(calendar, getProfileIntervalInSeconds())) {
                ParseUtils.roundDown2nearestInterval(calendar, getProfileIntervalInSeconds());
            }
        } else {
            calendar.setTime(getTimeStampOfNewestRecordMonthly(toDate, lastLoggedValue));
        }

        //Add the interval values to the channel
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (int index = 0; index < rawIntervalDataSensor1.size(); index++) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
            ProfileDataValue profileDataValue1 = rawIntervalDataSensor1.get(index);
            ProfileDataValue profileDataValue2 = rawIntervalDataSensor2.get(index);
            if (profileDataValue1.isValid() && (profileDataValue2.isValid() || (getNumberOfChannels() == 1))) {    //Don't add missing/invalid values
                intervalValues.add(new IntervalValue(profileDataValue1.getValue(), 0, 0));
                if (getNumberOfChannels() > 1) {
                    intervalValues.add(new IntervalValue(profileDataValue2.getValue(), 0, 0));        //Add the data for sensor 2, if available
                }
            }
            if (calendar.getTime().after(lastReading) && calendar.getTime().before(toDate)) {
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

    private int getSteps() throws IOException {
        if (getNumberOfChannels() > 1) {
            return STEPS2;
        } else {
            return STEPS1;
        }
    }

    private int getNumberOfChannels() throws IOException {
        return waveTherm.getNumberOfChannels();
    }

    private Date getTimeStampOfNewestRecordMonthly(Date toDate, Date lastLoggedValue) throws IOException {
        Calendar lastLogged = Calendar.getInstance(waveTherm.getTimeZone());
        lastLogged.setTime(lastLoggedValue);
        lastLogged.setLenient(true);

        //Go back month by month until you have the date closest to the toDate.
        while (lastLogged.getTime().after(toDate)) {
            lastLogged.add(Calendar.MONTH, -1);
        }
        return lastLogged.getTime();
    }

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        OperatingMode operatingMode = waveTherm.getParameterFactory().readOperatingMode();
        ApplicationStatus status = waveTherm.getParameterFactory().readApplicationStatus();

        if (thresholdEventsEnabled(operatingMode)) {
            AlarmTable alarmTable = waveTherm.getRadioCommandFactory().readAlarmTable();
            for (AlarmEvent event : alarmTable.getLowThresholdEvents()) {
                String mode = operatingMode.getDetectionModeDescription();
                meterEvents.add(new MeterEvent(event.getEventDate(), MeterEvent.OTHER, EVENTCODE_HIGH_THRESHOLD, "Low threshold exceeded (" + mode + ") on sensor " + event.getSensorNumber() + ". Integrated value: " + event.getIntegratedValue() + " \u00B0C, duration: " + event.getDuration() + " minutes."));
            }
            for (AlarmEvent event : alarmTable.getHighThresholdEvents()) {
                String mode = operatingMode.getDetectionModeDescription();
                meterEvents.add(new MeterEvent(event.getEventDate(), MeterEvent.OTHER, EVENTCODE_LOW_THRESHOLD, "High threshold exceeded (" + mode + ") on sensor " + event.getSensorNumber() + ". Integrated value: " + event.getIntegratedValue() + "\u00B0C, duration: " + event.getDuration() + " minutes."));
            }
        }

        if (status.endOfBatteryLifeDetected()) {
            Date endDate = waveTherm.getParameterFactory().readLowBatteryDetectionDate();
            meterEvents.add(new MeterEvent(endDate, MeterEvent.BATTERY_VOLTAGE_LOW, EVENTCODE_LOW_BATTERY, "Low battery warning"));
        }

        if (status.resetDetected()) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EVENTCODE_RESET, "Reset detected"));
        }

        //Reset the flags
        if (status.getStatus() != 0) {
            waveTherm.getParameterFactory().resetApplicationStatus();
        }

        return checkValid(meterEvents, lastReading, toDate);
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

    private boolean thresholdEventsEnabled(OperatingMode operatingMode) {
        return ((operatingMode.highThresholdDetectionIsActivated()) || (operatingMode.lowThresholdDetectionIsActivated()));
    }

    private Date getTimeStampOfNewestRecord(Date lastLoggedValue, long offset) throws IOException {
        long timeStamp = lastLoggedValue.getTime();
        timeStamp -= 1000 * (offset * getProfileIntervalInSeconds());
        return new Date(timeStamp);
    }
}