/**
 *
 */
package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public class SFSKIec61334LLCSetup extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.5.0.255").getLN();

	/** Attributes */
	private Unsigned16			maxFrameLength			= null;

	/** Attribute numbers */
	private static final int	ATTRB_MAX_FRAME_LENGTH	= 0x08;
	private static final int	ATTRB_SYNC_CONFIRM_TIMEOUT		= 0x10;
	private static final int	ATTRB_TIMEOUT_NOT_ADDRESSED		= 0x18;
	private static final int	ATTRB_TIMEOUT_FRAME_NOT_OK		= 0x20;


	public static ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_IEC_61334_4_32_LLC_SETUP.getClassId();
	}

	public SFSKIec61334LLCSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}


	@Override
	public String toString() {
		final String crlf = "\r\n";

		Unsigned16 temp = null;

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKIec61334LLCSetup").append(crlf);
		sb.append(" > temp = ").append(temp != null ? temp.getValue() : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getObisCode(), toString());
	}

}
