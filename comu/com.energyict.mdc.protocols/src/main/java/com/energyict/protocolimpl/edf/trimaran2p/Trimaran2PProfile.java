/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import java.io.IOException;
import java.util.Date;

/**
 * @author gna
 *
 */
public class Trimaran2PProfile {

	Trimaran2P trimaran;

	public Trimaran2PProfile(Trimaran2P trimaran2P) {
		this.trimaran = trimaran2P;
	}

	public ProfileData getProfileData(Date lastReading, Date to) throws IOException {
		return trimaran.getTrimaranObjectFactory().getCourbeCharge(lastReading, to).getProfileData();
	}

}