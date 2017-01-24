package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW.MeterProtocolType;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlarmFrameParser {

	/**
	 * generic header
	 */
	GenericHeader<?> genericHeader;

	/**
	 * alarmstatus
	 */
	AlarmStatus<?> alarmStatus;

	/**
	 * the alarm date time event
	 */
	Date date;

	/**
	 *
	 */
	int alarmData;

	/**
	 * Reference to the instantiation class
	 */
	WaveFlow100mW waveFlow100mW;

	AlarmFrameParser(byte[] data,WaveFlow100mW waveFlow100mW) throws IOException {

		this.waveFlow100mW=waveFlow100mW;

		DataInputStream dais = null;

		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

			if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.SM150E)	{
				genericHeader = new EncoderGenericHeader(dais,waveFlow100mW.getLogger(),waveFlow100mW.getTimeZone());
			}
			else if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.ECHODIS) {
				genericHeader = new MBusGenericHeader(dais,waveFlow100mW.getLogger(),waveFlow100mW.getTimeZone());
			}

			int offset = 0;
			byte[] temp = new byte[dais.available()];
			dais.read(temp);

			if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.SM150E)	{
				alarmStatus = new EncoderAlarmStatus(ProtocolUtils.getSubArray(temp, offset), waveFlow100mW);
			}
			else if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.ECHODIS) {
				alarmStatus = new MBusAlarmStatus(ProtocolUtils.getSubArray(temp, offset), waveFlow100mW);
			}

			offset += AlarmStatus.size();
			date = TimeDateRTCParser.parse(ProtocolUtils.getSubArray(temp, offset), waveFlow100mW.getTimeZone()).getTime();

			alarmData = WaveflowProtocolUtils.toInt(dais.readShort());

		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					waveFlow100mW.getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}

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

		if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.SM150E)	{
			EncoderAlarmStatus encoderAlarmStatus = (EncoderAlarmStatus)alarmStatus;
			if (encoderAlarmStatus.isBackFlowPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Back flow port A"));
			}
			if (encoderAlarmStatus.isBackFlowPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Back flow port B"));
			}
			if (encoderAlarmStatus.isEncoderCommunicationErrorPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Communication error port A"));
			}
			if (encoderAlarmStatus.isEncoderCommunicationErrorPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Communication error port B"));
			}
			if (encoderAlarmStatus.isEncoderReadingErrorPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Reading error port A"));
			}
			if (encoderAlarmStatus.isEncoderReadingErrorPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Reading error port B"));
			}
			if (encoderAlarmStatus.isExtremeLeakDetectionPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Extreme leak detection port A, measured ["+WaveflowProtocolUtils.toHexString(alarmData)+"]"));
			}
			if (encoderAlarmStatus.isExtremeLeakDetectionPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Extreme leak detection port B, measured ["+WaveflowProtocolUtils.toHexString(alarmData)+"]"));
			}
			if (encoderAlarmStatus.isResidualLeakDetectionPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Residual leak detection port A, measured ["+WaveflowProtocolUtils.toHexString(alarmData)+"]"));
			}
			if (encoderAlarmStatus.isResidualLeakDetectionPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Residual leak detection port B, measured ["+WaveflowProtocolUtils.toHexString(alarmData)+"]"));
			}
			if (encoderAlarmStatus.isLowBatteryWarning()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.BATTERY_VOLTAGE_LOW,"Alarm received: Battery low voltage, short life counter ["+WaveflowProtocolUtils.toHexString(alarmData)+"]"));
			}
		}
		else if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.ECHODIS) {
			MBusAlarmStatus mbusAlarmStatus = (MBusAlarmStatus)alarmStatus;
			if (mbusAlarmStatus.isHydraulicSensorOutOfOrderPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Hydrolic sensor out of order port A"));
			}
			if (mbusAlarmStatus.isHydraulicSensorOutOfOrderPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Hydrolic sensor out of order port B"));
			}
			if (mbusAlarmStatus.isLowBatteryWarning()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.BATTERY_VOLTAGE_LOW,"Alarm received: Battery low voltage, short life counter ["+WaveflowProtocolUtils.toHexString(alarmData)+"]"));
			}
			if (mbusAlarmStatus.isManipulationAtHydraulicSensorPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Manipulation at hydrolic sensor port A"));
			}
			if (mbusAlarmStatus.isManipulationAtHydraulicSensorPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Manipulation at hydrolic sensor port B"));
			}
			if (mbusAlarmStatus.isMeterCommunicationErrorPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.COMMUNICATION_ERROR_MBUS,"Alarm received: Communication error meter on port A"));
			}
			if (mbusAlarmStatus.isMeterCommunicationErrorPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.COMMUNICATION_ERROR_MBUS,"Alarm received: Communication error meter on port B"));
			}
			if (mbusAlarmStatus.isMeterReadingErrorPortA()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Reading error port A"));
			}
			if (mbusAlarmStatus.isMeterReadingErrorPortB()) {
				meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,"Alarm received: Reading error port B"));
			}
		}

		return meterEvents;
	}


    /**
     * Used in the acknowledgement of the push frame.
     */
    public byte[] getResponseACK() {
        return ProtocolUtils.concatByteArrays(new byte[]{(byte)0xC0},alarmStatus.getStatus());
    }

}
