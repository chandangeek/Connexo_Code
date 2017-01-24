package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	final byte[] getResponseACK() {
		return ProtocolUtils.concatByteArrays(new byte[]{(byte)0xC0},response);
	}

	// alarm data field 2 bytes unused...

	AlarmFrameParser(byte[] data,AbstractDLMS abstractDLMS) throws IOException {

		int offset = 1; // skip  the 0x40 or 0x41
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


		// we don't use the timestamp from the alarmframe because the waveflow AC seems to loose its RTC state
		if (alarmStatus.isPowerDown()) {
			meterEvents.add(new MeterEvent(new Date(),MeterEvent.POWERDOWN,"Alarm received: power down"));
		}

		if (alarmStatus.isPowerUp()) {
			meterEvents.add(new MeterEvent(new Date(),MeterEvent.POWERUP,"Alarm received: power up"));
		}

		return meterEvents;
	}

}
