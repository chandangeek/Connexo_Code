/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributeobjects.DesynchronizationListing;
import com.energyict.dlms.cosem.attributeobjects.MacUnsigned32Couples;
import com.energyict.dlms.cosem.attributes.SFSKMacCountersAttribute;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKMacCounters extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.3.0.255").getLN();

	/** Method numbers */
	private static final int	METHOD_RESET_DATA				= 0x50;

	/**
	 * @param protocolLink
	 * @param objectReference
	 */
	public SFSKMacCounters(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	public static ObisCode getDefaultObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_MAC_COUNTERS.getClassId();
	}

	/**
	 * Get the logicalname of the object. Identifies the object instance.
	 * @return
	 */
	public OctetString getLogicalName() {
		try {
			return new OctetString(getResponseData(SFSKMacCountersAttribute.LOGICAL_NAME), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public MacUnsigned32Couples getSynchronizationRegister() {
		try {
			return new MacUnsigned32Couples(getResponseData(SFSKMacCountersAttribute.SYNCHRONIZATION_REGISTER), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

	public DesynchronizationListing getDesynchronizationListing() {
		try {
			return new DesynchronizationListing(getResponseData(SFSKMacCountersAttribute.DESYNCHRONIZATION_LISTING), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

	public MacUnsigned32Couples getBroadcastFramesCounter() {
		try {
			return new MacUnsigned32Couples(getResponseData(SFSKMacCountersAttribute.BROADCAST_FRAMES_COUNTER), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned32 getRepetitionsCounter() {
		try {
			return new Unsigned32(getResponseData(SFSKMacCountersAttribute.REPETITIONS_COUNTER), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned32 getTransmissionsCounter() {
		try {
			return new Unsigned32(getResponseData(SFSKMacCountersAttribute.TRANSMISSIONS_COUNTER), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned32 getCrcOkFramesCounter() {
		try {
			return new Unsigned32(getResponseData(SFSKMacCountersAttribute.CRC_OK_FRAMES_COUNTER), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned32 getCrcNOkFramesCounter() {
		try {
			return new Unsigned32(getResponseData(SFSKMacCountersAttribute.CRC_NOK_FRAMES_COUNTER), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void invokeResetData() throws IOException {
		write(METHOD_RESET_DATA, new Unsigned16(0).getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		MacUnsigned32Couples synchronizationRegister = getSynchronizationRegister();
		DesynchronizationListing desynchronizationListing = getDesynchronizationListing();
		MacUnsigned32Couples broadcastFramesCounter = getBroadcastFramesCounter();
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
		return new RegisterValue(getDefaultObisCode(), toString());
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		SFSKMacCountersAttribute attribute = SFSKMacCountersAttribute.findByAttributeNumber(attributeNumber);
		if (attribute != null) {
			switch (attribute) {
				case LOGICAL_NAME:
					OctetString ln = getLogicalName();
					return new RegisterValue(getDefaultObisCode(), ln != null ? ObisCode.fromByteArray(ln.getOctetStr()).toString() : "null");
				case SYNCHRONIZATION_REGISTER:
					MacUnsigned32Couples sync = getSynchronizationRegister();
					return new RegisterValue(getDefaultObisCode(), sync != null ? sync.toString() : "null");
				case DESYNCHRONIZATION_LISTING:
					DesynchronizationListing desync = getDesynchronizationListing();
					return new RegisterValue(getDefaultObisCode(), desync != null ? desync.toString() : "null");
				case BROADCAST_FRAMES_COUNTER:
					MacUnsigned32Couples bc = getBroadcastFramesCounter();
					return new RegisterValue(getDefaultObisCode(), bc != null ? bc.toString() : "null");
				case REPETITIONS_COUNTER:
					Unsigned32 rep = getRepetitionsCounter();
					return new RegisterValue(getDefaultObisCode(), rep != null ? String.valueOf(rep.getValue()) : "null");
				case TRANSMISSIONS_COUNTER:
					Unsigned32 tx = getTransmissionsCounter();
					return new RegisterValue(getDefaultObisCode(), tx != null ? String.valueOf(tx.getValue()) : "null");
				case CRC_OK_FRAMES_COUNTER:
					Unsigned32 crcOk = getCrcOkFramesCounter();
					return new RegisterValue(getDefaultObisCode(), crcOk != null ? String.valueOf(crcOk.getValue()) : "null");
				case CRC_NOK_FRAMES_COUNTER:
					Unsigned32 crcNotOk = getCrcNOkFramesCounter();
					return new RegisterValue(getDefaultObisCode(), crcNotOk != null ? String.valueOf(crcNotOk.getValue()) : "null");
			}
		}
		return null;
	}

}
