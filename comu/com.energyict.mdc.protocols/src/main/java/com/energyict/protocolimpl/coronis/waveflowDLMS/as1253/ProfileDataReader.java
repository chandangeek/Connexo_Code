/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS.as1253;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflowDLMS.AS1253;
import com.energyict.protocolimpl.coronis.waveflowDLMS.ErrorRegisterParser;
import com.energyict.protocolimpl.coronis.waveflowDLMS.MeterEventParser;
import com.energyict.protocolimpl.coronis.waveflowDLMS.TransparantObjectAccessFactory;
import com.energyict.protocolimpl.coronis.waveflowDLMS.WaveFlowDLMSException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProfileDataReader {

    AS1253 as1253;
    private static final long MAX_REQUESTED_LP_ENTRIES = 36;

    public ProfileDataReader(AS1253 as1253) {
        this.as1253 = as1253;
    }

    public static final int CAPTURED_OBJECTS_DATE_FIELD_INDEX = 0;
    public static final int CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX = 1;
    public static final int CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX = 2;

    int readProfileInterval() throws IOException {
        AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttribute(as1253.getLoadProfileObisCode(), 4);
        return adt.intValue();
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {

        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        int profileInterval = as1253.getProfileInterval();

        if (as1253.isVerifyProfileInterval()) {
            profileInterval = readProfileInterval();
        }

        if (profileInterval != as1253.getProfileInterval()) {
            throw new WaveFlowDLMSException("Invalid profile interval. Configured is [" + as1253.getProfileInterval() + "] s, configured in meter is [" + profileInterval + "]!");
        }

        Date toDate = new Date();
        long numberOfRequestedLPEntries = 1 + ((toDate.getTime() - lastReading.getTime()) / (profileInterval * 1000));
        int max = as1253.getMaxNumberOfIntervals();
        if (max != 0) {
            numberOfRequestedLPEntries = max;   //Limit the number of intervals that is requested in total
            lastReading.setTime(toDate.getTime() - (profileInterval * max * 1000));
        }

        Date realLastReading = (Date) lastReading.clone();

        Array results = new Array();
        //Read out the entries in steps of 36, and add each response to the result array
        while (numberOfRequestedLPEntries > MAX_REQUESTED_LP_ENTRIES) {
            lastReading.setTime(toDate.getTime() - (profileInterval * MAX_REQUESTED_LP_ENTRIES * 1000));   //Create a new last reading to avoid results larger than 36 entries.
            AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttributeRange(as1253.getLoadProfileObisCode(), TransparantObjectAccessFactory.ATTRIBUTE_VALUE, lastReading, toDate);

            for (AbstractDataType abstractDataType : adt.getArray().getAllDataTypes()) {
                results.addDataType(abstractDataType);      //Add the entries to the result
            }

            toDate.setTime(toDate.getTime() - (profileInterval * MAX_REQUESTED_LP_ENTRIES * 1000));
            numberOfRequestedLPEntries -= MAX_REQUESTED_LP_ENTRIES;
        }


        AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttributeRange(as1253.getLoadProfileObisCode(), TransparantObjectAccessFactory.ATTRIBUTE_VALUE, realLastReading, toDate);
        for (AbstractDataType abstractDataType : adt.getArray().getAllDataTypes()) {
            results.addDataType(abstractDataType);      //Add the entries to the result
        }

        // parse the AXD-R returned data...
        Calendar calendar = Calendar.getInstance(as1253.getTimeZone());
        for (AbstractDataType arrayElement : results.getAllDataTypes()) {

            Structure structure = arrayElement.getStructure();
            int nrOfchannels = structure.nrOfDataTypes() - CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX; // nr of channels = nr of elements in structure - date field - startus bits field
            if (nrOfchannels == 0) {
                throw new WaveFlowDLMSException("No channels in the load profile. Might be a configuration error!");
            }

            if (channelInfos.size() == 0) {
                // because we have to take care not to do too many roundtrips, we leave the unit type responsability to EIServer to configurate.
                for (int i = 0; i < nrOfchannels; i++) {
                    channelInfos.add(new ChannelInfo(i, "AS1253_" + (i + 1), Unit.get("")));
                }
                profileData.setChannelInfos(channelInfos);
            }

            // Workaround from Peter Bungert (Elster R&D Lampertheim)
            // Due to a bug in the meter, we reset the protocolstatus each time and only use it when there is also a timestamp involved...
            int protocolStatus = 0;
            AbstractDataType structureElement = structure.getDataType(CAPTURED_OBJECTS_DATE_FIELD_INDEX);
            if (!structureElement.isNullData()) {
                // set the interval timestamp if it has a value
                DateTime dateTime = new DateTime(structureElement.getOctetString(), as1253.getTimeZone());
                calendar.setTime(dateTime.getValue().getTime());
                ParseUtils.roundUp2nearestInterval(calendar, as1253.getProfileInterval());
                protocolStatus = structure.getDataType(CAPTURED_OBJECTS_STATUSBITS_FIELD_INDEX).intValue();
            }


            IntervalData intervalData = new IntervalData(calendar.getTime(), protocolStatus2EICode(protocolStatus), protocolStatus);
            for (int index = 0; index < nrOfchannels; index++) {
                BigDecimal bd = BigDecimal.valueOf(structure.getDataType(CAPTURED_OBJECTS_CHANNELS_OFFSET_INDEX + index).longValue());
                intervalData.addValue(bd, protocolStatus, protocolStatus2EICode(protocolStatus));
            }
            profileData.addInterval(intervalData);

            // increment the interval timestamp...
            calendar.add(Calendar.SECOND, profileInterval);

        }

        if (includeEvents) {
            profileData.setMeterEvents(readMeterLogbook(lastReading));
        }

        return profileData;
    }

    private List<MeterEvent> readMeterLogbook(Date lastReading) throws IOException {

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        AbstractDataType adt = as1253.getTransparantObjectAccessFactory().readObjectAttributeRange(AS1253.LOG_PROFILE, TransparantObjectAccessFactory.ATTRIBUTE_VALUE, lastReading);

        Array array = adt.getArray();
        for (AbstractDataType arrayElement : array.getAllDataTypes()) {
            DateTime dateTime = new DateTime(arrayElement.getStructure().getDataType(0).getOctetString(), as1253.getTimeZone());
            Date date = dateTime.getValue().getTime();
            int eventCode = arrayElement.getStructure().getDataType(1).intValue();
            if (as1253.isOldFirmware()) {
                eventCode = reverseCode(eventCode);
            }
            meterEvents.addAll(MeterEventParser.parseEventCode(date, eventCode));
        }

        //Read out all 4 error registers (fatal and non fatal)
        meterEvents.addAll(ErrorRegisterParser.readMeterEvents(as1253));

        int applicationstatus = as1253.getParameterFactory().readApplicationStatus();

        if ((applicationstatus & 0x01) == 0x01) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.OTHER, 10, "Link fault with energymeter"));
        }
        if ((applicationstatus & 0x02) == 0x02) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.POWERUP, 9, "Power Back notification (RF module)"));
        }
        if ((applicationstatus & 0x04) == 0x04) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.POWERDOWN, 12, "Power down notification (RF module)"));
        }

        if ((applicationstatus & 0x7) != 0) {
            as1253.getParameterFactory().writeApplicationStatus(0);
        }

        return meterEvents;
    }

    /**
     * Old AM700 firmware sends the event code with bits reversed!
     */
    private int reverseCode(int eventCode) {
        int result = 0;
        for (int i = 0; i < 32; ++i) {
            result <<= 1;
            result |= (eventCode & 1);
            eventCode >>= 1;
        }
        return result;
    }

    private int protocolStatus2EICode(int protocolStatus) {

        /*
          b7 Power failure
          b6 Power recovery
          b5 Change of time/date
          b4 Demand reset
          b3 Seasonal switchover (summer/winter time)
          b2 Measure value disturbed
          b1 Running reserve exhausted
          b0 Fatal device error
          */

        int eiCode = 0;
        if ((protocolStatus & 0x01) == 0x01) {
            eiCode |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((protocolStatus & 0x02) == 0x02) {
            eiCode |= IntervalStateBits.BATTERY_LOW;
        }
        if ((protocolStatus & 0x04) == 0x04) {
            eiCode |= IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & 0x08) == 0x08) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & 0x10) == 0x10) {
            eiCode |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & 0x20) == 0x20) {
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & 0x40) == 0x40) {
            eiCode |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatus & 0x80) == 0x80) {
            eiCode |= IntervalStateBits.POWERDOWN;
        }

        return eiCode;
    }
}