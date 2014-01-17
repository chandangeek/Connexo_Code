package com.energyict.protocolimpl.coronis.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class WavenisStackConfigRFResponseTimeout extends AbstractEscapeCommand {

	WavenisStackConfigRFResponseTimeout(ProtocolStackLink protocolStackLink, int timeout) {
		super(protocolStackLink);
		this.timeout=timeout;
	}

	int timeout;

	@Override
    EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.WAVENIS_CONFIG_RF_TIMEOUT;
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
			daos.writeInt(timeout);
			return baos.toByteArray();
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getProtocolStackLink().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}


}
