/**
 * UNIFLO1200Profile.java
 * 
 * Created on 15-dec-2008, 13:30:51 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile;

import java.util.Date;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;


/**
 * @author jme
 *
 */
public class UNIFLO1200Profile {

	private UNIFLO1200 uniflo1200;
	
	public UNIFLO1200Profile(UNIFLO1200 uniflo1200) {
		this.uniflo1200 = uniflo1200;
	}

	private int getLatestLogIndex() {
		
		return 0;
	}
	
	
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getProfileInterval() {
		// TODO Auto-generated method stub
		return uniflo1200.getInfoTypeProfileInterval();
	}

}
