package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.LeakageEventTable.LeakageEvent;

public class ProfileDataReader {

	/**
	 * reference to the implementation class of the waveflow protocol
	 */
	private WaveFlow100mW waveFlow100mW;

	ProfileDataReader(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}
	
	final ProfileData getProfileData(Date lastReading, int portId, boolean includeEvents) throws UnsupportedException, IOException {
		
        ProfileData profileData = new ProfileData();
		
		// calc nr of intervals to read...
		Date now = new Date();
		int nrOfIntervals = (int)(((now.getTime() - lastReading.getTime())/1000) / waveFlow100mW.getProfileInterval())+1;
		
		// read all intervals for the period lastreading .. now
		EncoderDataloggingTable encoderDataloggingTable; 
		
		// create channelinfos
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		if ((portId==0) || (portId==1)) {
			// only 1 channel
			encoderDataloggingTable = waveFlow100mW.getRadioCommandFactory().readEncoderDataloggingTable(portId==0?true:false,portId==1?true:false,nrOfIntervals,0);
			ChannelInfo channelInfo = new ChannelInfo(0, portId==0?"PortA":"PortB", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getEncoderUnitType().toUnit());
			channelInfo.setCumulative();
			channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
			//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
			channelInfos.add(channelInfo);
		}
		else {
			// both channels
			encoderDataloggingTable = waveFlow100mW.getRadioCommandFactory().readEncoderDataloggingTable(true,true,nrOfIntervals,0);
			ChannelInfo channelInfo = new ChannelInfo(0, "PortA", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getEncoderUnitType().toUnit());
			channelInfo.setCumulative();
			channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
			//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
			channelInfos.add(channelInfo);
			
			channelInfo = new ChannelInfo(1, "PortB", encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getEncoderUnitType().toUnit());
			channelInfo.setCumulative();
			channelInfo.setCumulativeWrapValue(new BigDecimal(2^32));
			//channelInfo.setCumulativeWrapValue(new BigDecimal("100000000"));
			channelInfos.add(channelInfo);
			
		}
		profileData.setChannelInfos(channelInfos);
		

		// initialize calendar
		Calendar calendar = Calendar.getInstance(waveFlow100mW.getTimeZone());
		calendar.setTime(encoderDataloggingTable.getLastLoggingRTC());
		
		if (!ParseUtils.isOnIntervalBoundary(calendar, waveFlow100mW.getProfileInterval())) {
			ParseUtils.roundDown2nearestInterval(calendar, waveFlow100mW.getProfileInterval());
		}
		

		// Build intervaldatas list
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		if ((portId==0) || (portId==1)) {
			int nrOfReadings = portId==0?encoderDataloggingTable.getNrOfReadingsPortA():encoderDataloggingTable.getNrOfReadingsPortB();
			long[] readings = portId==0?encoderDataloggingTable.getEncoderReadingsPortA():encoderDataloggingTable.getEncoderReadingsPortB();
			for (int index = 0;index < nrOfReadings; index++) {
				BigDecimal bd = null;
				if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getEncoderUnitType() != EncoderUnitType.Unknown) {
					bd = new BigDecimal(readings[index]);
					bd = bd.movePointLeft(encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[portId].getNrOfDigitsBeforeDecimalPoint());
				}
				else {
					bd = new BigDecimal(0);
				}
				List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
				intervalValues.add(new IntervalValue(bd, 0, 0));
				intervalDatas.add(new IntervalData(calendar.getTime(),0,0,0,intervalValues));
				calendar.add(Calendar.SECOND, -1 * waveFlow100mW.getProfileInterval());
			}
		}
		else {
			
			
			// get the smallest nr of readings
			int smallestNrOfReadings = encoderDataloggingTable.getNrOfReadingsPortA()<encoderDataloggingTable.getNrOfReadingsPortB()?encoderDataloggingTable.getNrOfReadingsPortA():encoderDataloggingTable.getNrOfReadingsPortB();
			for (int index = 0;index < smallestNrOfReadings; index++) {
				BigDecimal bdA=null;
				if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getEncoderUnitType() != EncoderUnitType.Unknown) {
					bdA = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortA()[index]);
					bdA = bdA.movePointLeft(encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[0].getNrOfDigitsBeforeDecimalPoint());
				}
				else {
					bdA = new BigDecimal(0);
				}
				
