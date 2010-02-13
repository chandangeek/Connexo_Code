/**
 *
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public class SFSKIec61334LLCSetup extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.5.0.255").getLN();

	/** Attributes */
	private Unsigned8			maxFrameLength			= null;
	private Array				replyStatusList			= null;

	/** Attribute numbers */
	private static final int	ATTRB_MAX_FRAME_LENGTH	= 0x08;
	private static final int	ATTRB_REPLY_STATUS_LIST	= 0x10;


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

	public Unsigned8 getMaxFrameLength() {
		try {
			maxFrameLength = new Unsigned8(getResponseData(ATTRB_MAX_FRAME_LENGTH), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return maxFrameLength;
	}

	public Array getReplyStatusList() {
		try {
			replyStatusList = new Array(getResponseData(ATTRB_REPLY_STATUS_LIST), 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replyStatusList;
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		Unsigned8 maxFrameLength = getMaxFrameLength();
		Array replyStatusList = getReplyStatusList();

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKIec61334LLCSetup").append(crlf);
		sb.append(" > maxFrameLength = ").append(maxFrameLength != null ? maxFrameLength.getValue() : null).append(crlf);
		sb.append(" > replyStatusList = ").append(replyStatusList != null ? replyStatusList : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getObisCode(), toString());
	}

}
