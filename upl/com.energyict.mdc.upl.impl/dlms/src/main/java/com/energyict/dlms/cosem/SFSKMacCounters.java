package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

/**
 * @author jme
 *
 */
public class SFSKMacCounters extends AbstractCosemObject {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.3.0.255").getLN();

	/** Attributes */
	private Array				synchronizationRegister			= null;
	private Structure			desynchronizationListing		= null;
	private Array				broadcastFramesCounter			= null;
	private Unsigned32			repetitionsCounter				= null;
	private Unsigned32			transmissionsCounter			= null;
	private Unsigned32			crcOkFramesCounter				= null;
	private Unsigned32			crcNOkFramesCounter				= null;

	/** Attribute numbers */
	private static final int	ATTRB_SYNCHRONIZATION_REGISTER	= 0x08;
	private static final int	ATTRB_DESYNCHRONIZATION_LISTING	= 0x10;
	private static final int	ATTRB_BROADCAST_FRAMES_COUNTER	= 0x18;
	private static final int	ATTRB_REPETITIONS_COUNTER		= 0x20;
	private static final int	ATTRB_TRANSMISSIONS_COUNTER		= 0x28;
	private static final int	ATTRB_CRC_OK_FRAMES_COUNTER		= 0x30;
	private static final int	ATTRB_CRC_NOK_FRAMES_COUNTER	= 0x38;

	/** Method numbers */
	private static final int	METHOD_RESET_DATA				= 0x50;

	/**
	 * @param protocolLink
	 * @param objectReference
	 */
	public SFSKMacCounters(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	public static ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_MAC_COUNTERS.getClassId();
	}

	public Array getSynchronizationRegister() {
		try {
			this.synchronizationRegister = new Array(getResponseData(ATTRB_SYNCHRONIZATION_REGISTER), 0, 0);
		} catch (IOException e) {}
		return synchronizationRegister;
	}

	public Structure getDesynchronizationListing() {
		try {
			this.desynchronizationListing = new Structure(getResponseData(ATTRB_DESYNCHRONIZATION_LISTING), 0, 0);
		} catch (IOException e) {}
		return desynchronizationListing;
	}

	public Array getBroadcastFramesCounter() {
		try {
			this.broadcastFramesCounter = new Array(getResponseData(ATTRB_BROADCAST_FRAMES_COUNTER), 0, 0);
		} catch (IOException e) {}
		return broadcastFramesCounter;
	}

	public Unsigned32 getRepetitionsCounter() {
		try {
			this.repetitionsCounter = new Unsigned32(getResponseData(ATTRB_REPETITIONS_COUNTER), 0);
		} catch (IOException e) {}
		return repetitionsCounter;
	}

	public Unsigned32 getTransmissionsCounter() {
		try {
			this.transmissionsCounter = new Unsigned32(getResponseData(ATTRB_TRANSMISSIONS_COUNTER), 0);
		} catch (IOException e) {}
		return transmissionsCounter;
	}

	public Unsigned32 getCrcOkFramesCounter() {
		try {
			this.crcOkFramesCounter = new Unsigned32(getResponseData(ATTRB_CRC_OK_FRAMES_COUNTER), 0);
		} catch (IOException e) {}
		return crcOkFramesCounter;
	}

	public Unsigned32 getCrcNOkFramesCounter() {
		try {
			this.crcNOkFramesCounter = new Unsigned32(getResponseData(ATTRB_CRC_NOK_FRAMES_COUNTER), 0);
		} catch (IOException e) {}
		return crcNOkFramesCounter;
	}

	public void invokeResetData() throws IOException {
		write(METHOD_RESET_DATA, new Unsigned16(0).getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		Array synchronizationRegister = getSynchronizationRegister();
		Structure desynchronizationListing = getDesynchronizationListing();
		Array broadcastFramesCounter = getBroadcastFramesCounter();
		Unsigned32 repetitionsCounter = getRepetitionsCounter();
		Unsigned32 transmissionsCounter = getTransmissionsCounter();
		Unsigned32 crcOkFramesCounter = getCrcOkFramesCounter();
		Unsigned32 crcNOkFramesCounter = getCrcNOkFramesCounter();

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKMacCounters").append(crlf);
		sb.append(" > synchronizationRegister = ").append(synchronizationRegister != null ? synchronizationRegister.toString() : null).append(crlf);
		sb.append(" > desynchronizationListing = ").append(desynchronizationListing != null ? desynchronizationListing.toString() : null).append(crlf);
		sb.append(" > broadcastFramesCounter = ").append(broadcastFramesCounter != null ? broadcastFramesCounter.toString() : null).append(crlf);
		sb.append(" > repetitionsCounter = ").append(repetitionsCounter != null ? repetitionsCounter.getValue() : null).append(crlf);
		sb.append(" > transmissionsCounter = ").append(transmissionsCounter != null ? transmissionsCounter.getValue() : null).append(crlf);
		sb.append(" > crcOkFramesCounter = ").append(crcOkFramesCounter != null ? crcOkFramesCounter.getValue() : null).append(crlf);
		sb.append(" > crcNOkFramesCounter = ").append(crcNOkFramesCounter != null ? crcNOkFramesCounter.getValue() : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getObisCode(), toString());
	}

}
