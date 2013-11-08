package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class MBusAlarmStatus extends AlarmStatus {

	
	/*
	  CS-FW-SFW-WF_100mW_RS232MBUS-E04 page 28
	  3 bytes, 24 flag bits	
	  BYTE 2      BYTE 1     BYTE 0
	  bit 23..16  bit 15..8  bit 7..0
	 */
	
	private boolean meterReadingErrorPortB; // bit 12
	private boolean meterReadingErrorPortA; // bit 11
	private boolean meterCommunicationErrorPortB; // bit 10
	private boolean meterCommunicationErrorPortA; // bit 9
	private boolean lowBatteryWarning; //bit 8
	
	private boolean manipulationAtHydraulicSensorPortB; // bit 3
	private boolean hydraulicSensorOutOfOrderPortB; // bit 2
	private boolean manipulationAtHydraulicSensorPortA; // bit 1
	private boolean hydraulicSensorOutOfOrderPortA; // bit 0
	
	MBusAlarmStatus(byte[] data, WaveFlow100mW waveFlow100mW) throws IOException {
		super(data, waveFlow100mW);
		
		meterReadingErrorPortB = (data[1] & 0x10)==0x10;
		meterReadingErrorPortA = (data[1] & 0x08)==0x08;
		meterCommunicationErrorPortB = (data[1] & 0x04)==0x04;
		meterCommunicationErrorPortA = (data[1] & 0x02)==0x02;
		lowBatteryWarning = (data[1] & 0x01)==0x01;
		
		manipulationAtHydraulicSensorPortB = (data[2] & 0x08)==0x08;
		hydraulicSensorOutOfOrderPortB = (data[2] & 0x04)==0x04;
		manipulationAtHydraulicSensorPortA = (data[2] & 0x02)==0x02;
		hydraulicSensorOutOfOrderPortA = (data[2] & 0x01)==0x01;
	}

	static int size() {
		return 3;
	}	

	final boolean isMeterReadingErrorPortB() {
		return meterReadingErrorPortB;
	}

	final boolean isMeterReadingErrorPortA() {
		return meterReadingErrorPortA;
	}

	final boolean isMeterCommunicationErrorPortB() {
		return meterCommunicationErrorPortB;
	}

	final boolean isMeterCommunicationErrorPortA() {
		return meterCommunicationErrorPortA;
	}

	final boolean isLowBatteryWarning() {
		return lowBatteryWarning;
	}

	final boolean isManipulationAtHydraulicSensorPortB() {
		return manipulationAtHydraulicSensorPortB;
	}

	final boolean isHydraulicSensorOutOfOrderPortB() {
		return hydraulicSensorOutOfOrderPortB;
	}

	final boolean isManipulationAtHydraulicSensorPortA() {
		return manipulationAtHydraulicSensorPortA;
	}

	final boolean isHydraulicSensorOutOfOrderPortA() {
		return hydraulicSensorOutOfOrderPortA;
	}
	
}
