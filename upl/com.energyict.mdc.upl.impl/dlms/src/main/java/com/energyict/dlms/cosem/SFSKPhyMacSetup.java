/**
 *
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.Frequencies;
import com.energyict.dlms.cosem.attributeobjects.MacAddress;
import com.energyict.dlms.cosem.attributeobjects.MacAddressList;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

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
	private MacAddress			macAddress							= null;
	private MacAddressList		macGroupAddresses					= null;
	private TypeEnum			repeater							= null;
	private BooleanObject		repeaterStatus						= null;
	private Unsigned8			minDeltaCredit						= null;
	private MacAddress			initiatorMacAddress					= null;
	private BooleanObject		synchronizationLocked				= null;

	/**
	 * This attribute is not described in the DLMS blue book 9, but it's
	 * implemented in the AM500 module. It's used to read/write the active PLC
	 * channel
	 */
	private Unsigned8			activeChannel						= null;

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

	/**
	 * This attribute is not described in the DLMS blue book 9, but it's
	 * implemented in the AM500 module. It's used to read/write the active PLC
	 * channel
	 */
	private static final int	ATTRB_ACTIVE_CHANNEL				= 0x78;

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


	/**
	 * Holds the MIB variable initiator-electrical-phase (variable 18) specified in IEC 61334-4-512 5.8.
	 * It is written by the client system to indicate the phase to which it is connected.
	 * enum: 	(0) Not defined (default),
	 * 			(1) Phase 1,
	 * 			(2) Phase 2,
	 * 			(3) Phase 3.
	 *
	 * @return {@link TypeEnum} with the current value
	 */
	public TypeEnum getInitiatorElectricalPhase() {
		if (initiatorElectricalPhase == null) {
			try {
				this.initiatorElectricalPhase = new TypeEnum(getResponseData(ATTRB_INITIATOR_ELECTRICAL_PHASE), 0);
			} catch (IOException e) {}
		}
		return initiatorElectricalPhase;
	}

	/**
	 * Holds the MIB variable delta-electrical-phase (variable 1) specified in
	 * IEC 61334-4-512 5.2 and IEC 61334-5-1 3.5.5.3. It indicates the phase difference
	 * between the client's connecting phase and the server's connecting phase.
	 *
	 * The following values are predefined:
	 * enum:	(0) Not defined: the server is temporarily not able to
	 * 				determine the phase difference,
	 * 			(1) The server system is connected to the same phase as
	 * 				the client system.
	 * 				The phase difference between the server's connecting
	 * 				phase and the client's connecting phase is equal to:
	 * 			(2) 60 degrees,
	 * 			(3) 120 degrees,
	 * 			(4) 180 degrees,
	 * 			(5) -120 degrees,
	 * 			(6) -60 degrees.
	 *
	 * @return
	 */
	public TypeEnum getDeltaElectricalPhase() {
		try {
			this.deltaElectricalPhase = new TypeEnum(getResponseData(ATTRB_DELTA_ELECTRICAL_PHASE), 0);
		} catch (IOException e) {}
		return deltaElectricalPhase;
	}

	/**
	 * Holds the MIB variable max-receiving-gain (variable 2) specified in IEC
	 * 61334-4-512 5.2 and in IEC 61334-5-1 3.5.5.3. Corresponds to the maximum
	 * allowed gain bound to be used by the server system in the receiving mode.
	 * The default unit is dB. NOTE 1 In IEC 61334-4-512, no units is specified.
	 * NOTE 2 The possible values of the gain may depend on the hardware.
	 * Therefore, after writing a value to this attribute, the value should be
	 * read back to know the actual value.
	 *
	 * @return
	 */
	public Unsigned8 getMaxReceivingGain() {
		if (maxReceivingGain == null) {
			try {
				this.maxReceivingGain = new Unsigned8(getResponseData(ATTRB_MAX_RECEIVING_GAIN), 0);
			} catch (IOException e) {}
		}
		return maxReceivingGain;
	}

	/**
	 * Holds the value of the max-transmitting-gain. Corresponds to the maximum
	 * attenuation bound to be used by the server system in the transmitting
	 * mode. The default unit is dB. NOTE The possible values of the gain may
	 * depend on the hardware. Therefore, after writing a value to this
	 * attribute, the value should be read back to know the actual value.
	 *
	 * @return
	 */
	public Unsigned8 getMaxTransmittingGain() {
		if (maxTransmittingGain == null) {
			try {
				this.maxTransmittingGain = new Unsigned8(getResponseData(ATTRB_MAX_TRANSMITTING_GAIN), 0);
			} catch (IOException e) {}
		}
		return maxTransmittingGain;
	}

	/**
	 * This attribute is used in the intelligent search initiator process. If
	 * the value of the max_receiving_gain is below the value of this attribute,
	 * a fast synchronization process is possible.
	 *
	 * @return
	 */
	public Unsigned8 getSearchInitiatorGain() {
		if (searchInitiatorGain == null) {
			try {
				this.searchInitiatorGain = new Unsigned8(getResponseData(ATTRB_SEARCH_INITIATOR_GAIN), 0);
			} catch (IOException e) {}
		}
		return searchInitiatorGain;
	}

	/**
	 * Contains frequencies required for S-FSK modulation.
	 * The default unit is Hz.
	 *
	 * @return
	 */
	public Frequencies getFrequencies() {
		if (frequencies == null) {
			try {
				this.frequencies = new Frequencies(getResponseData(ATTRB_FREQUENCIES), 0, 0);
			} catch (IOException e) {}
		}
		return frequencies;
	}

	/**
	 * Holds the MIB variable mac-address (variable 3) specified in IEC 61334-4-512 5.3 and in IEC 61334-5-1 4.3.7.6.
	 * NOTE MAC addresses are expressed on 12 bits.
	 *
	 * Contains the value of the address of the physical attachment (MAC address)
	 * associated to the local system. In the unconfigured state, the MAC address is “NEW-address”.
	 * This attribute is locally written by the CIASE when the system is registered
	 * (with a Register service). The value is used in each outgoing or incoming
	 * frame. The default value is "NEW-address".
	 *
	 * This attribute is set to NEW:
	 * - by the MAC sub-layer, once the time-out-not-addressed delay is exceeded;
	 * - when a client system “resets” the server system.
	 *
	 * When this attribute is set to NEW:
	 * - the system loses its synchronization (function of the MAC-sublayer);
	 * - the mac_group_address attribute is reset (array of 0 elements);
	 * - the system automatically releases all AAs which can be released.
	 *
	 * @return
	 */
	public MacAddress getMacAddress() {
		try {
			this.macAddress = new MacAddress(getResponseData(ATTRB_MAC_ADDRESS), 0);
		} catch (IOException e) {}
		return macAddress;
	}

	public MacAddressList getMacGroupAddresses() {
		if (macGroupAddresses == null) {
			try {
				this.macGroupAddresses = new MacAddressList(getResponseData(ATTRB_MAC_GROUP_ADDRESSES), 0, 0);
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

	public MacAddress getInitiatorMacAddress() {
		try {
			this.initiatorMacAddress = new MacAddress(getResponseData(ATTRB_INITIATOR_MAC_ADDRESS), 0);
		} catch (IOException e) {}
		return initiatorMacAddress;
	}

	public BooleanObject getSynchronizationLocked() {
		try {
			this.synchronizationLocked = new BooleanObject(getResponseData(ATTRB_SYNCHRONIZATION_LOCKED), 0);
		} catch (IOException e) {}
		return synchronizationLocked;
	}

	public Unsigned8 getActiveChannel() {
		try {
			this.activeChannel = new Unsigned8(getResponseData(ATTRB_ACTIVE_CHANNEL), 0);
		} catch (IOException e) {}
		return activeChannel;
	}

	public void setActiveChannel(Unsigned8 activeChannel) throws IOException {
			write(ATTRB_ACTIVE_CHANNEL, activeChannel.getBEREncodedByteArray());
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

		TypeEnum initiatorElectricalPhase = getInitiatorElectricalPhase();
		TypeEnum deltaElectricalPhase = getDeltaElectricalPhase();
		Unsigned8 maxReceivingGain = getMaxReceivingGain();
		Unsigned8 maxTransmittingGain = getMaxTransmittingGain();
		Unsigned8 searchInitiatorGain = getSearchInitiatorGain();
		Frequencies frequencies = getFrequencies();
		MacAddress macAddress = getMacAddress();
		MacAddressList macGroupAddresses = getMacGroupAddresses();
		TypeEnum repeater = getRepeater();
		BooleanObject repeaterStatus = getRepeaterStatus();
		Unsigned8 minDeltaCredit = getMinDeltaCredit();
		MacAddress initiatorMacAddress = getInitiatorMacAddress();
		BooleanObject synchronizationLocked = getSynchronizationLocked();
		Unsigned8 activeChannel = getActiveChannel();

		StringBuffer sb = new StringBuffer();
		sb.append("SFSKPhyMacSetup").append(crlf);
		sb.append(" > initiatorElectricalPhase = ").append(initiatorElectricalPhase != null ? initiatorElectricalPhase.getValue() : null).append(crlf);
		sb.append(" > deltaElectricalPhase = ").append(deltaElectricalPhase != null ? deltaElectricalPhase.getValue() : null).append(crlf);
		sb.append(" > maxReceivingGain = ").append(maxReceivingGain != null ? maxReceivingGain.getValue() : null).append(crlf);
		sb.append(" > maxTransmittingGain = ").append(maxTransmittingGain != null ? maxTransmittingGain.getValue() : null).append(crlf);
		sb.append(" > searchInitiatorGain = ").append(searchInitiatorGain != null ? searchInitiatorGain.getValue() : null).append(crlf);
		sb.append(" > frequencies = ").append(frequencies != null ? frequencies.toString().replace("\n", " ").replace("\r", "") : null).append(crlf);
		sb.append(" > macAddress = ").append(macAddress != null ? macAddress : null).append(crlf);
		sb.append(" > macGroupAddresses = ").append(macGroupAddresses != null ? macGroupAddresses.toString().replace("\n", " ").replace("\r", "") : null).append(crlf);
		sb.append(" > repeater = ").append(repeater != null ? repeater.getValue() : null).append(crlf);
		sb.append(" > repeaterStatus = ").append(repeaterStatus != null ? repeaterStatus.getState() : null).append(crlf);
		sb.append(" > minDeltaCredit = ").append(minDeltaCredit != null ? minDeltaCredit.getValue() : null).append(crlf);
		sb.append(" > initiatorMacAddress = ").append(initiatorMacAddress != null ? initiatorMacAddress : null).append(crlf);
		sb.append(" > synchronizationLocked = ").append(synchronizationLocked != null ? synchronizationLocked.getState() : null).append(crlf);
		sb.append(" > activeChannel = ").append(activeChannel != null ? activeChannel.getValue() : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
//		StringBuffer sb = new StringBuffer();
//		sb.append("activeChannel=").append(getActiveChannel() != null ? getActiveChannel().getValue() : null).append(", ");
//		sb.append(getFrequencies() != null ? getFrequencies().toString() : null);
		return new RegisterValue(getObisCode(), toString());
	}

}
