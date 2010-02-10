/**
 * 
 */
package com.energyict.protocolimpl.dlms.as220.plc;

import java.util.Date;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.as220.AS220;

/**
 * @author jme
 *
 */
public class PLC {

	private final AS220 as220;
	
	/**
	 * @param as220
	 */
	public PLC(AS220 as220) {
		this.as220 = as220;
	}
	
	/**
	 * @return
	 */
	public AS220 getAs220() {
		return as220;
	}
	
	public ProfileData getStatistics(Date from, Date to) {
		return null;
	}
	
	
}
