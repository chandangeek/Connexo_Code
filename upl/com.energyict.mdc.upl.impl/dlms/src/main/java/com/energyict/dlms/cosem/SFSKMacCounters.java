package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

public class SFSKMacCounters extends AbstractCosemObject {

	public SFSKMacCounters(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_MAC_COUNTERS.getClassId();
	}

	public static ObisCode getObisCode() {
		return ObisCode.fromString("0.0.26.3.0.255");
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getObisCode());
	}

}
