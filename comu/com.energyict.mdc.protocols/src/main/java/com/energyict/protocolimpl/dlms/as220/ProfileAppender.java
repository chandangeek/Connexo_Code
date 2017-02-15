/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jme
 *
 */
public final class ProfileAppender {

	private ProfileAppender() {
		// Hide util class constructor
	}

	/**
	 * @param firstProfile
	 * @param secondProfile
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ProfileData appendProfiles(ProfileData firstProfile, ProfileData secondProfile) {
		ProfileData pd = new ProfileData();

		pd.setIntervalDatas(appendIntervalDatas(firstProfile.getIntervalDatas(), secondProfile.getIntervalDatas(), firstProfile.getNumberOfChannels() + secondProfile.getNumberOfChannels()));
		pd.setChannelInfos(appendChannelInfos(firstProfile.getChannelInfos(), secondProfile.getChannelInfos()));
		pd.setMeterEvents(appendMeterEvents(firstProfile.getMeterEvents(), secondProfile.getMeterEvents()));

		return pd;
	}

	/**
     *
     * @param firstIntervalDatas
     * @param secondIntervalDatas
     * @param totalChannels
	 * @return
	 */
	public static List<IntervalData> appendIntervalDatas(List<IntervalData> firstIntervalDatas, List<IntervalData> secondIntervalDatas, int totalChannels) {
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();

		for (IntervalData fid : firstIntervalDatas) {
			IntervalData intervalData = null;
			IntervalData id = null;

			int firstChannelCount = fid.getValueCount();
			int secondChannelCount = totalChannels - firstChannelCount;

			for (IntervalData sid : secondIntervalDatas) {
				if (fid.getEndTime().compareTo(sid.getEndTime()) == 0) {
					id = sid;
					break;
				}
			}

			if (id != null) {
				intervalData = new IntervalData(fid.getEndTime(), fid.getEiStatus(), fid.getProtocolStatus(), fid.getTariffCode());
				List<IntervalValue> intervalValues = fid.getIntervalValues();
				for (IntervalValue iv : intervalValues) {
					intervalData.addValue(iv.getNumber(), iv.getEiStatus(), iv.getEiStatus());
				}
				intervalValues = id.getIntervalValues();
				for (IntervalValue iv : intervalValues) {
					intervalData.addValue(iv.getNumber(), iv.getEiStatus(), iv.getEiStatus());
				}
			} else {
				intervalData = new IntervalData(fid.getEndTime(), fid.getEiStatus(), fid.getProtocolStatus(), fid.getTariffCode());
				List<IntervalValue> intervalValues = fid.getIntervalValues();
				for (IntervalValue iv : intervalValues) {
					intervalData.addValue(iv.getNumber(), iv.getEiStatus(), iv.getEiStatus());
				}
				for (int i = 0; i < secondChannelCount; i++) {
					intervalData.addValue(0, IntervalData.MISSING, IntervalData.MISSING);
				}
			}
			intervalDatas.add(intervalData);
		}

		return intervalDatas;
	}

	/**
	 * @param firstChannelInfos
	 * @param secondChannelInfos
	 * @return
	 */
	public static List<ChannelInfo> appendChannelInfos(List<ChannelInfo> firstChannelInfos, List<ChannelInfo> secondChannelInfos) {
		List<ChannelInfo> ci = new ArrayList<ChannelInfo>();
		for (ChannelInfo channelInfo : firstChannelInfos) {
			ci.add(channelInfo);
		}
		for (ChannelInfo channelInfo : secondChannelInfos) {
			ci.add(channelInfo);
		}

        for (int i = 0; i < ci.size(); i++) {
            ci.get(i).setChannelId(i);
            ci.get(i).setId(i);
        }

		return ci;
	}

	/**
	 * @param firstMeterEvents
	 * @param secondMeterEvents
	 * @return
	 */
	public static List<MeterEvent> appendMeterEvents(List<MeterEvent> firstMeterEvents, List<MeterEvent> secondMeterEvents) {
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		for (MeterEvent meterEvent : firstMeterEvents) {
			meterEvents.add(meterEvent);
		}
		for (MeterEvent meterEvent : secondMeterEvents) {
			meterEvents.add(meterEvent);
		}
		return meterEvents;
	}


}
