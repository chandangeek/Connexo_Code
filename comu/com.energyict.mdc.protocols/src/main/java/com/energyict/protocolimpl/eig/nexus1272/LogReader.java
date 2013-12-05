package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.mdc.protocol.device.data.ProfileData;

import java.io.IOException;
import java.util.Date;

public interface LogReader {

	public byte[] readLog(Date lastReadDate) throws IOException;
	public void parseLog(byte[] byteArray, ProfileData profileData, Date from, int intervalInSeconds)
			throws IOException;

}
