/**
 * UNIFLO1200Profile.java
 * 
 * Created on 15-dec-2008, 13:30:51 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile;

import java.io.IOException;
import java.util.Date;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;


/**
 * @author jme
 *
 */
public class UNIFLO1200Profile {

	private UNIFLO1200 uniflo1200;
	private UNIFLO1200ProfileInfo profileInfo;
	
	public UNIFLO1200Profile(UNIFLO1200 uniflo1200) throws IOException {
		this.uniflo1200 = uniflo1200;
		init();
	}

	private void init() throws IOException {
		this.profileInfo = new UNIFLO1200ProfileInfo(this);
		
	}

	public UNIFLO1200 getUniflo1200() {
		return uniflo1200;
	}

	protected int getLoadProfileNumber() {
		return getUniflo1200().getLoadProfileNumber();
	}
 
	public UNIFLO1200ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getProfileInterval() {
		return getProfileInfo().getProfileInterval();
	}

	public int getNumberOfChannels() {
		// TODO Auto-generated method stub
		return getProfileInfo().getNumberOfChannels();
	}

}
