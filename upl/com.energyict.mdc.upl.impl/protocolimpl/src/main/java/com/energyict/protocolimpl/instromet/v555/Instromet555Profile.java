package com.energyict.protocolimpl.instromet.v555;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.instromet.v555.tables.TableFactory;

public class Instromet555Profile {
	
	private Instromet555 instromet555;
	
	public Instromet555Profile(Instromet555 instromet555) {
        this.instromet555=instromet555;
    }
	
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        TableFactory tablefactory = instromet555.getTableFactory();
        List channelInfos = tablefactory.getLoggingConfigurationTable()
        	            .getChannelInfos();
        int size = channelInfos.size();
        System.out.println("size channelinfos= " + size);
        for (int i = 0; i < size; i++) {
        	profileData.addChannel((ChannelInfo) channelInfos.get(i));
        	System.out.println("addChannel" + ((ChannelInfo) channelInfos.get(i)).getName()
                    + ", " + ((ChannelInfo) channelInfos.get(i)).getUnit()
                    + ", " + ((ChannelInfo) channelInfos.get(i)).getId() 
                    + ", " + ((ChannelInfo) channelInfos.get(i)).getCumulativeWrapValue());
        }
        List intervalDatas = 
        	tablefactory.getLoggedDataTable(lastReading).getIntervalDatas();
        size = intervalDatas.size();
        for (int i = 0; i < size; i++) 
        	profileData.addInterval((IntervalData) intervalDatas.get(i));
        profileData.sort();
        return profileData;
	}

}
