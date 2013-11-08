package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

public class EncoderGenericHeader extends GenericHeader {

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
	
	
	
	EncoderGenericHeader(DataInputStream dais, Logger logger, TimeZone timeZone) throws IOException {
		super(dais, logger, timeZone);
		
	}

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
	
	final public int getApplicationStatus() {
		return applicationStatus;
	}

	final public int getLeakageDetectionStatus() {
		return leakageDetectionStatus;
	}

	@Override
	void parse(DataInputStream dais) throws IOException {
		applicationStatus = WaveflowProtocolUtils.toInt(dais.readByte());
		leakageDetectionStatus = WaveflowProtocolUtils.toInt(dais.readByte());		
	}
	
}
