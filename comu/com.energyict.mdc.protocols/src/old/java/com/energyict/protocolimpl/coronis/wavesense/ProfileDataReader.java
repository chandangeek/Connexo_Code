package com.energyict.protocolimpl.coronis.wavesense;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.wavesense.core.parameter.ApplicationStatus;
import com.energyict.protocolimpl.coronis.wavesense.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.wavesense.core.radiocommand.DetectionTable;
import com.energyict.protocolimpl.coronis.wavesense.core.radiocommand.ExtendedDataloggingTable;
import com.energyict.protocolimpl.coronis.wavesense.core.radiocommand.ThresholdEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
    private WaveSense waveSense;
    private static final int EVENTCODE_HIGH_THRESHOLD = 0;
    private static final int EVENTCODE_LOW_THRESHOLD = 1;
    private static final int EVENTCODE_LOW_BATTERY = 2;
    private static final int EVENTCODE_RESET = 3;
    private static final int EVENTCODE_SENSOR_FAULT = 4;
    private static final int MONTHLY = 60 * 60 * 24 * 7 * 4;
    private static final int STEPS = 59;

    ProfileDataReader(WaveSense waveSense) {
        this.waveSense = waveSense;
    }

    private int getNrOfIntervals(Date lastReading, Date toDate) throws IOException {

        //The monthly logging doesn't have a fixed time interval.
        if (waveSense.getParameterFactory().readOperatingMode().isMonthlyMeasurement()) {
            Calendar checkDate = new GregorianCalendar(waveSense.getTimeZone());
            checkDate.setTime(toDate);
            checkDate.setLenient(true);
            int numberOfIntervals = 0;
            while (checkDate.getTime().after(lastReading)) {
                checkDate.add(Calendar.MONTH, -1);
                numberOfIntervals++;
            }
            return numberOfIntervals;
        }
        return (int) (((toDate.getTime() - lastReading.getTime()) / 1000) / getProfileIntervalInSeconds()) + 1;
    }

    private int getProfileIntervalInSeconds() throws IOException {
        return waveSense.getParameterFactory().getProfileIntervalInSeconds();
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
        Calendar calendar = Calendar.getInstance(waveSense.getTimeZone());
        calendar.setLenient(true);
        Date lastLoggedValue = new Date();
        BigDecimal[] rawIntervalData = new BigDecimal[0];

        long startOffset = -1;
        long initialOffset = -1;
        int counter = 0;
        int indexFirst = 0;

        while (nrOfIntervals > 0) {
            if (startOffset == -1) {
                ExtendedDataloggingTable table = waveSense.getRadioCommandFactory().readProfileData((nrOfIntervals < STEPS ? nrOfIntervals : STEPS), toDate);
                rawIntervalData = ProtocolTools.concatBigDecimalArrays(rawIntervalData, table.getProfileData());
                startOffset = table.getOffset();
                initialOffset = startOffset;
                lastLoggedValue = table.getMostRecentRecordTimeStamp();
                indexFirst = table.getNumberOfFirstIndex();
                if (table.getProfileData().length < STEPS) {
                    break;   //To avoid invalid offsets in the next iteration
                }
            } else {
                long offset;
                if (startOffset == 0) {
                    offset = indexFirst - (startOffset + STEPS * counter);
                } else {
                    offset = (startOffset - STEPS * counter);
                }
                offset = (offset == indexFirst ? 0 : offset);   //Offset = 0 represents the highest record number
                if (offset < 0) {
                    break;
                }
                ExtendedDataloggingTable table = waveSense.getRadioCommandFactory().readProfileData((nrOfIntervals < STEPS ? nrOfIntervals : STEPS), offset);
                rawIntervalData = ProtocolTools.concatBigDecimalArrays(rawIntervalData, table.getProfileData());
                if (table.getProfileData().length < STEPS) {
                    break;   //To avoid invalid offsets in the next iteration
                }
            }
            counter++;
            nrOfIntervals -= STEPS;
        }


        //Set up the channel and its info
        String description = waveSense.getRadioCommandFactory().readModuleType().getDescription();
        Unit unit = waveSense.getRadioCommandFactory().readModuleType().getUnit();
        ChannelInfo channelInfo = new ChannelInfo(0, "Input channel for " + description, unit);
        channelInfo.setCumulative();
        channelInfo.setCumulativeWrapValue(new BigDecimal(0xFFFF));
        channelInfos.add(channelInfo);
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
        for (BigDecimal aRawIntervalData : rawIntervalData) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
            intervalValues.add(new IntervalValue(aRawIntervalData, 0, 0));    //The module doesn't send any information about the value's status..
            if (calendar.getTime().after(lastReading) && calendar.getTime().before(toDate)) {
                intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));   //Only add the values when they fit in the requested interval.
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

    private Date getTimeStampOfNewestRecordMonthly(Date toDate, Date lastLoggedValue) throws IOException {
        Calendar lastLogged = Calendar.getInstance(waveSense.getTimeZone());
        lastLogged.setTime(lastLoggedValue);
        lastLogged.setLenient(true);

        //Go back month by month until you have the date closest to the toDate.
        while (lastLogged.getTime().after(toDate)) {
            lastLogged.add(Calendar.MONTH, -1);
        }
        return lastLogged.getTime();
    }

    //TODO: test threshold events

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        OperatingMode operatingMode = waveSense.getParameterFactory().readOperatingMode();
        ApplicationStatus status = waveSense.getParameterFactory().readApplicationStatus();

        if (thresholdEventsEnabled(operatingMode)) {
            DetectionTable eventsTable;
            ThresholdEvent[] highThresholdEvents;
            ThresholdEvent[] lowThresholdEvents;

            eventsTable = waveSense.getRadioCommandFactory().readDetectionTable();
            highThresholdEvents = eventsTable.getHighThresholdEvents();
            lowThresholdEvents = eventsTable.getLowThresholdEvents();

            for (ThresholdEvent event : highThresholdEvents) {
                String mode = operatingMode.getDetectionModeDescription();
                meterEvents.add(new MeterEvent(event.getEventDate(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EVENTCODE_HIGH_THRESHOLD, "High threshold exceeded (" + mode + "). Integrated value: " + event.getIntegratedValue() + ", duration: " + event.getDuration() + " seconds."));
            }
            for (ThresholdEvent event : lowThresholdEvents) {
                String mode = waveSense.getParameterFactory().readOperatingMode().getDetectionModeDescription();
                meterEvents.add(new MeterEvent(event.getEventDate(), MeterEvent.LIMITER_THRESHOLD_EXCEEDED, EVENTCODE_LOW_THRESHOLD, "Low threshold exceeded (" + mode + "). Integrated value: " + event.getIntegratedValue() + ", duration: " + event.getDuration() + " seconds."));
            }
        }

        if (status.endOfBatteryLifeDetected()) {
            Date endDate = waveSense.getParameterFactory().readLowBatteryDetectionDate();
            meterEvents.add(new MeterEvent(endDate, MeterEvent.BATTERY_VOLTAGE_LOW, EVENTCODE_LOW_BATTERY, "Low battery warning"));
        }

        if (status.resetDetected()) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EVENTCODE_RESET, "Reset detected"));
        }

        if (status.sensorFaultDetected()) {
            Date eventDate = waveSense.getParameterFactory().readSensorFaultDetectionDate();
            meterEvents.add(new MeterEvent(eventDate, MeterEvent.OTHER, EVENTCODE_SENSOR_FAULT, "Sensor fault detection"));
        }

        //Reset the flags
        if (status.getStatus() != 0) {
            waveSense.getParameterFactory().writeApplicationStatus(0);
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