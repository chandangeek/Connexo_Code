package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class EncoderAlarmStatus extends AlarmStatus {

	/*
	    CS-FW-SFW-WF_100mW_Encoder-E08 page 35
	    3 bytes, 24 flag bits	
	    BYTE 2      BYTE 1     BYTE 0
	    bit 23..16  bit 15..8  bit 7..0
    */
	
	private boolean backFlowPortB; // bit 14
	private boolean backFlowPortA; // bit 13
	private boolean encoderReadingErrorPortB; // bit 12
	private boolean encoderReadingErrorPortA; // bit 11
	private boolean encoderCommunicationErrorPortB; // bit 10
	private boolean encoderCommunicationErrorPortA; // bit 9
	private boolean lowBatteryWarning; //bit 8
	private boolean extremeLeakDetectionPortB; // bit 3
	private boolean residualLeakDetectionPortB; // bit 2
	private boolean extremeLeakDetectionPortA; // bit 1
	private boolean residualLeakDetectionPortA; // bit 0
	
	
	EncoderAlarmStatus(byte[] data, WaveFlow100mW waveFlow100mW) throws IOException {
		super(data, waveFlow100mW);
		
		backFlowPortB = (data[1] & 0x40)==0x40;
		backFlowPortA = (data[1] & 0x20)==0x20;
		encoderReadingErrorPortB = (data[1] & 0x10)==0x10;
		encoderReadingErrorPortA = (data[1] & 0x08)==0x08;
		encoderCommunicationErrorPortB = (data[1] & 0x04)==0x04;
		encoderCommunicationErrorPortA = (data[1] & 0x02)==0x02;
		lowBatteryWarning = (data[1] & 0x01)==0x01;
		
		extremeLeakDetectionPortB = (data[2] & 0x08)==0x08;
		residualLeakDetectionPortB = (data[2] & 0x04)==0x04;
		extremeLeakDetectionPortA = (data[2] & 0x02)==0x02;
		residualLeakDetectionPortA = (data[2] & 0x01)==0x01;
	}

	static int size() {
		return 3;
	}
	
	final boolean isBackFlowPortB() {
		return backFlowPortB;
	}

	final boolean isBackFlowPortA() {
		return backFlowPortA;
	}

	final boolean isEncoderReadingErrorPortB() {
		return encoderReadingErrorPortB;
	}

	final boolean isEncoderReadingErrorPortA() {
		return encoderReadingErrorPortA;
	}

	final boolean isEncoderCommunicationErrorPortB() {
		return encoderCommunicationErrorPortB;
	}

	final boolean isEncoderCommunicationErrorPortA() {
		return encoderCommunicationErrorPortA;
	}

	final boolean isLowBatteryWarning() {
		return lowBatteryWarning;
	}

	final boolean isExtremeLeakDetectionPortB() {
		return extremeLeakDetectionPortB;
	}

	final boolean isResidualLeakDetectionPortB() {
		return residualLeakDetectionPortB;
	}

	final boolean isExtremeLeakDetectionPortA() {
		return extremeLeakDetectionPortA;
	}

	final boolean isResidualLeakDetectionPortA() {
		return residualLeakDetectionPortA;
	}
	
	
}
