package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.SFSKSyncTimeoutsAttribute;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKSyncTimeouts extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.2.0.255").getLN();

	public static ObisCode getDefaultObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_MAC_SYNC_TIMEOUTS.getClassId();
	}

	public SFSKSyncTimeouts(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	/**
	 * Get the logicalname of the object. Identifies the object instance.
	 * @return
	 */
	public OctetString getLogicalName() {
		try {
			return new OctetString(getResponseData(SFSKSyncTimeoutsAttribute.LOGICAL_NAME), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned16 getSearchInitiatorTimeout() {
		try {
			return new Unsigned16(getResponseData(SFSKSyncTimeoutsAttribute.SEARCH_INITIATOR_TIMEOUT), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void setSearchInitiatorTimeout(int value) throws IOException {
		write(SFSKSyncTimeoutsAttribute.SEARCH_INITIATOR_TIMEOUT, new Unsigned16(value).getBEREncodedByteArray());
	}

	public Unsigned16 getSyncConfirmTimeout() {
		try {
			return new Unsigned16(getResponseData(SFSKSyncTimeoutsAttribute.SYNCHRONIZATION_CONFIRMATION_TIMEOUT), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void setSyncConfirmTimeout(int value) throws IOException {
		write(SFSKSyncTimeoutsAttribute.SYNCHRONIZATION_CONFIRMATION_TIMEOUT, new Unsigned16(value).getBEREncodedByteArray());
	}

	public Unsigned16 getTimeoutNotAddressed() {
		try {
			return new Unsigned16(getResponseData(SFSKSyncTimeoutsAttribute.TIME_OUT_NOT_ADDRESSED), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void setTimeoutNotAddressed(int value) throws IOException {
		write(SFSKSyncTimeoutsAttribute.TIME_OUT_NOT_ADDRESSED, new Unsigned16(value).getBEREncodedByteArray());
	}

	public Unsigned16 getTimeoutFrameNotOk() {
		try {
			return new Unsigned16(getResponseData(SFSKSyncTimeoutsAttribute.TIME_OUT_FRAME_NOT_OK), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void setTimeoutFrameNotOk(int value) throws IOException {
		write(SFSKSyncTimeoutsAttribute.TIME_OUT_FRAME_NOT_OK, new Unsigned16(value).getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		Unsigned16 searchInitiatorTimeout = getSearchInitiatorTimeout();
		Unsigned16 syncConfirmTimeout = getSyncConfirmTimeout();
		Unsigned16 timeoutNotAddressed = getTimeoutNotAddressed();
		Unsigned16 timeoutFrameNotOk = getTimeoutFrameNotOk();

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKSyncTimeouts").append(crlf);
		sb.append(" > searchInitiatorTimeout = ").append(searchInitiatorTimeout != null ? searchInitiatorTimeout.getValue() : null).append(crlf);
		sb.append(" > syncConfirmTimeout = ").append(syncConfirmTimeout != null ? syncConfirmTimeout.getValue() : null).append(crlf);
		sb.append(" > timeoutNotAddressed = ").append(timeoutNotAddressed != null ? timeoutNotAddressed.getValue() : null).append(crlf);
		sb.append(" > timeoutFrameNotOk = ").append(timeoutFrameNotOk != null ? timeoutFrameNotOk.getValue() : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getDefaultObisCode(), toString());
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		SFSKSyncTimeoutsAttribute attribute = SFSKSyncTimeoutsAttribute.findByAttributeNumber(attributeNumber);
		if (attribute != null) {
			switch (attribute) {
				case LOGICAL_NAME:
					OctetString ln = getLogicalName();
					return new RegisterValue(getDefaultObisCode(), ln != null ? ObisCode.fromByteArray(ln.getOctetStr()).toString() : "null");
				case SEARCH_INITIATOR_TIMEOUT:
					Unsigned16 search = getSearchInitiatorTimeout();
					return new RegisterValue(getDefaultObisCode(), search != null ? String.valueOf(search.getValue()) : "null");
				case SYNCHRONIZATION_CONFIRMATION_TIMEOUT:
					Unsigned16 confirmTimeout = getSyncConfirmTimeout();
					return new RegisterValue(getDefaultObisCode(), confirmTimeout != null ? String.valueOf(confirmTimeout.getValue()) : "null");
				case TIME_OUT_NOT_ADDRESSED:
					Unsigned16 notAddressed = getTimeoutNotAddressed();
					return new RegisterValue(getDefaultObisCode(), notAddressed != null ? String.valueOf(notAddressed.getValue()) : "null");
				case TIME_OUT_FRAME_NOT_OK:
					Unsigned16 frameNOK = getTimeoutFrameNotOk();
					return new RegisterValue(getDefaultObisCode(), frameNOK != null ? String.valueOf(frameNOK.getValue()) : "null");
			}
		}
		return null;
	}

}
