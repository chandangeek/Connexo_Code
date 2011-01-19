package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

public class AlarmFrameParser {

	/**
	 * generic header
	 */
	GenericHeader genericHeader;
	
	/**
	 * alarmstatus
	 */
	AlarmStatus alarmStatus;

	/**
	 * the alarm date time event
	 */
	Date date;
	
	/**
	 * Response byte array. this is different per implementation
	 * For the waveflow AC it is the alarmstatus.
	 */
	byte[] response;

	final byte[] getResponse() {
		return response;
	}

	// alarm data field 2 bytes unused...
	
	AlarmFrameParser(byte[] data,AbstractDLMS abstractDLMS) throws IOException {
		
		int offset = 0;
		genericHeader = new GenericHeader(ProtocolUtils.getSubArray(data, offset), abstractDLMS);
		offset += GenericHeader.size();
		
		response = ProtocolUtils.getSubArray2(data, offset, AlarmStatus.size());
		offset += AlarmStatus.size();
		alarmStatus = new AlarmStatus(response, abstractDLMS);

		date = TimeDateRTCParser.parse(ProtocolUtils.getSubArray(data, offset), abstractDLMS.getTimeZone()).getTime();
	}

	final GenericHeader getGenericHeader() {
		return genericHeader;
	}

	final AlarmStatus getAlarmStatus() {
		return alarmStatus;
	}

	final Date getDate() {
		return date;
	}
	
	
	final List getMeterEvents() {
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		if (alarmStatus.isLinkFaultWithMeter()) {
			meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Link fault with meter"));
		}
		
		if (alarmStatus.isPowerDown()) {
			meterEvents.add(new MeterEvent(date,MeterEvent.POWERDOWN,"Alarm received: power down"));
		}
		
		if (alarmStatus.isPowerUp()) {
			meterEvents.add(new MeterEvent(date,MeterEvent.POWERUP,"Alarm received: power up"));
		}
		
		return meterEvents;
	}
	
}