				BigDecimal bdB=null;
				if (encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getEncoderUnitType() != EncoderUnitType.Unknown) {
					bdB = new BigDecimal(encoderDataloggingTable.getEncoderReadingsPortB()[index]);
					bdB = bdB.movePointLeft(encoderDataloggingTable.getEncoderGenericHeader().getEncoderUnitInfos()[1].getNrOfDigitsBeforeDecimalPoint());
				}
				else {
					bdB = new BigDecimal(0);
				}
				List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
				intervalValues.add(new IntervalValue(bdA, 0, 0));
				intervalValues.add(new IntervalValue(bdB, 0, 0));
				intervalDatas.add(new IntervalData(calendar.getTime(),0,0,0,intervalValues));
				calendar.add(Calendar.SECOND, -1 * waveFlow100mW.getProfileInterval());
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
		
		for (int portId=0;portId<2;portId++) {
			Date date = waveFlow100mW.getParameterFactory().readBackflowDetectionDate(portId);
			if (date != null) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Data internal meter alarm date ["+date+"] for port "+(portId==0?"A":"B")));
			}
		}
		
		for (int portId=0;portId<2;portId++) {
			Date date = waveFlow100mW.getParameterFactory().readCommunicationErrorDetectionDate(portId);
			if (date != null) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Communication error detection date ["+date+"] for port "+(portId==0?"A":"B")));
			}
			date = waveFlow100mW.getParameterFactory().readCommunicationErrorReadingDate(portId);
			if (date != null) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Communication error reading date ["+date+"] for port "+(portId==0?"A":"B")));
			}
		}
		
		Date date = waveFlow100mW.getParameterFactory().readBatteryLifeDateEnd();
		if (date != null) {
			int availableBatteryPower = waveFlow100mW.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife();
			meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Battery life end date ["+date+"], ["+availableBatteryPower+"%] available battery power"));
		}
		
		
		
		if (waveFlow100mW.getCachedEncoderGenericHeader() != null) {
			
			// FIXME create superclass generic data from where the severntrent and mbus generic extend and so the applicationstatus can be modelled with 1 or 2 bytes 
			int leakageDetectionStatus = waveFlow100mW.getCachedEncoderGenericHeader().getLeakageDetectionStatus();
			if ((leakageDetectionStatus & 0x01) == 0x01) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Meter internal alarm. Hydrolic sensor out of order Port A"));
			}
			if ((leakageDetectionStatus & 0x02) == 0x02) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Meter internal alarm. Manipulation at hydrolic sensor Port A"));
			}
			if ((leakageDetectionStatus & 0x04) == 0x04) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Meter internal alarm. Hydrolic sensor out of order Port B"));
			}
			if ((leakageDetectionStatus & 0x08) == 0x08) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Meter internal alarm. Manipulation at hydrolic sensor Port B"));
			}
			
			int applicationStatus = waveFlow100mW.getCachedEncoderGenericHeader().getApplicationStatus();
			if ((applicationStatus & 0x01) == 0x01) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Appl status: Low battery warning"));
			}
			if ((applicationStatus & 0x02) == 0x02) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Appl status: Meter communication fault detection on Port A"));
			}
			if ((applicationStatus & 0x04) == 0x04) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Appl status: Meter communication fault detection on Port B"));
			}
			if ((applicationStatus & 0x08) == 0x08) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Appl status: Meter misread	detection on Port A"));
			}
			if ((applicationStatus & 0x10) == 0x10) {
				meterEvents.add(new MeterEvent(new Date(),MeterEvent.OTHER,"Appl status: Meter misread	detection on Port B"));
			}
		}
	
		
		return meterEvents;
		
	} // private List<MeterEvents> buildMeterEvents()
	
} // public class ProfileDataReader
