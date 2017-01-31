/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.ActarisMBusInternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderDataloggingTable;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.InternalData;
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
                    bd = bd.movePointLeft(encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getNrOfDigitsBeforeDecimalPoint());
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
                if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getEncoderUnitType() != EncoderUnitType.Unknown) {
                    bdA = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortA()[index]);
                    bdA = bdA.movePointLeft(encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getNrOfDigitsBeforeDecimalPoint());
                } else {
                    bdA = BigDecimal.ZERO;
                }

                BigDecimal bdB = null;
                if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getEncoderUnitType() != EncoderUnitType.Unknown) {
                    bdB = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortB()[index]);
                    bdB = bdB.movePointLeft(encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getNrOfDigitsBeforeDecimalPoint());
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
        meterEvents.addAll(buildApplicationStatusEvents());
        return meterEvents;
    }

    private List<MeterEvent> buildApplicationStatusEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        if (waveFlow100mW.getCachedGenericHeader() != null) {

            /*
               "Application Status" parameter give at any time Waveflow 100mW RS232/MBUS fault, or consumptionrate, status.
               Each Waveflow 100mW RS232/MBUS internal feature that can be activated or deactivated through its
               corresponding bit in "Operating Mode" has an associated status bit in "Application status" parameter.
               User has to reset each bit by writing the "Application Status" parameter once the default has been handled.
               If a fault detection is not handled properly the corresponding bit in "Application Status" parameter will be
               set once again.

               bit12 Meter reading error detection on Port B
               bit11 Meter reading error detection on Port A
               bit10 Meter communication error detection	on Port B
               bit9 Meter communication	error detection	on Port A
               bit8 Low Battery	Warning

               bit3 Meter internal alarm on port B: Manipulation at hydraulic sensor
               bit2 Meter internal alarm on port B: Hydraulic sensor out of order
               bit1 Meter internal alarm on port A: Manipulation at hydraulic sensor
               bit0 Meter internal alarm on port A: Hydraulic sensor out of order
               */

            int applicationStatus = waveFlow100mW.getCachedGenericHeader().getApplicationStatus();

            if ((applicationStatus & 0x01) == 0x01) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, getDeviceCode(0), "Meter internal alarm. Hydrolic sensor out of order Port A"));
            }
            if ((applicationStatus & 0x02) == 0x02) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, getDeviceCode(1), "Meter internal alarm. Manipulation at hydrolic sensor Port A"));
            }
            if ((applicationStatus & 0x04) == 0x04) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, getDeviceCode(2), "Meter internal alarm. Hydrolic sensor out of order Port B"));
            }
            if ((applicationStatus & 0x08) == 0x08) {
                meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, getDeviceCode(3), "Meter internal alarm. Manipulation at hydrolic sensor Port B"));
            }
            if ((applicationStatus & 0x100) == 0x100) {
                Date timestamp = waveFlow100mW.getParameterFactory().readBatteryLifeDateEnd();
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(8), "Low battery warning"));
            }
            if ((applicationStatus & 0x200) == 0x200) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorDetectionDate(0);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(9), "Meter communication fault detection on Port A"));
            }
            if ((applicationStatus & 0x400) == 0x400) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorDetectionDate(1);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(10), "Meter communication fault detection on Port B"));
            }
            if ((applicationStatus & 0x800) == 0x800) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorReadingDate(0);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(11), "Meter misread detection on Port A"));
            }
            if ((applicationStatus & 0x1000) == 0x1000) {
                Date timestamp = waveFlow100mW.getParameterFactory().readCommunicationErrorReadingDate(1);
                meterEvents.add(new MeterEvent(timestamp, MeterEvent.OTHER, getDeviceCode(12), "Meter misread detection on Port B"));
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

    /**
     * Check the alarm code of the connected meter (included in its internal data) and create the proper events
     */
    private List<MeterEvent> buildMeterSpecificEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        for (InternalData internalData : waveFlow100mW.readInternalDatas()) {
            if (internalData != null) {
                ActarisMBusInternalData actarisMBusInternalData = (ActarisMBusInternalData) internalData;
                meterEvents.addAll(actarisMBusInternalData.getMeterEvents());
            }
        }
        return meterEvents;
    }
}