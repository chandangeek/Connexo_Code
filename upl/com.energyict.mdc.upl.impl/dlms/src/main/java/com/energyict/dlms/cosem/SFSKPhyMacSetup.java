/**
 *
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.Frequencies;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class SFSKPhyMacSetup extends AbstractCosemObject {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.0.0.255").getLN();

	/** Attributes */
	private TypeEnum			initiatorElectricalPhase			= null;
	private TypeEnum			deltaElectricalPhase				= null;
	private Unsigned8			maxReceivingGain					= null;
	private Unsigned8			maxTransmittingGain					= null;
	private Unsigned8			searchInitiatorGain					= null;
	private Frequencies			frequencies							= null;
	private Unsigned16			macAddress							= null;
	private Array				macGroupAddresses					= null;
	private TypeEnum			repeater							= null;
	private BooleanObject		repeaterStatus						= null;
	private Unsigned8			minDeltaCredit						= null;
	private Unsigned16			initiatorMacAddress					= null;
	private BooleanObject		synchronizationLocked				= null;

	/** Attribute numbers */
	private static final int	ATTRB_INITIATOR_ELECTRICAL_PHASE	= 0x08;
	private static final int	ATTRB_DELTA_ELECTRICAL_PHASE		= 0x10;
	private static final int	ATTRB_MAX_RECEIVING_GAIN			= 0x18;
	private static final int	ATTRB_MAX_TRANSMITTING_GAIN			= 0x20;
	private static final int	ATTRB_SEARCH_INITIATOR_GAIN			= 0x28;
	private static final int	ATTRB_FREQUENCIES					= 0x30;
	private static final int	ATTRB_MAC_ADDRESS					= 0x38;
	private static final int	ATTRB_MAC_GROUP_ADDRESSES			= 0x40;
	private static final int	ATTRB_REPEATER						= 0x48;
	private static final int	ATTRB_REPEATER_STATUS				= 0x50;
	private static final int	ATTRB_MIN_DELTA_CREDIT				= 0x58;
	private static final int	ATTRB_INITIATOR_MAC_ADDRESS			= 0x60;
	private static final int	ATTRB_SYNCHRONIZATION_LOCKED		= 0x68;

	public static ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN);
	}

	public SFSKPhyMacSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	@Override
	protected int getClassId() {
		return DLMSClassId.S_FSK_PHY_MAC_SETUP.getClassId();
	}

	public TypeEnum getInitiatorElectricalPhase() {
		if (initiatorElectricalPhase == null) {
			try {
				this.initiatorElectricalPhase = new TypeEnum(getResponseData(ATTRB_INITIATOR_ELECTRICAL_PHASE), 0);
			} catch (IOException e) {}
		}
		return initiatorElectricalPhase;
	}

	public TypeEnum getDeltaElectricalPhase() {
		try {
			this.deltaElectricalPhase = new TypeEnum(getResponseData(ATTRB_DELTA_ELECTRICAL_PHASE), 0);
		} catch (IOException e) {}
		return deltaElectricalPhase;
	}

	public Unsigned8 getMaxReceivingGain() {
		if (maxReceivingGain == null) {
			try {
				this.maxReceivingGain = new Unsigned8(getResponseData(ATTRB_MAX_RECEIVING_GAIN), 0);
			} catch (IOException e) {}
		}
		return maxReceivingGain;
	}

	public Unsigned8 getMaxTransmittingGain() {
		if (maxTransmittingGain == null) {
			try {
				this.maxTransmittingGain = new Unsigned8(getResponseData(ATTRB_MAX_TRANSMITTING_GAIN), 0);
			} catch (IOException e) {}
		}
		return maxTransmittingGain;
	}

	public Unsigned8 getSearchInitiatorGain() {
		if (searchInitiatorGain == null) {
			try {
				this.searchInitiatorGain = new Unsigned8(getResponseData(ATTRB_SEARCH_INITIATOR_GAIN), 0);
			} catch (IOException e) {}
		}
		return searchInitiatorGain;
	}

	public Frequencies getFrequencies() {
		if (frequencies == null) {
			try {
				this.frequencies = new Frequencies(getResponseData(ATTRB_FREQUENCIES), 0, 0);
			} catch (IOException e) {}
		}
		return frequencies;
	}

	public Unsigned16 getMacAddress() {
		try {
			this.macAddress = new Unsigned16(getResponseData(ATTRB_MAC_ADDRESS), 0);
		} catch (IOException e) {}
		return macAddress;
	}

	public Array getMacGroupAddresses() {
		if (macGroupAddresses == null) {
			try {
				this.macGroupAddresses = new Array(getResponseData(ATTRB_MAC_GROUP_ADDRESSES), 0, 0);
			} catch (IOException e) {}
		}
		return macGroupAddresses;
	}

	public TypeEnum getRepeater() {
		if (repeater == null) {
			try {
				this.repeater = new TypeEnum(getResponseData(ATTRB_REPEATER), 0);
			} catch (IOException e) {}
		}
		return repeater;
	}

	public BooleanObject getRepeaterStatus() {
		try {
			this.repeaterStatus = new BooleanObject(getResponseData(ATTRB_REPEATER_STATUS), 0);
		} catch (IOException e) {}
		return repeaterStatus;
	}

	public Unsigned8 getMinDeltaCredit() {
		try {
			this.minDeltaCredit = new Unsigned8(getResponseData(ATTRB_MIN_DELTA_CREDIT), 0);
		} catch (IOException e) {}
		return minDeltaCredit;
	}

	public Unsigned16 getInitiatorMacAddress() {
		try {
			this.initiatorMacAddress = new Unsigned16(getResponseData(ATTRB_INITIATOR_MAC_ADDRESS), 0);
		} catch (IOException e) {}
		return initiatorMacAddress;
	}

	public BooleanObject getSynchronizationLocked() {
		try {
			this.synchronizationLocked = new BooleanObject(getResponseData(ATTRB_SYNCHRONIZATION_LOCKED), 0);
		} catch (IOException e) {}
		return synchronizationLocked;
	}

	public void clearCache() {
		initiatorElectricalPhase = null;
		maxReceivingGain = null;
		maxTransmittingGain = null;
		searchInitiatorGain = null;
		frequencies = null;
		macGroupAddresses = null;
		repeater = null;
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";
		StringBuffer sb = new StringBuffer();
		sb.append("SFSKPhyMacSetup").append(crlf);
		sb.append(" > initiatorElectricalPhase = ").append(getInitiatorElectricalPhase().getValue()).append(crlf);
		sb.append(" > deltaElectricalPhase = ").append(getDeltaElectricalPhase().getValue()).append(crlf);
		sb.append(" > maxReceivingGain = ").append(getMaxReceivingGain().getValue()).append(crlf);
		sb.append(" > maxTransmittingGain = ").append(getMaxTransmittingGain().getValue()).append(crlf);
		sb.append(" > searchInitiatorGain = ").append(getSearchInitiatorGain().getValue()).append(crlf);
		sb.append(" > frequencies = ").append(getFrequencies().toString().replace("\n", " ").replace("\r", "")).append(crlf);
		sb.append(" > macAddress = ").append(getMacAddress().getValue()).append(crlf);
		sb.append(" > macGroupAddresses = ").append(getMacGroupAddresses().toString().replace("\n", " ").replace("\r", "")).append(crlf);
		sb.append(" > repeater = ").append(getRepeater().getValue()).append(crlf);
		sb.append(" > repearterStatus = ").append(getRepeaterStatus().getState()).append(crlf);
		sb.append(" > minDeltaCredit = ").append(getMinDeltaCredit().getValue()).append(crlf);
		sb.append(" > initiatorMacAddress = ").append(getInitiatorMacAddress().getValue()).append(crlf);
		sb.append(" > synchronizationLocked = ").append(getSynchronizationLocked().getState()).append(crlf);
		return sb.toString();
	}

}
