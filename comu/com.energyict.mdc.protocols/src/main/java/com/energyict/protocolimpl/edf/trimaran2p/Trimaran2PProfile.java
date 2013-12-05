/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import com.energyict.mdc.protocol.device.data.ProfileData;

import java.io.IOException;
import java.util.Date;

/**
 * @author gna
 *
 */
public class Trimaran2PProfile {

	Trimaran2P trimaran;

	/**
	 *
	 */
	public Trimaran2PProfile() {
	}

	public Trimaran2PProfile(Trimaran2P trimaran2P) {
		this.trimaran = trimaran2P;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ProfileData getProfileData(Date lastReading, Date to) throws IOException {
		return trimaran.getTrimaranObjectFactory().getCourbeCharge(lastReading, to).getProfileData();
	}

}
