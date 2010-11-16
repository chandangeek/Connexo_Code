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

	/**
	 * reference to the implementation class of the waveflow protocol
	 */
	private WaveLogV2 waveLogV2;

	ProfileDataReader(WaveLogV2 waveLogV2) {
		this.waveLogV2 = waveLogV2;
	}
	
	final ProfileData getProfileData(Date lastReading, int portId, boolean includeEvents) throws UnsupportedException, IOException {
		
        ProfileData profileData = new ProfileData();
		
		// calc nr of intervals to read...
		Date now = new Date();
		int nrOfIntervals = (int)(((now.getTime() - lastReading.getTime())/1000) / waveLogV2.getProfileInterval())+1;
		
		//??? 4 inputs
		//??? sampling rate		
		
		// read all intervals for the period lastreading .. now
		ExtendedDataloggingTable encoderDataloggingTable; 
		
		// create channelinfos
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		if ((portId==0) || (portId==1)) {
			// only 1 channel
			encoderDataloggingTable = waveLogV2.getRadioCommandFactory().readExtendedDataloggingTable(portId==0?true:false,portId==1?true:false,false,false,nrOfIntervals,0);
			ChannelInfo channelInfo = new ChannelInfo(0, portId==0?"InputA":"InputA", Unit.get(""));
			channelInfo.setCumulative();
			channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
			//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
			channelInfos.add(channelInfo);
		}
		else {
			// both channels
			encoderDataloggingTable = waveLogV2.getRadioCommandFactory().readExtendedDataloggingTable(true,true,false,false,nrOfIntervals,0);
			ChannelInfo channelInfo = new ChannelInfo(0, "InputA", Unit.get(""));
			channelInfo.setCumulative();
			channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
			//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
			channelInfos.add(channelInfo);
			
			channelInfo = new ChannelInfo(1, "InputB", Unit.get(""));
			channelInfo.setCumulative();
			channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
			//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
			channelInfos.add(channelInfo);
			
		}
		profileData.setChannelInfos(channelInfos);
		

		// initialize calendar
		Calendar calendar = Calendar.getInstance(waveLogV2.getTimeZone());
		calendar.setTime(encoderDataloggingTable.getLastLoggingRTC());

		if (!ParseUtils.isOnIntervalBoundary(calendar, waveLogV2.getProfileInterval())) {
			ParseUtils.roundDown2nearestInterval(calendar, waveLogV2.getProfileInterval());
		}
		
		
		// Build intervaldatas list
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		if ((portId==0) || (portId==1)) {
			int nrOfReadings = portId==0?encoderDataloggingTable.getNrOfReadingsPortA():encoderDataloggingTable.getNrOfReadingsPortB();
			long[] readings = portId==0?encoderDataloggingTable.getEncoderReadingsPortA():encoderDataloggingTable.getEncoderReadingsPortB();
			for (int index = 0;index < nrOfReadings; index++) {
				BigDecimal bd = null;
				bd = new BigDecimal(readings[index]);
				List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
				intervalValues.add(new IntervalValue(bd, 0, 0));
				intervalDatas.add(new IntervalData(calendar.getTime(),0,0,0,intervalValues));
				calendar.add(Calendar.SECOND, -1 * waveLogV2.getProfileInterval());
			}
		}
		else {
			// get the smallest nr of readings
			int smallestNrOfReadings = encoderDataloggingTable.getNrOfReadingsPortA()<encoderDataloggingTable.getNrOfReadingsPortB()?encoderDataloggingTable.getNrOfReadingsPortA():encoderDataloggingTable.getNrOfReadingsPortB();
			for (int index = 0;index < smallestNrOfReadings; index++) {
				BigDecimal bdA=null;
				bdA = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortA()[index]);
				BigDecimal bdB=null;
				bdB = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortB()[index]);
				List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
				intervalValues.add(new IntervalValue(bdA, 0, 0));
				intervalValues.add(new IntervalValue(bdB, 0, 0));
				intervalDatas.add(new IntervalData(calendar.getTime(),0,0,0,intervalValues));
				calendar.add(Calendar.SECOND, -1 * waveLogV2.getProfileInterval());
			}
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
