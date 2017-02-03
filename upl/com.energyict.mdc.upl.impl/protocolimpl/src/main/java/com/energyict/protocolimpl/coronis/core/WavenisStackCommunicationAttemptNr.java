package com.energyict.protocolimpl.coronis.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class WavenisStackCommunicationAttemptNr extends AbstractEscapeCommand {

	WavenisStackCommunicationAttemptNr(ProtocolStackLink protocolStackLink, int communicationAttemptNr) {
		super(protocolStackLink);
		this.communicationAttemptNr=communicationAttemptNr;
	}

	int communicationAttemptNr;
	
	@Override
    EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.WAVENIS_COMMUNICATION_ATTEMPT_NR;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte((byte)communicationAttemptNr);
			return baos.toByteArray();
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getProtocolStackLink().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}	
	}
	
	
}
