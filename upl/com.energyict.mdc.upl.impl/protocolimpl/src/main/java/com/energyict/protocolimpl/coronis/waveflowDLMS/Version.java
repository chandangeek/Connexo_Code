package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;


public class Version extends AbstractParameter {

	Version(ProtocolLink protocolLink) {
		super(protocolLink);
	}
	
	private int version;

	/**
	 * The "Version" byte is used to inform user application of features included in current Waveflow AC 150mW
	 * DLMS. This specification describes features embedded in "Version 0x02".
	 * This parameter is accessible through parameter number "0x56" and is also systematically returned in
	 * generic header present in almost each response frame of the Waveflow AC 150mW DLMS.
	 */
	final int getVersion() {
		return version;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.Version;
	}

	@Override
	void parse(byte[] data) throws IOException {
		version = ProtocolUtils.getInt(data,0,2);
	}

	@Override
	byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}

}
