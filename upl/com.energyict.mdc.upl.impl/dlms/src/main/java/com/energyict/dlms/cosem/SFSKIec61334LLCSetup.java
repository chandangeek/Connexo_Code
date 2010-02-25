package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.ReplyStatusList;
import com.energyict.dlms.cosem.attributes.SFSKIec61334LLCSetupAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public class SFSKIec61334LLCSetup extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.5.0.255").getLN();

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

	/**
	 * Get the logicalname of the object. Identifies the object instance.
	 * @return
	 */
	public OctetString getLogicalName() {
		try {
			return new OctetString(getResponseData(SFSKIec61334LLCSetupAttribute.LOGICAL_NAME));
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned8 getMaxFrameLength() {
		try {
			return new Unsigned8(getResponseData(SFSKIec61334LLCSetupAttribute.MAX_FRAME_LENGTH), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public ReplyStatusList getReplyStatusList() {
		try {
			return new ReplyStatusList(getResponseData(SFSKIec61334LLCSetupAttribute.REPLY_STATUS_LIST), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		Unsigned8 maxFrameLen = getMaxFrameLength();
		Array replyStatList = getReplyStatusList();

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKIec61334LLCSetup").append(crlf);
		sb.append(" > maxFrameLength = ").append(maxFrameLen != null ? maxFrameLen.getValue() : null).append(crlf);
		sb.append(" > replyStatusList = ").append(replyStatList != null ? replyStatList : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getObisCode(), toString());
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		SFSKIec61334LLCSetupAttribute attribute = SFSKIec61334LLCSetupAttribute.findByAttributeNumber(attributeNumber);
		if (attribute != null) {
			switch (attribute) {
				case LOGICAL_NAME:
					OctetString ln = getLogicalName();
					return new RegisterValue(getObisCode(), ln != null ? ObisCode.fromByteArray(ln.getContentBytes()).toString() : "null");
				case MAX_FRAME_LENGTH:
					Unsigned8 maxFrameLen = getMaxFrameLength();
					return new RegisterValue(getObisCode(), maxFrameLen != null ? String.valueOf(maxFrameLen.getValue()) : "null");
				case REPLY_STATUS_LIST:
					ReplyStatusList reply = getReplyStatusList();
					return new RegisterValue(getObisCode(), reply != null ? reply.toString() : "null");
			}
		}
		return null;
	}

}
