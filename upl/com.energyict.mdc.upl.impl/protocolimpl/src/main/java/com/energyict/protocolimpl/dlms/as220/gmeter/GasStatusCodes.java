package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.protocol.IntervalStateBits;

/**
 * OverView of the 
 * 
 * @author gna
 * @since 17-feb-2010
 *
 */
public class GasStatusCodes {
	
    /** Creates a new instance of StatusCodeProfile */
    private GasStatusCodes() {
    }
    
    
    static final int APPLICATION_ERROR1=0x01;
    static final int APPLICATION_ERROR2=0x02;
    static final int BATTERY_LOW=0x04;
    static final int PERMANENT_ERROR=0x08;
    static final int TEMPORARY_ERROR=0x10;  
    static final int CLOCK_ADJUSTED=0x20;	// more then 60 seconds
    static final int FRAUD_ATTEMPT=0x40; 
    static final int VALVE_ALARM=0x80;
    
    public static int intervalStateBits(int statusCodeProfile) {
        
        int eiCode=0;
        
        int firstByte = (statusCodeProfile >> 8)&0x00FF;
        int secondByte = (statusCodeProfile >> 0)&0x00FF;
        
        if ((firstByte & APPLICATION_ERROR1) == APPLICATION_ERROR1) {
			eiCode |= IntervalStateBits.DEVICE_ERROR;
		}
        if ((firstByte & APPLICATION_ERROR2) == APPLICATION_ERROR2) {
			eiCode |= IntervalStateBits.DEVICE_ERROR;
		}
        if ((firstByte & BATTERY_LOW) == BATTERY_LOW) {
			eiCode |= IntervalStateBits.BATTERY_LOW;
		}
        if ((firstByte & PERMANENT_ERROR) == PERMANENT_ERROR) {
			eiCode |= IntervalStateBits.DEVICE_ERROR;
		}
        if ((firstByte & TEMPORARY_ERROR) == TEMPORARY_ERROR) {
			eiCode |= IntervalStateBits.DEVICE_ERROR;
		}
        if ((firstByte & CLOCK_ADJUSTED) == CLOCK_ADJUSTED) {
			eiCode |= IntervalStateBits.SHORTLONG;
		}
        if ((firstByte & FRAUD_ATTEMPT) == FRAUD_ATTEMPT) {
			eiCode |= IntervalStateBits.OTHER;
		}
        if ((firstByte & VALVE_ALARM) == VALVE_ALARM) {
			eiCode |= IntervalStateBits.OTHER;
		}
        
        return eiCode;
    }
}
