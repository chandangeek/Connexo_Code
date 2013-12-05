package com.energyict.protocolimpl.instromet.v444;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocolimpl.instromet.v444.tables.TableFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Instromet444Profile {

	private Instromet444 instromet444;

	public Instromet444Profile(Instromet444 instromet444) {
        this.instromet444=instromet444;
    }

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        TableFactory tablefactory = instromet444.getTableFactory();
        List channelInfos = tablefactory.getLoggingConfigurationTable()
        	            .getChannelInfos();
        int size = channelInfos.size();
        for (int i = 0; i < size; i++)
        	profileData.addChannel((ChannelInfo) channelInfos.get(i));
        List intervalDatas =
        	tablefactory.getLoggedDataTable(lastReading).getIntervalDatas();
        size = intervalDatas.size();
        for (int i = 0; i < size; i++)
        	profileData.addInterval((IntervalData) intervalDatas.get(i));
        profileData.sort();
        return profileData;
	}

}
