package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.energyict.protocolimpl.coronis.core.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderModelInfo.EncoderModelType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;

public class EncoderGenericHeader {


	private static final int GENERIC_STRUCTURE_SIZE = 23;


	/**
	 * 23 bytes generic header. 
	 * first byte is unused
	 */
	
	/**
	 * The "Operating Mode" is used to activate/deactivate each Waveflow 100mW Encoder feature. This
	 * parameter is accessible through the command write parameters.
	 * "Operating Mode" parameter is systematically returned in generic header present in almost each response
	 * frame of the Waveflow 500mW Encoder.
	 * bit15..9 unused
	 * bit8 backflow detection activated/deactivated
	 * bit7 Encoder misread detection activated/deactivated
	 * bit6 Extreme leak detection activated/deactivated
	 * bit5 Residual leak detection activated/deactivated
	 * bit4 Encoder	communication fault detection activated/deactivated
	 * bit3..2 Datalogging 00 : deactivated 01 : time steps mngt 10 : once a week mngt 11 : once a month mngt
	 * bit1..0 Ports management 00 : one Port (A) 01 :2 Ports (A & B)	
	 */
	int operatingMode; // 2 bytes
	
	
	/**
	 * "Application Status" parameter give at any time Waveflow 100mW Encoder fault, or consumption-rate, status.
	 * Each Waveflow 100mW Encoder internal feature that can be activated or deactivated through its
	 * corresponding bit in "Operating Mode" has an associated status bit in "Application status" parameter.
	 * User has to reset each bit by writing the "Application Status" parameter once the default has been handled.
	 * If a fault detection is not handled properly the corresponding bit in "Application Status" parameter will be
	 * set once again. (1 detected, 0 not detected)
	 * bit7	Leak detection (extreme or residual)
	 * bit6	Back flow detection	on Port B
	 * bit5 Back flow detection	on Port A
	 * bit4 Encoder	misread	detection on Port B
	 * bit3 Encoder misread	detection on Port A
	 * bit2 Encoder	communication fault detection on Port B
	 * bit1 Encoder	communication fault detection on Port A
	 * bit0 Low	Battery	Warning
	 */
	int applicationStatus; // 1 byte
	
	/**
	 * This control byte is used to detect leakage in real time. Indeed, each bit is set to one when a leakage is
	 * detected and reset to zero automatically when it ended. This information can be read by the standard read
	 * parameter command. This parameter is in read access only.
	 * bit3 high threshold (extreme leak)	Port B
	 * bit2 Low	threshold (residual	leak) Port B
	 * bit1 High threshold (extreme	leak) Port A
	 * bit0 Low	threshold (residual	leak) Port A	
	 */
	int leakageDetectionStatus; // 1 byte
	
	Date currentRTC; // 7 bytes
	
	/**
	 * The QoS value gives an image of the last beacon radio reception signal strength
	 */
	int qos; // 1 byte
	
	/**
	 * The "Short Life Counter" value gives correspond to the 2 most significant bytes of the real "Life Counter"
	 * (3 bytes). This real "Life Counter" gives an estimated quantity of energy that remains in Waveflow 100mW
	 * Encoder battery. User software has to take into account the default value of this counter to compute an
	 * estimated remaining lifetime.
	 */
	int shortLiftCounter; // 2 bytes
	
	/**
	 * The encodermodel id and manufacturer for the port A and B
	 */
	EncoderModelInfo[] encoderModelInfos = new EncoderModelInfo[2];
	
	/**
	 * The encoder unit and digits info for the port A and B
	 */
	EncoderUnitInfo[] encoderUnitInfos = new EncoderUnitInfo[2];
	
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EncoderGenericHeader:\n");
        strBuff.append("   operatingMode="+WaveflowProtocolUtils.toHexString(getOperatingMode())+"\n");
        strBuff.append("   applicationStatus="+WaveflowProtocolUtils.toHexString(getApplicationStatus())+"\n");
        strBuff.append("   leakageDetectionStatus="+WaveflowProtocolUtils.toHexString(getLeakageDetectionStatus())+"\n");
        strBuff.append("   currentRTC="+getCurrentRTC()+"\n");
        strBuff.append("   qos="+WaveflowProtocolUtils.toHexString(getQos())+"\n");
        strBuff.append("   shortLiftCounter="+WaveflowProtocolUtils.toHexString(getShortLiftCounter())+"\n");
        for (int i=0;i<getEncoderModelInfos().length;i++) {
            strBuff.append("       encoderModelInfos["+i+"]="+getEncoderModelInfos()[i]+"\n");
        }
        for (int i=0;i<getEncoderUnitInfos().length;i++) {
            strBuff.append("       encoderUnitInfos["+i+"]="+getEncoderUnitInfos()[i]+"\n");
        }
        return strBuff.toString();
    }    
    
	EncoderGenericHeader(DataInputStream dais, Logger logger, TimeZone timeZone) throws IOException {
			
		dais.readByte(); // skip unused byte (value 0)
		operatingMode = WaveflowProtocolUtils.toInt(dais.readShort());
		applicationStatus = WaveflowProtocolUtils.toInt(dais.readByte());
		leakageDetectionStatus = WaveflowProtocolUtils.toInt(dais.readByte());

		byte[] temp = new byte[7];
		dais.read(temp);
		currentRTC = TimeDateRTCParser.parse(temp, timeZone).getTime();
		
		qos = WaveflowProtocolUtils.toInt(dais.readByte());
		
		shortLiftCounter = WaveflowProtocolUtils.toInt(dais.readShort());
		
		/*
		Encoder unit on Port A
		byte7   Number of digits before the decimal point 
		byte6   Unit
		Encoder unit on Port B 
		byte5   Number of digits before the decimal point 
		byte4   Unit
		Encoder model on Port A	
		byte3   Encoder Manufacturer 
		byte2   Adapter code
		Encoder model on Port B	
		byte1   Encoder Manufacturer 
		byte0   Adapter code
		*/

		for (int port = 0;port<2;port++) {
			int nrOfDigitsBeforeDecimalPoint = WaveflowProtocolUtils.toInt(dais.readByte());
			int id = WaveflowProtocolUtils.toInt(dais.readByte());
			encoderUnitInfos[port] = new EncoderUnitInfo(EncoderUnitType.fromId(id),nrOfDigitsBeforeDecimalPoint);
		}
		
		for (int port = 0;port<2;port++) {
			int id = WaveflowProtocolUtils.toInt(dais.readByte());
			int manufacturerId = WaveflowProtocolUtils.toInt(dais.readByte());
			encoderModelInfos[port] = new EncoderModelInfo(EncoderModelType.fromId(id),manufacturerId);
		}
	}
	
	static int size() {
		return GENERIC_STRUCTURE_SIZE;
	}
	
	final int getOperatingMode() {
		return operatingMode;
	}


	final public int getApplicationStatus() {
		return applicationStatus;
	}


	final public int getLeakageDetectionStatus() {
		return leakageDetectionStatus;
	}


	final Date getCurrentRTC() {
		return currentRTC;
	}


	final int getQos() {
		return qos;
	}


	final int getShortLiftCounter() {
		return shortLiftCounter;
	}


	final EncoderModelInfo[] getEncoderModelInfos() {
		return encoderModelInfos;
	}


	final public EncoderUnitInfo[] getEncoderUnitInfos() {
		return encoderUnitInfos;
	}
	
}
