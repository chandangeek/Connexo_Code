/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Logger;

public class MBusGenericHeader extends GenericHeader {
	
	/*
	"Application Status" parameter give at any time Waveflow 100mW RS232/MBUS fault, or consumptionrate, status.
	Each Waveflow 100mW RS232/MBUS internal feature that can be activated or deactivated through its
	corresponding bit in "Operating Mode" has an associated status bit in "Application status" parameter.
	User has to reset each bit by writing the "Application Status" parameter once the default has been handled.
	If a fault detection is not handled properly the corresponding bit in "Application Status" parameter will be
	set once again.
	 
	bit12 Meter reading error detection on Port B
	bit11 Meter reading error detection on Port A
	bit10 Meter communication error detection	on Port B
	bit9 Meter communication	error detection	on Port A
	bit8 Low Battery	Warning	
	
	bit3 Meter internal alarm on port B: Manipulation at hydraulic sensor
	bit2 Meter internal alarm on port B: Hydraulic sensor out of order
	bit1 Meter internal alarm on port A: Manipulation at hydraulic sensor
	bit0 Meter internal alarm on port A: Hydraulic sensor out of order	
	*/
	
	int applicationStatus; // 2 byte
	
	final public int getApplicationStatus() {
		return applicationStatus;
	}

	MBusGenericHeader(DataInputStream dais, Logger logger, TimeZone timeZone)
			throws IOException {
		super(dais, logger, timeZone);
		
	}

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MBusGenericHeader:\n");
        strBuff.append("   operatingMode="+WaveflowProtocolUtils.toHexString(getOperatingMode())+"\n");
        strBuff.append("   applicationStatus="+WaveflowProtocolUtils.toHexString(getApplicationStatus())+"\n");
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
	
	
	@Override
	void parse(DataInputStream dais) throws IOException {
		applicationStatus = WaveflowProtocolUtils.toInt(dais.readShort());
	}

}
