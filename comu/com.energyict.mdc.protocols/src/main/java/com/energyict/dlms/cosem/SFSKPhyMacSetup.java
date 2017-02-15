/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributeobjects.DeltaElectricalPhase;
import com.energyict.dlms.cosem.attributeobjects.ElectricalPhase;
import com.energyict.dlms.cosem.attributeobjects.Frequencies;
import com.energyict.dlms.cosem.attributeobjects.FrequencyGroup;
import com.energyict.dlms.cosem.attributeobjects.MacAddress;
import com.energyict.dlms.cosem.attributeobjects.MacAddressList;
import com.energyict.dlms.cosem.attributeobjects.Repeater;
import com.energyict.dlms.cosem.attributes.SFSKPhyMacSetupAttribute;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class SFSKPhyMacSetup extends AbstractCosemObject implements RegisterReadable {

	private static final byte[]	LN	= ObisCode.fromString("0.0.26.0.0.255").getLN();

	public static ObisCode getDefaultObisCode() {
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
	 * Get the logicalname of the object. Identifies the object instance.
	 * @return
	 */
	public OctetString getLogicalName() {
		try {
			return new OctetString(getResponseData(SFSKPhyMacSetupAttribute.LOGICAL_NAME), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Holds the MIB variable initiator-electrical-phase (variable 18) specified in IEC 61334-4-512 5.8.
	 * It is written by the client system to indicate the phase to which it is connected.
	 * enum: 	(0) Not defined (default),
	 * 			(1) Phase 1,
	 * 			(2) Phase 2,
	 * 			(3) Phase 3.
	 *
	 * @return {@link com.energyict.dlms.axrdencoding.TypeEnum} with the current value
	 */
	public ElectricalPhase getInitiatorElectricalPhase() {
		try {
			return new ElectricalPhase(getResponseData(SFSKPhyMacSetupAttribute.INITIATOR_ELECTRICAL_PHASE), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Holds the MIB variable initiator-electrical-phase (variable 18) specified in IEC 61334-4-512 5.8.
	 * It is written by the client system to indicate the phase to which it is connected.
	 * enum: 	(0) Not defined (default),
	 * 			(1) Phase 1,
	 * 			(2) Phase 2,
	 * 			(3) Phase 3.
	 *
	 * @param electricalPhase
	 * @throws java.io.IOException
	 */
	public void setInitiatorElectricalPhase(ElectricalPhase electricalPhase) throws IOException {
		write(SFSKPhyMacSetupAttribute.INITIATOR_ELECTRICAL_PHASE, electricalPhase.getBEREncodedByteArray());
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
	public DeltaElectricalPhase getDeltaElectricalPhase() {
		try {
			return new DeltaElectricalPhase(getResponseData(SFSKPhyMacSetupAttribute.DELTA_ELECTRICAL_PHASE), 0);
		} catch (IOException e) {
			return null;
		}
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
		try {
			return new Unsigned8(getResponseData(SFSKPhyMacSetupAttribute.MAX_RECEIVING_GAIN), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @param maxRxGain
	 * @throws java.io.IOException
	 */
	public void setMaxReceivingGain(int maxRxGain) throws IOException {
		write(SFSKPhyMacSetupAttribute.MAX_RECEIVING_GAIN, new Unsigned8(maxRxGain).getBEREncodedByteArray());
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
		try {
			return new Unsigned8(getResponseData(SFSKPhyMacSetupAttribute.MAX_TRANSMITTING_GAIN), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @param maxTxGain
	 * @throws java.io.IOException
	 */
	public void setMaxTransmittingGain(int maxTxGain) throws IOException {
		write(SFSKPhyMacSetupAttribute.MAX_TRANSMITTING_GAIN, new Unsigned8(maxTxGain).getBEREncodedByteArray());
	}

	/**
	 * This attribute is used in the intelligent search initiator process. If
	 * the value of the max_receiving_gain is below the value of this attribute,
	 * a fast synchronization process is possible.
	 *
	 * @return
	 */
	public Unsigned8 getSearchInitiatorGain() {
		try {
			return new Unsigned8(getResponseData(SFSKPhyMacSetupAttribute.SEARCH_INITIATOR_GAIN), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @param searchInitGain
	 * @throws java.io.IOException
	 */
	public void setSearchInitiatorGain(int searchInitGain) throws IOException {
		write(SFSKPhyMacSetupAttribute.SEARCH_INITIATOR_GAIN, new Unsigned8(searchInitGain).getBEREncodedByteArray());
	}

	/**
	 * Contains frequencies required for S-FSK modulation.
	 * The default unit is Hz.
	 *
	 * @return
	 */
	public Frequencies getFrequencies() {
		try {
			return new Frequencies(getResponseData(SFSKPhyMacSetupAttribute.FREQUENCIES), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @param frequencies
	 * @throws java.io.IOException
	 */
	public void setFrequencies(Frequencies frequencies) throws IOException {
		write(SFSKPhyMacSetupAttribute.FREQUENCIES, frequencies.getBEREncodedByteArray());
	}

	/**
	 * @param frequencyGroups
	 * @throws java.io.IOException
	 */
	public void setFrequencies(FrequencyGroup[] frequencyGroups) throws IOException {
		setFrequencies(Frequencies.fromFrequencyGroups(frequencyGroups));
	}

	/**
	 * Holds the MIB variable mac-address (variable 3) specified in IEC 61334-4-512 5.3 and in IEC 61334-5-1 4.3.7.6.
	 * NOTE MAC addresses are expressed on 12 bits.
	 *
	 * Contains the value of the address of the physical attachment (MAC address)
	 * associated to the local system. In the unconfigured state, the MAC address is �NEW-address�.
	 * This attribute is locally written by the CIASE when the system is registered
	 * (with a Register service). The value is used in each outgoing or incoming
	 * frame. The default value is "NEW-address".
	 *
	 * This attribute is set to NEW:
	 * - by the MAC sub-layer, once the time-out-not-addressed delay is exceeded;
	 * - when a client system �resets� the server system.
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
			return new MacAddress(getResponseData(SFSKPhyMacSetupAttribute.MAC_ADDRESS), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public MacAddressList getMacGroupAddresses() {
		try {
			return new MacAddressList(getResponseData(SFSKPhyMacSetupAttribute.MAC_GROUP_ADDRESSES), 0, 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Repeater getRepeater() {
		try {
			return new Repeater(getResponseData(SFSKPhyMacSetupAttribute.REPEATER), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void setRepeater(Repeater repeater) throws IOException {
		write(SFSKPhyMacSetupAttribute.REPEATER, repeater.getBEREncodedByteArray());
	}

	public BooleanObject getRepeaterStatus() {
		try {
			return new BooleanObject(getResponseData(SFSKPhyMacSetupAttribute.REPEATER_STATUS), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned8 getMinDeltaCredit() {
		try {
			return new Unsigned8(getResponseData(SFSKPhyMacSetupAttribute.MIN_DELTA_CREDIT), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public MacAddress getInitiatorMacAddress() {
		try {
			return new MacAddress(getResponseData(SFSKPhyMacSetupAttribute.INITIATOR_MAC_ADDRESS), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public BooleanObject getSynchronizationLocked() {
		try {
			return new BooleanObject(getResponseData(SFSKPhyMacSetupAttribute.SYNCHRONIZATION_LOCKED), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public Unsigned8 getActiveChannel() {
		try {
			return new Unsigned8(getResponseData(SFSKPhyMacSetupAttribute.ACTIVE_CHANNEL), 0);
		} catch (IOException e) {
			return null;
		}
	}

	public void setActiveChannel(Unsigned8 activeChannel) throws IOException {
			write(SFSKPhyMacSetupAttribute.ACTIVE_CHANNEL, activeChannel.getBEREncodedByteArray());
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		TypeEnum initElectricalPhase = getInitiatorElectricalPhase();
		TypeEnum deltaElectPhase = getDeltaElectricalPhase();
		Unsigned8 maxRXGain = getMaxReceivingGain();
		Unsigned8 maxTXGain = getMaxTransmittingGain();
		Unsigned8 searchInitGain = getSearchInitiatorGain();
		Frequencies freq = getFrequencies();
		MacAddress macAddr = getMacAddress();
		MacAddressList macGroupAddr = getMacGroupAddresses();
		TypeEnum rep = getRepeater();
		BooleanObject repeaterStat = getRepeaterStatus();
		Unsigned8 minDeltaCred = getMinDeltaCredit();
		MacAddress initMacAddress = getInitiatorMacAddress();
		BooleanObject syncLocked = getSynchronizationLocked();
		Unsigned8 activeCh = getActiveChannel();

		StringBuilder sb = new StringBuilder();
		sb.append("SFSKPhyMacSetup").append(crlf);
		sb.append(" > initiatorElectricalPhase = ").append(initElectricalPhase != null ? initElectricalPhase.getValue() : null).append(crlf);
		sb.append(" > deltaElectricalPhase = ").append(deltaElectPhase != null ? deltaElectPhase.getValue() : null).append(crlf);
		sb.append(" > maxReceivingGain = ").append(maxRXGain != null ? maxRXGain.getValue() : null).append(crlf);
		sb.append(" > maxTransmittingGain = ").append(maxTXGain != null ? maxTXGain.getValue() : null).append(crlf);
		sb.append(" > searchInitiatorGain = ").append(searchInitGain != null ? searchInitGain.getValue() : null).append(crlf);
		sb.append(" > frequencies = ").append(freq != null ? freq.toString().replace("\n", " ").replace("\r", "") : null).append(crlf);
		sb.append(" > macAddress = ").append(macAddr != null ? macAddr : null).append(crlf);
		sb.append(" > macGroupAddresses = ").append(macGroupAddr != null ? macGroupAddr.toString().replace("\n", " ").replace("\r", "") : null).append(crlf);
		sb.append(" > repeater = ").append(rep != null ? rep.getValue() : null).append(crlf);
		sb.append(" > repeaterStatus = ").append(repeaterStat != null ? repeaterStat.getState() : null).append(crlf);
		sb.append(" > minDeltaCredit = ").append(minDeltaCred != null ? minDeltaCred.getValue() : null).append(crlf);
		sb.append(" > initiatorMacAddress = ").append(initMacAddress != null ? initMacAddress : null).append(crlf);
		sb.append(" > synchronizationLocked = ").append(syncLocked != null ? syncLocked.getState() : null).append(crlf);
		sb.append(" > activeChannel = ").append(activeCh != null ? activeCh.getValue() : null).append(crlf);
		return sb.toString();
	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getDefaultObisCode(), toString());
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		SFSKPhyMacSetupAttribute attribute = SFSKPhyMacSetupAttribute.findByAttributeNumber(attributeNumber);
		if (attribute != null) {
			switch (attribute) {
				case LOGICAL_NAME:
					OctetString ln = getLogicalName();
					return new RegisterValue(getDefaultObisCode(), ln != null ? ObisCode.fromByteArray(ln.getOctetStr()).toString() : "null");
				case INITIATOR_ELECTRICAL_PHASE:
					ElectricalPhase phase = getInitiatorElectricalPhase();
					return new RegisterValue(getDefaultObisCode(), phase != null ? phase.toString() : "null");
				case DELTA_ELECTRICAL_PHASE:
					DeltaElectricalPhase deltaPhase = getDeltaElectricalPhase();
					return new RegisterValue(getDefaultObisCode(), deltaPhase != null ? deltaPhase.toString() : "null");
				case MAX_RECEIVING_GAIN:
					Unsigned8 gainTx = getMaxReceivingGain();
					return new RegisterValue(getDefaultObisCode(), gainTx != null ? String.valueOf(gainTx.getValue()) : "null");
				case MAX_TRANSMITTING_GAIN:
					Unsigned8 gainRx = getMaxTransmittingGain();
					return new RegisterValue(getDefaultObisCode(), gainRx != null ? String.valueOf(gainRx.getValue()) : "null");
				case SEARCH_INITIATOR_GAIN:
					Unsigned8 gainInitiator = getSearchInitiatorGain();
					return new RegisterValue(getDefaultObisCode(), gainInitiator != null ? String.valueOf(gainInitiator.getValue()) : "null");
				case FREQUENCIES:
					Frequencies freq = getFrequencies();
					return new RegisterValue(getDefaultObisCode(), freq != null ? freq.toString() : "null");
				case MAC_ADDRESS:
					MacAddress mac = getMacAddress();
					return new RegisterValue(getDefaultObisCode(), mac != null ? mac.toString() : "null");
				case MAC_GROUP_ADDRESSES:
					MacAddressList groupMacs = getMacGroupAddresses();
					return new RegisterValue(getDefaultObisCode(), groupMacs != null ? groupMacs.toString() : "null");
				case REPEATER:
					Repeater rep = getRepeater();
					return new RegisterValue(getDefaultObisCode(), rep != null ? rep.toString() : "null");
				case REPEATER_STATUS:
					BooleanObject repStatus = getRepeaterStatus();
					return new RegisterValue(getDefaultObisCode(), repStatus != null ? String.valueOf(repStatus.getState()) : "null");
				case MIN_DELTA_CREDIT:
					Unsigned8 minDeltaCred = getMinDeltaCredit();
					return new RegisterValue(getDefaultObisCode(), minDeltaCred != null ? String.valueOf(minDeltaCred.getValue()) : "null");
				case INITIATOR_MAC_ADDRESS:
					MacAddress initMac = getInitiatorMacAddress();
					return new RegisterValue(getDefaultObisCode(), initMac != null ? initMac.toString() : "null");
				case SYNCHRONIZATION_LOCKED:
					BooleanObject syncLocked = getSynchronizationLocked();
					return new RegisterValue(getDefaultObisCode(), syncLocked != null ? String.valueOf(syncLocked.getState()) : "null");
				case ACTIVE_CHANNEL:
					Unsigned8 channel = getActiveChannel();
					return new RegisterValue(getDefaultObisCode(), channel != null ? String.valueOf(channel.getValue()) : "null");
			}

		}
		return null;
	}

}
