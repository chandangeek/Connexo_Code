package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.protocol.*;

public class ProfileDataReader {

	WaveFlow100mW waveFlow100mW;

	ProfileDataReader(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}

	
	
	ProfileData getProfileData(Date lastReading, int portId) throws UnsupportedException, IOException {
		
        ProfileData profileData = new ProfileData();
		
		// calc nr of intervals to read...
		Date now = new Date();
		int nrOfIntervals = (int)(((now.getTime() - lastReading.getTime())/1000) / waveFlow100mW.getProfileInterval())+1;
		
		// read all intervals for the period lastreading .. now
		EncoderDataloggingTable encoderDataloggingTable = waveFlow100mW.getRadioCommandFactory().readEncoderDataloggingTable(portId==0?true:false,portId==1?true:false,nrOfIntervals,0);
		
		// create channelinfos
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		channelInfos.add(new ChannelInfo(0, portId==0?"PortA":"PortB", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getEncoderUnitType().toUnit()));
		profileData.setChannelInfos(channelInfos);
		

		// initialize calendar
		Calendar calendar = Calendar.getInstance(waveFlow100mW.getTimeZone());
		calendar.setTime(encoderDataloggingTable.getLastLoggingRTC());

		// Build intervaldatas list
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		int nrOfReadings = portId==0?encoderDataloggingTable.getNrOfReadingsPortA():encoderDataloggingTable.getNrOfReadingsPortB();
		long[] readings = portId==0?encoderDataloggingTable.getEncoderReadingsPortA():encoderDataloggingTable.getEncoderReadingsPortB();
		
		for (int index = 0;index < nrOfReadings; index++) {
			BigDecimal bd = new BigDecimal(readings[index]);
			bd = bd.movePointLeft(8-encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getNrOfDigitsBeforeDecimalPoint());
			List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
			intervalValues.add(new IntervalValue(bd, 0, 0));
			intervalDatas.add(new IntervalData(calendar.getTime(),0,0,0,intervalValues));
			calendar.add(Calendar.SECOND, -1 * waveFlow100mW.getProfileInterval());
		}
		profileData.setIntervalDatas(intervalDatas);		
		
		return profileData;
	}
	
	
	
}
