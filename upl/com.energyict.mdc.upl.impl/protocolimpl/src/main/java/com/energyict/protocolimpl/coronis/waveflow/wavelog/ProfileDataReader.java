package com.energyict.protocolimpl.coronis.waveflow.wavelog;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.*;

public class ProfileDataReader {

	private final int MAX_NR_OF_INPUTS=4;
	
	/**
	 * reference to the implementation class of the waveflow protocol
	 */
	private WaveLogV2 waveLogV2;

	ProfileDataReader(WaveLogV2 waveLogV2) {
		this.waveLogV2 = waveLogV2;
	}
	
	/**
	 * Validate if the mask has inputId true or false
	 * @param inputId
	 * @return true or false for the input
	 */
	private final boolean validateMask(int inputMask,int inputId) {
		int inputIdMask = 0x01 << inputId;
		return (inputMask&inputIdMask) == inputIdMask; 
	}
	
	final ProfileData getProfileData(Date lastReading, int inputMask, boolean includeEvents) throws UnsupportedException, IOException {
		
		// portmask bit 3..0 = input D..A
		
		
        ProfileData profileData = new ProfileData();
		
		// calc nr of intervals to read...
		Date now = new Date();
		int nrOfIntervals = (int)(((now.getTime() - lastReading.getTime())/1000) / waveLogV2.getProfileInterval())+1;
		
		//??? 4 inputs
		//??? sampling rate		
		
		// read all intervals for the period lastreading .. now
		ExtendedDataloggingTable extendedDataloggingTable; 
		
		// create channelinfos
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		extendedDataloggingTable = waveLogV2.getRadioCommandFactory().readExtendedDataloggingTable(validateMask(inputMask,0),validateMask(inputMask,1),validateMask(inputMask,2),validateMask(inputMask,3),nrOfIntervals,0);
		int channelId=0;
		for (int inputId=0;inputId<MAX_NR_OF_INPUTS;inputId++) {
			if (validateMask(inputMask,inputId)) {
				ChannelInfo channelInfo = new ChannelInfo(channelId++,"channel_"+inputId , Unit.get(""));
				channelInfo.setCumulative();
				channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
				//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
				channelInfos.add(channelInfo);
			}
		}
		
		profileData.setChannelInfos(channelInfos);
		

		// initialize calendar
		Calendar calendar = Calendar.getInstance(waveLogV2.getTimeZone());
		calendar.setTime(extendedDataloggingTable.getLastLoggingRTC());

		if (!ParseUtils.isOnIntervalBoundary(calendar, waveLogV2.getProfileInterval())) {
			ParseUtils.roundDown2nearestInterval(calendar, waveLogV2.getProfileInterval());
		}
		
		// Build intervaldatas list
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		
		// get the smallest nr of readings
		int smallestNrOfReadings = extendedDataloggingTable.getSmallestNrOfReadings();

		for (int index = 0;index < smallestNrOfReadings; index++) {
			List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
			for (int inputId=0;inputId<MAX_NR_OF_INPUTS;inputId++) {
				if (validateMask(inputMask,inputId)) {
					BigDecimal bd=new BigDecimal(extendedDataloggingTable.getReadingsInputs()[inputId][index]);
					intervalValues.add(new IntervalValue(bd, 0, 0));
				}
			}
			intervalDatas.add(new IntervalData(calendar.getTime(),0,0,0,intervalValues));
			calendar.add(Calendar.SECOND, -1 * waveLogV2.getProfileInterval());
		}
		profileData.setIntervalDatas(intervalDatas);
		
		// build meterevents
		if (includeEvents) {
			profileData.setMeterEvents(buildMeterEvents());
		}
		
		return profileData;
	}

	private List<MeterEvent> buildMeterEvents() throws IOException {
			
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		
		return meterEvents;
		
	} // private List<MeterEvents> buildMeterEvents()
	
} // public class ProfileDataReader
