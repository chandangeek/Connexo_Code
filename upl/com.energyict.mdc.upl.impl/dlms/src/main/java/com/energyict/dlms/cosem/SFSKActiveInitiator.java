/**
 *
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.cosem.attributeobjects.InitiatorDescriptor;
import com.energyict.dlms.cosem.attributeobjects.MacAddress;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public class SFSKActiveInitiator extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.1.0.255").getLN();

	/** Attributes */
	private InitiatorDescriptor	activeInitiator			= null;

	/** Attribute numbers */
	private static final int	ATTRB_ACTIVE_INITIATOR		= 0x08;
	private static final int	ATTRB_RESET_NEW_NOT_SYNC	= 0x10;

	public static ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_ACTIVE_INITIATOR.getClassId();
	}

	public SFSKActiveInitiator(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	public InitiatorDescriptor getActiveInitiator() {
		try {
			this.activeInitiator = new InitiatorDescriptor(getResponseData(ATTRB_ACTIVE_INITIATOR), 0, 0);
		} catch (IOException e) {}
		return activeInitiator;
	}

	public void doResetNewNotSynchronized(MacAddress mac) throws IOException {
		write(ATTRB_RESET_NEW_NOT_SYNC, mac.getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		InitiatorDescriptor activeInitiator = getActiveInitiator();

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKActiveInitiator").append(crlf);
		sb.append(" > activeInitiator = ").append(activeInitiator != null ? activeInitiator : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getObisCode(), toString());
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		return asRegisterValue();
	}

}
