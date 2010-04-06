package com.energyict.protocolimpl.eig.nexus1272;

import java.io.IOException;
import java.util.Date;

import com.energyict.protocol.ProfileData;

public interface LogReader {

	public byte[] readLog(Date lastReadDate) throws IOException;
//	public Object parseLog(byte[] byteArray) throws IOException;
	public void parseLog(byte[] byteArray, ProfileData profileData)
			throws IOException;
	
}
