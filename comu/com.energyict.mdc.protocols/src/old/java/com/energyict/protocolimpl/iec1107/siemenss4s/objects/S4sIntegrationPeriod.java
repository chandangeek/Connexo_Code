/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s.objects;


/**
 * Integration period, or interval, of the meter
 * @author gna
 *
 */
public class S4sIntegrationPeriod {
	
	private int[] periods = {300, 600, 900, 1200, 1800, 3600};	// periods in seconds
	private byte[] rawBytes;
	
	/**
	 * Creates a new instance of the integrationPeriod object
	 * @param period a hexadecimal number referring to a number in the periods array
	 */
	public S4sIntegrationPeriod(byte[] period){
		this.rawBytes = S4sObjectUtils.hexStringToByteArray(new String(period));
		if(this.rawBytes.length != 1){
			throw new IllegalArgumentException("Period is only one byte long, received " + period.length);
		} else if(this.rawBytes[0] > 5){
			throw new IllegalArgumentException("Number of period is not valid: " + this.rawBytes[0] + " - Should be between 0 and 5");
		}
	}
	
	/**
	 * @return the meters interval in SECONDS
	 */
	public int getInterval(){
		return this.periods[this.rawBytes[0]];
	}
}
