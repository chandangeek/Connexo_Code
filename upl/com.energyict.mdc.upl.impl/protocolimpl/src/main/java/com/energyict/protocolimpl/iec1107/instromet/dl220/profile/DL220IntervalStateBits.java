/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.protocol.IntervalStateBits;

/**
 * @author gna
 * @since 28-mrt-2010
 *
 */
public final class DL220IntervalStateBits {

	/** Private instance */
	private DL220IntervalStateBits(){
	}
	
	private static final int RESTART = 1;
//	private static final int RESTART = 2;
	private static final int CLOCK_STOPPED = 3;
	private static final int POWER_SUPPLY_FAULT = 4;
//	private static final int RESTART = 5;
//	private static final int RESTART = 6;
	private static final int SOFTWARE_ERROR = 7;
	private static final int SETTING_ERROR = 8;
	private static final int BATTERY_LOW = 9;
//	private static final int RESTART = 10;
	private static final int CLOCK_NOT_ADJUSTED = 11;
//	private static final int RESTART = 12;
	private static final int DATA_TRANSFER_RUNNING = 13;
	private static final int CLOCK_SYNC = 14;
//	private static final int BATTERY_MODE = 15;
//	private static final int TIME_IN_SUMMERTIME = 16;
	
	/**
	 * Create the EICode for the given statuscode
	 * 
	 * @param status
	 * 			- the statusCode(s) from the intervalrecord from the device. The status can contain muliplte values separated by a semicolon.
	 * 
	 * @return a valide EICode
	 */
    public static int intervalStateBits(String status) {

        int eiCode=0;
        String[] codes = status.split(";");
        for(String code : codes){
        	if (RESTART == Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.OTHER;
        	}
        	if (CLOCK_STOPPED == Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.BADTIME;
        	}
        	if (POWER_SUPPLY_FAULT== Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.OTHER;
        	}
        	if (SOFTWARE_ERROR== Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.DEVICE_ERROR;
        	}
        	if (SETTING_ERROR == Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.CONFIGURATIONCHANGE;
        		eiCode |= IntervalStateBits.DEVICE_ERROR;
        	}
        	if (BATTERY_LOW == Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.BATTERY_LOW;
        	}
        	if (CLOCK_NOT_ADJUSTED == Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.SHORTLONG;
        		eiCode |= IntervalStateBits.DEVICE_ERROR;
        	}
        	if (DATA_TRANSFER_RUNNING == Integer.valueOf(code)) {
        		eiCode |= IntervalStateBits.OTHER;
        	}
        	if (CLOCK_SYNC == Integer.valueOf(code)) {	// it seems like this one is not always set ...
        		eiCode |= IntervalStateBits.SHORTLONG;
        	}
//        	if(BATTERY_MODE == Integer.valueOf(code)) {
//        		// just leave it like this, it's almost always in this mode ...
//        	}
//        	if(TIME_IN_SUMMERTIME == Integer.valueOf(code)) {
//        		// Just leave it like this, it's have of the year in summertime, just change it if 
//        		// the customer wants it.
//        	}
        	
        }
        return eiCode;
    }
}
