package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.protocol.*;

import java.util.*;


public class ProfileBlock {

    private static final String CHANNEL_OBIS = "0.1.128.0.0.255";
    private ProfileHeader profileHeader;
    private ProfileRecords profileRecords;

    public ProfileBlock(int[] values, double ctRatio) {
        this.profileHeader = ProfileHeader.parse(values, 0, ctRatio);
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
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        channelInfos.add(new ChannelInfo(channelInfos.size(), CHANNEL_OBIS, getProfileHeader().getEisUnit()));
        return channelInfos;
    }

    private List<IntervalData> buildIntervalDatas() {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        for (ProfileRecord profileRecord : getProfileRecords().getProfileRecords()) {
            List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
            double value = profileRecord.getValue() * getProfileHeader().getNumeratorRate() / getProfileHeader().getDenominatorRate();
            double valueWithCtRate = value * getProfileHeader().getCtRatio();

            intervalValues.add(new IntervalValue(
                    valueWithCtRate,
                    0,
                    0
            ));

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
