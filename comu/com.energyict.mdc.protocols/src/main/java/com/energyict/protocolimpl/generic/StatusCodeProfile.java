/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * StatusCodeProfile.java
 *
 * Created on 5 december 2007, 16:05
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.generic;


import com.energyict.mdc.common.interval.IntervalStateBits;

/**
 *
 * @author kvds
 */
public class StatusCodeProfile { 
    
   
    /** Creates a new instance of StatusCodeProfile */
    private StatusCodeProfile() {
    }
    
    
    static final int CRITICAL_ERROR=0x01;
    static final int CLOCK_INVALID=0x02;
    static final int DATA_NOT_VALID=0x04;
    static final int DAYLIGHT_SAVING=0x08;
    static final int BILLING_RESET=0x10;  // only used by IskraEmeko for the billing reset
    static final int CLOCK_ADJUSTED=0x20;
    static final int POWER_UP=0x40;  // only used by IskraEmeko for power return
    static final int POWER_DOWN=0x80;
    
    public static int intervalStateBits(int statusCodeProfile) {
        
            int eiCode=0;
        
        if ((statusCodeProfile & CRITICAL_ERROR) == CRITICAL_ERROR) {
			eiCode |= IntervalStateBits.DEVICE_ERROR;
		}
        if ((statusCodeProfile & CLOCK_INVALID) == CLOCK_INVALID) {
			eiCode |= IntervalStateBits.BADTIME;
		}
        if ((statusCodeProfile & DATA_NOT_VALID) == DATA_NOT_VALID) {
			eiCode |= IntervalStateBits.CORRUPTED;
		}
        if ((statusCodeProfile & DAYLIGHT_SAVING) == DAYLIGHT_SAVING) {
			eiCode |= IntervalStateBits.OTHER;
		}
        if ((statusCodeProfile & BILLING_RESET) == BILLING_RESET) {
			eiCode |= IntervalStateBits.OTHER;
		}
        if ((statusCodeProfile & CLOCK_ADJUSTED) == CLOCK_ADJUSTED) {
			eiCode |= IntervalStateBits.SHORTLONG;
		}
        if ((statusCodeProfile & POWER_UP) == POWER_UP) {
			eiCode |= IntervalStateBits.POWERUP;
		}
        if ((statusCodeProfile & POWER_DOWN) == POWER_DOWN) {
			eiCode |= IntervalStateBits.POWERDOWN;
		}
        
        return eiCode;
    }
    
}
