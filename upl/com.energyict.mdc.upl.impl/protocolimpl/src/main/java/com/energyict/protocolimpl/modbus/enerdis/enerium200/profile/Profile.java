package com.energyict.protocolimpl.modbus.enerdis.enerium200.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.core.Modbus;

public class Profile {

	private static final int DEBUG = 0;

	private ProfileInfo profileInfo = null;
	private List profileParts		= null;
	private Modbus modBus			= null;
	
	/*
	 * Constructors
	 */

	public Profile(Modbus modBus) throws IOException {
		this.modBus = modBus;
		this.profileInfo = new ProfileInfo(this.modBus);
		this.profileParts = profileInfo.generateProfileParts();
	}
	
	/*
	 * Public getters and setters
	 */

	public ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public List getChannelInfos() {
		List channelInfos = new ArrayList(0);

		channelInfos.add(new ChannelInfo(0, 0, "P+", Unit.get("kW")));
		channelInfos.add(new ChannelInfo(1, 1, "P-", Unit.get("kW")));
		channelInfos.add(new ChannelInfo(2, 2, "S+", Unit.get("kVA")));
		channelInfos.add(new ChannelInfo(3, 3, "S-", Unit.get("kVA")));
		channelInfos.add(new ChannelInfo(4, 4, "Q1", Unit.get("kvar")));
		channelInfos.add(new ChannelInfo(5, 5, "Q2", Unit.get("kvar")));
		channelInfos.add(new ChannelInfo(6, 6, "Q3", Unit.get("kvar")));
		channelInfos.add(new ChannelInfo(7, 7, "Q4", Unit.get("kvar")));
		
		return channelInfos;
	}

	public List getIntervalDatas(Date from, Date to) throws IOException {
		List intervalDatas = new ArrayList(0);
		for (int i = 0; i < profileParts.size(); i++) {
			ProfilePart pp = (ProfilePart) profileParts.get(i);
			if (pp.getProfileInfoEntry().getInterval() == getProfileInterval()){
				intervalDatas.addAll(pp.getIntervalDatas(from, to));
			}
		}
		return intervalDatas;
	}

	public int getProfileInterval() {
		return getProfileInfo().getProfileInterval();
	}

}
