/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.request.ProfileDataEntry;
import com.energyict.protocolimpl.din19244.poreg2.request.register.ProfileDescription;
import com.energyict.protocolimpl.din19244.poreg2.request.register.ProfileInfo;
import com.energyict.protocolimpl.din19244.poreg2.request.register.ProfileParameters;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileDataReader {

    private Poreg poreg;

    public ProfileDataReader(Poreg poreg) {
        this.poreg = poreg;
    }

    final ProfileData getProfileData(Date lastReading, Date toDate, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        ProfileParameters parameters = poreg.getRegisterFactory().readProfileParameters();
        List<ProfileDescription> profileDescriptions = new ArrayList<ProfileDescription>();
        int profileId = 0;

        for (ProfileInfo profileInfo : parameters.getProfileParameters()) {
            if ((profileInfo.getLocation() != 0) && (profileInfo.getPercent() != 0) && (profileInfo.getSavingEvent() == 1)) {
                int interval = poreg.getRegisterFactory().readMeasurementPeriod(profileInfo.getProfileIntervalIndex());
                int index = 0;
                for (int gid : profileInfo.getGid()) {
                    if (gid != 0) {
                        ProfileDescription description = new ProfileDescription(profileInfo.getMaxBytesPerField()[0], interval, profileId, profileInfo.getFieldAddress()[index], gid, profileInfo.getNumberOfFields()[index], profileInfo.getNumberOfRegisters()[index], profileInfo.getRegisterAddress()[index]);
                        profileDescriptions.add(description);
                    }
                    index++;
                }
            }
            profileId++;
        }

        List<List<ProfileDataEntry>> profileDataEntriesForAllChannels = new ArrayList<List<ProfileDataEntry>>();
        List<String> channelNames = new ArrayList<String>();
        List<Unit> units = new ArrayList<Unit>();

        //Parse all register groups containing multiple registers, each with a fixed amount of fields.
        for (ProfileDescription description : profileDescriptions) {
            for (int registerAddress = description.getRegisterAddresses(); registerAddress < (description.getRegisterAddresses() + description.getNumberOfRegisters()); registerAddress++) {
                for (int fieldAddress = description.getFieldAddresses(); fieldAddress < (description.getFieldAddresses() + description.getNumberOfFields()); fieldAddress++) {
                    List<ProfileDataEntry> dataEntries = poreg.getRequestFactory().readProfileData(description.getLength(), description, registerAddress, fieldAddress, lastReading, toDate);
                    profileDataEntriesForAllChannels.add(dataEntries);
                    channelNames.add(getChannelName(description.getGid(), registerAddress, fieldAddress));
                    units.add(getUnit(description.getGid()));
                }
            }
        }

        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (int channel = 0; channel < profileDataEntriesForAllChannels.size(); channel++) {
            ChannelInfo channelInfo = new ChannelInfo(channel, channelNames.get(channel), units.get(channel));
            channelInfo.setCumulative();
            ExtendedValue value = profileDataEntriesForAllChannels.get(channel).get(0).getValue();
            int sign = value.getType().isSigned() ? 1 : 0;
            channelInfo.setCumulativeWrapValue(new BigDecimal(Math.pow(2, (8 * profileDataEntriesForAllChannels.get(channel).get(0).getLength()) - sign)));  //Signed value has range - 1
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);


        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (int number = 0; number < profileDataEntriesForAllChannels.get(0).size(); number++) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();

            for (List<ProfileDataEntry> profileDataEntriesForAllChannel : profileDataEntriesForAllChannels) {
                ProfileDataEntry dataEntry = profileDataEntriesForAllChannel.get(number);
                ExtendedValue value = dataEntry.getValue();
                if (value.isValid()) {        //Don't add missing values :)
                    intervalValues.add(new IntervalValue(new BigDecimal(value.getValue()), dataEntry.getStatus(), convertStatus(dataEntry.getStatus())));
                }
            }
            if (intervalValues.size() > 0) {
                intervalDatas.add(new IntervalData(profileDataEntriesForAllChannels.get(0).get(number).getDate(), 0, 0, 0, intervalValues));
            }
        }
        profileData.setIntervalDatas(intervalDatas);

        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents(lastReading, toDate));
        }

        return profileData;
    }

    private Unit getUnit(int gid) {
        switch (gid) {
            case 10:
            case 11:
            case 12:
                return Unit.get(BaseUnit.WATTHOUR, 3);
            case 13:
                return Unit.get(BaseUnit.VOLTAMPEREHOUR, 3);
            default:
                return Unit.get("");
        }
    }

    private String getChannelName(int gid, int registerAddress, int fieldAddress) {
        switch (gid) {
            case 10:
                return "Active energy (on input " + registerAddress + "), " + getFieldDescription(fieldAddress);
            case 11:
                return "Active energy (level 1), " + getFieldDescription(fieldAddress);
            case 12:
                return "Active energy (level 2), " + getFieldDescription(fieldAddress);
            case 13:
                return "Apparent energy (level 3), " + getFieldDescription(fieldAddress);
            default:
                return "";
        }
    }

    private String getFieldDescription(int fieldAddress) {
        switch (fieldAddress) {
            case 0:
                return "Result of current measuring period 1";
            case 1:
                return "Result of current measuring period 2";
            case 2:
                return "Result of current measuring period 3";
            case 3:
                return "Result of previous measuring period 1";
            case 4:
                return "Result of previous measuring period 2";
            case 5:
                return "Result of previous measuring period 3";
            case 6:
                return "Cumulative result of measuring period 1";
            case 7:
                return "Cumulative result of measuring period 2";
            case 8:
                return "Cumulative result of measuring period 3";
            case 9:
                return "Current cumulative result";
            default:
                return "";
        }
    }

    private int convertStatus(int status) {
        int result = 0;
        switch (status) {
            case 1:
                result += IntervalStateBits.POWERDOWN;
            case 2:
                result += IntervalStateBits.POWERUP;
            case 4:
                result += IntervalStateBits.SHORTLONG;
            case 8:
                result += IntervalStateBits.SHORTLONG;
            case 16:
                result += IntervalStateBits.MODIFIED;
            case 32:
                result += IntervalStateBits.OVERFLOW;
            case 64:
                result += IntervalStateBits.CORRUPTED;
            case 256:
                result += IntervalStateBits.CORRUPTED;
            case 512:
                result += IntervalStateBits.CORRUPTED;
            case 32768:
                result += IntervalStateBits.DEVICE_ERROR;
        }
        return result;
    }

    private List<MeterEvent> buildMeterEvents(Date lastReading, Date toDate) throws IOException {
        return poreg.getRegisterFactory().readEvents(lastReading, toDate);
    }
}