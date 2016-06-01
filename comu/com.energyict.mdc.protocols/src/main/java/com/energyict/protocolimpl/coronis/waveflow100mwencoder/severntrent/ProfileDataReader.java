package com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderDataloggingTable;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderGenericHeader;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderInternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.InternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.LeakageEventTable;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProfileDataReader {

    /**
     * reference to the implementation class of the waveflow protocol
     */
    private WaveFlow100mW waveFlow100mW;

    ProfileDataReader(WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW = waveFlow100mW;
    }

    final ProfileData getProfileData(Date lastReading, int portId, boolean includeEvents) throws IOException {

        ProfileData profileData = new ProfileData();

        if (!waveFlow100mW.isReadLoadProfile()) {  //Don't read out the profile data in this case
            waveFlow100mW.getLogger().info("Reading out load profile data is disabled by the property. Skipping.");
            if (includeEvents) {
                waveFlow100mW.getLogger().info("Reading out events only.");
                profileData.setMeterEvents(buildMeterEvents());
                profileData.sort();
            }
            return profileData;
        }

        // calc nr of intervals to read...
        Date now = new Date();
        int nrOfIntervals = (int) (((now.getTime() - lastReading.getTime()) / 1000) / waveFlow100mW.getProfileInterval()) + 1;

        // read all intervals for the period lastreading .. now
        EncoderDataloggingTable encoderDataloggingTable;

        // create channelinfos
        List<ChannelInfo> channelInfos = new ArrayList<>();
        if ((portId == 0) || (portId == 1)) {
            // only 1 channel
            encoderDataloggingTable = waveFlow100mW.getRadioCommandFactory().readEncoderDataloggingTable(portId == 0 ? true : false, portId == 1 ? true : false, nrOfIntervals, 0);
            ChannelInfo channelInfo = new ChannelInfo(0, portId == 0 ? "PortA" : "PortB", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getEncoderUnitType().toUnit());
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(2 ^ 32));
            //channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
            channelInfos.add(channelInfo);
        } else {
            // both channels
            encoderDataloggingTable = waveFlow100mW.getRadioCommandFactory().readEncoderDataloggingTable(true, true, nrOfIntervals, 0);
            ChannelInfo channelInfo = new ChannelInfo(0, "PortA", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getEncoderUnitType().toUnit());
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(2 ^ 32));
            //channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
            channelInfos.add(channelInfo);

            channelInfo = new ChannelInfo(1, "PortB", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getEncoderUnitType().toUnit());
            channelInfo.setCumulative();
            channelInfo.setCumulativeWrapValue(new BigDecimal(2 ^ 32));
            //channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
            channelInfos.add(channelInfo);

        }
        profileData.setChannelInfos(channelInfos);


        // initialize calendar
        Calendar calendar = Calendar.getInstance(waveFlow100mW.getTimeZone());
        calendar.setTime(encoderDataloggingTable.getLastLoggingRTC());


        if (!ParseUtils.isOnIntervalBoundary(calendar, waveFlow100mW.getProfileInterval() < 3600 ? waveFlow100mW.getProfileInterval() : 3600)) {
            ParseUtils.roundDown2nearestInterval(calendar, waveFlow100mW.getProfileInterval() < 3600 ? waveFlow100mW.getProfileInterval() : 3600);
        }

        // Build intervaldatas list
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        if ((portId == 0) || (portId == 1)) {
            int nrOfReadings = portId == 0 ? encoderDataloggingTable.getNrOfReadingsPortA() : encoderDataloggingTable.getNrOfReadingsPortB();
            long[] readings = portId == 0 ? encoderDataloggingTable.getEncoderReadingsPortA() : encoderDataloggingTable.getEncoderReadingsPortB();
            for (int index = 0; index < nrOfReadings; index++) {
                BigDecimal bd = null;
                if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getEncoderUnitType() != EncoderUnitType.Unknown) {
                    bd = new BigDecimal(readings[index]);
                    bd = bd.movePointLeft(8 - encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getNrOfDigitsBeforeDecimalPoint());
                } else {
                    bd = BigDecimal.ZERO;
                }
                List<IntervalValue> intervalValues = new ArrayList<>();
                intervalValues.add(new IntervalValue(bd, 0, 0));
                intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
                calendar.add(Calendar.SECOND, -1 * waveFlow100mW.getProfileInterval());
            }
        } else {
            // get the smallest nr of readings
            int smallestNrOfReadings = encoderDataloggingTable.getNrOfReadingsPortA() < encoderDataloggingTable.getNrOfReadingsPortB() ? encoderDataloggingTable.getNrOfReadingsPortA() : encoderDataloggingTable.getNrOfReadingsPortB();
            for (int index = 0; index < smallestNrOfReadings; index++) {
                BigDecimal bdA = null;
                if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getEncoderUnitType() != EncoderUnitType.Unknown) {
                    bdA = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortA()[index]);
                    bdA = bdA.movePointLeft(8 - encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getNrOfDigitsBeforeDecimalPoint());
                } else {
                    bdA = BigDecimal.ZERO;
                }
                BigDecimal bdB = null;
                if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getEncoderUnitType() != EncoderUnitType.Unknown) {
                    bdB = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortB()[index]);
                    bdB = bdB.movePointLeft(8 - encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getNrOfDigitsBeforeDecimalPoint());
                } else {
                    bdB = BigDecimal.ZERO;
                }
                List<IntervalValue> intervalValues = new ArrayList<>();
                intervalValues.add(new IntervalValue(bdA, 0, 0));
                intervalValues.add(new IntervalValue(bdB, 0, 0));
                intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
                calendar.add(Calendar.SECOND, -1 * waveFlow100mW.getProfileInterval());
            }
        }
        profileData.setIntervalDatas(intervalDatas);

        // build meterevents
        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents());
        }

        profileData.sort();

        return profileData;
    }

    private List<MeterEvent> buildMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        meterEvents.addAll(buildMeterSpecificEvents());
        for (LeakageEventTable.LeakEvent leakEvent : waveFlow100mW.getRadioCommandFactory().readLeakageEventTable().getLeakageEvents()) {
            if (leakEvent.isValid()) {
                meterEvents.add(
                        new MeterEvent(
                                leakEvent.getDate(),
                                MeterEvent.OTHER,
                                leakEvent.getDeviceCode(),
                                leakEvent.getEventDescription() + ", consumptionRate=" + WaveflowProtocolUtils.toHexString(leakEvent.getConsumptionRate())));
            }
        }
        meterEvents.addAll(buildStatusEvents());
        return meterEvents;
    }

    private List<MeterEvent> buildStatusEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();

        if (waveFlow100mW.getCachedGenericHeader() != null) {

            int leakageDetectionStatus = ((EncoderGenericHeader) waveFlow100mW.getCachedGenericHeader()).getLeakageDetectionStatus();
            if ((leakageDetectionStatus & 0x01) == 0x01) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.EVENTCODE_LEAKAGE_RESIDUAL_START_A, "Leakage detection status: Low threshold (residual leak) Port A"));
            }
            if ((leakageDetectionStatus & 0x02) == 0x02) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.EVENTCODE_LEAKAGE_EXTREME_START_A, "Leakage detection status: High threshold (extreme leak) Port A"));
            }
            if ((leakageDetectionStatus & 0x04) == 0x04) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.EVENTCODE_LEAKAGE_RESIDUAL_START_B, "Leakage detection status: Low threshold (residual leak) Port B"));
            }
            if ((leakageDetectionStatus & 0x08) == 0x08) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, EventStatusAndDescription.EVENTCODE_LEAKAGE_EXTREME_START_B, "Leakage detection status: High threshold (extreme leak) Port B"));
            }

            int applicationStatus = waveFlow100mW.getCachedGenericHeader().getApplicationStatus();
            if ((applicationStatus & 0x01) == 0x01) {
                Date timestamp = waveFlow100mW.getParameterFactory().readBatteryLifeDateEnd();
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(8), "Low battery warning"));
            }
            if ((applicationStatus & 0x02) == 0x02) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorDetectionDate(0);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(9), "Encoder communication fault detection on Port A"));
            }
            if ((applicationStatus & 0x04) == 0x04) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorDetectionDate(1);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(10), "Encoder communication fault detection on Port B"));
            }
            if ((applicationStatus & 0x08) == 0x08) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorReadingDate(0);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(11), "Encoder misread detection on Port A"));
            }
            if ((applicationStatus & 0x10) == 0x10) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorReadingDate(1);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(12), "Encoder misread detection on Port B"));
            }
            if ((applicationStatus & 0x20) == 0x20) {
                Date date = waveFlow100mW.getParameterFactory().readBackflowDetectionDate(0);
                int flags = waveFlow100mW.getParameterFactory().readBackflowDetectionFlags(0);
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, getDeviceCode(13), "Back flow detection on Port A. Detection flags: " + WaveflowProtocolUtils.toHexString(flags)));
            }
            if ((applicationStatus & 0x40) == 0x40) {
                Date date = waveFlow100mW.getParameterFactory().readBackflowDetectionDate(1);
                int flags = waveFlow100mW.getParameterFactory().readBackflowDetectionFlags(1);
                meterEvents.add(new MeterEvent(date, MeterEvent.OTHER, getDeviceCode(14), "Back flow detection on Port B. Detection flags: " + WaveflowProtocolUtils.toHexString(flags)));
            }

            if (applicationStatus != 0) {
                waveFlow100mW.getParameterFactory().writeApplicationStatus(0);
            }
        }
        return meterEvents;
    }

    private int getDeviceCode(int code) {
        return code + 0x1000;
    }

    private List<MeterEvent> buildMeterSpecificEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        for (InternalData internalData : waveFlow100mW.readInternalDatas()) {
            if (internalData != null) {
                EncoderInternalData encoderInternalData = (EncoderInternalData) internalData;
                meterEvents.addAll(encoderInternalData.getMeterEvents());
            }
        }
        return meterEvents;
    }
}