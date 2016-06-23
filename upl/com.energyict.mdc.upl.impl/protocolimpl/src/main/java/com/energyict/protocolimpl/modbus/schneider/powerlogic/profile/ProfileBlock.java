package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.*;

import java.util.*;


public class ProfileBlock {

    private ProfileHeader profileHeader;
    private ProfileRecords profileRecords;

    public ProfileBlock(byte[] values, int recordCount) {
        this.profileHeader = ProfileHeader.parse(recordCount);
        this.profileRecords = ProfileRecords.parse(values, this.profileHeader, this.profileHeader.getWordLength());
    }

    public ProfileHeader getProfileHeader() {
        return profileHeader;
    }

    public ProfileRecords getProfileRecords() {
        return profileRecords;
    }

    public ProfileData getProfileData() {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(buildChannelInfos());
        profileData.setIntervalDatas(buildIntervalDatas());
        return profileData;
    }

    private List<ChannelInfo> buildChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Active Energy Delivered", Unit.get(BaseUnit.WATTHOUR)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Active Energy Received", Unit.get(BaseUnit.WATTHOUR)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Active Power A", Unit.get(BaseUnit.WATT)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Active Power B", Unit.get(BaseUnit.WATT)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Active Power C", Unit.get(BaseUnit.WATT)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Active Power Total", Unit.get(BaseUnit.WATT)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Apparent Energy Delivered", Unit.get(BaseUnit.WATTHOUR)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Apparent Energy Received", Unit.get(BaseUnit.WATTHOUR)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Reactive Energy Delivered", Unit.get(BaseUnit.WATTHOUR)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "Reactive Energy Received", Unit.get(BaseUnit.WATTHOUR)));
        return channelInfos;
    }

    private List<IntervalData> buildIntervalDatas() {
        List<IntervalData> intervalDatas = new ArrayList<>();
        for (ProfileRecord profileRecord : getProfileRecords().getProfileRecords()) {
            List<IntervalValue> intervalValues = new ArrayList<>();

            for(Object value: profileRecord.getValues()){
                intervalValues.add(new IntervalValue(
                        (Long)value,
                        0,
                        0
                ));
            }

            intervalDatas.add(
                    new IntervalData(
                            profileRecord.getDate(),
                            IntervalStateBits.OK,
                            profileRecord.isIncompleteIntegrationPeriod() ? 1 : 0,
                            IntervalStateBits.POWERDOWN,
                            intervalValues
                    )
            );

        }

        sortIntervalData(intervalDatas);
        return intervalDatas;
    }

    private void sortIntervalData(List<IntervalData> intervalDatas) {
        Collections.sort(intervalDatas,
                new Comparator<IntervalData>() {
                    public int compare(IntervalData o1, IntervalData o2) {
                        return o1.getEndTime().compareTo(o2.getEndTime());
                    }
                });
    }


    public Date getOldestProfileRecordDate() {
        Date oldestDate = null;
        for (ProfileRecord profileRecord : getProfileRecords().getProfileRecords()) {
            if (oldestDate == null) {
                oldestDate = profileRecord.getDate();
            } else if (oldestDate.after(profileRecord.getDate())) {
                oldestDate = profileRecord.getDate();
            }
        }

        return oldestDate;
    }
}
