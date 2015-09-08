package com.energyict.protocolimpl.modbus.socomec.countis.e44.profile;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProfileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 9/10/2014 - 10:34
 */
public class ProfileBlock {

    private static final String CHANNEL_OBIS = "0.1.128.0.0.255";
    private ProfileHeader profileHeader;
    private ProfileRecords profileRecords;

    public ProfileBlock(int[] values) {
        this.profileHeader = ProfileHeader.parse(values, 0);
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
            intervalValues.add(new IntervalValue(
                    (double) profileRecord.getValue() * getProfileHeader().getNumeratorRate() / getProfileHeader().getDenominatorRate(),
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
