/**
 *
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public class SFSKSyncTimeouts extends AbstractCosemObject {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.2.0.255").getLN();

	/** Attributes */                                           
	private Unsigned16			searchInitiatorTimeout			= null;
	private Unsigned16			syncConfirmTimeout				= null;
	private Unsigned16			timeoutNotAddressed				= null;
	private Unsigned16			timeoutFrameNotOk				= null;

	/** Attribute numbers */
	private static final int	ATTRB_SEARCH_INITIATOR_TIMEOUT	= 0x08;
	private static final int	ATTRB_SYNC_CONFIRM_TIMEOUT		= 0x10;
	private static final int	ATTRB_TIMEOUT_NOT_ADDRESSED		= 0x18;
	private static final int	ATTRB_TIMEOUT_FRAME_NOT_OK		= 0x20;


	public static ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_MAC_SYNC_TIMEOUTS.getClassId();
	}

	public SFSKSyncTimeouts(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	public Unsigned16 getSearchInitiatorTimeout() {
		try {
			this.searchInitiatorTimeout = new Unsigned16(getResponseData(ATTRB_SEARCH_INITIATOR_TIMEOUT), 0);
		} catch (IOException e) {}
		return searchInitiatorTimeout;
	}
	
	public Unsigned16 getSyncConfirmTimeout() {
		try {
			this.syncConfirmTimeout = new Unsigned16(getResponseData(ATTRB_SYNC_CONFIRM_TIMEOUT), 0);
		} catch (IOException e) {}
		return syncConfirmTimeout;
	}
	
	public Unsigned16 getTimeoutNotAddressed() {
		try {
			this.timeoutNotAddressed = new Unsigned16(getResponseData(ATTRB_TIMEOUT_NOT_ADDRESSED), 0);
		} catch (IOException e) {}
		return timeoutNotAddressed;
	}
	
	public Unsigned16 getTimeoutFrameNotOk() {
		try {
			this.timeoutFrameNotOk = new Unsigned16(getResponseData(ATTRB_TIMEOUT_FRAME_NOT_OK), 0);
		} catch (IOException e) {}
		return timeoutFrameNotOk;
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
		return new RegisterValue(getObisCode(), toString());
	}
	
}
