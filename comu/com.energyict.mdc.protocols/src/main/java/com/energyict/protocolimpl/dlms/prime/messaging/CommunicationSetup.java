/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.protocolimpl.dlms.prime.PrimeRegisters;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Messages concerning the communication setup.
 *
 * @author alex
 *
 */
public final class CommunicationSetup extends PrimeMessageExecutor {

	/**
	 * Create a new instance.
	 *
	 * @param 		session		The session.
	 */
	public CommunicationSetup(DlmsSession session) {
		super(session);
	}

	/** Root tag for setting the multicast addresses. */
	private static final String ROOT_TAG_SET_MULTICAST_ADDRESSES = "SetMulticastAddresses";

	/** The first address. */
	private static final String ATTRIBUTE_ADDRESS_1 = "Address 1";

	/** The second address. */
	private static final String ATTRIBUTE_ADDRESS_2 = "Address 2";

	/** The third address. */
	private static final String ATTRIBUTE_ADDRESS_3 = "Address 3";

	/**
	 * Gets the message category for the power quality messages.
	 *
	 * @return	The message category for the power quality messages.
	 */
	public static final MessageCategorySpec getCategorySpec() {
		final MessageCategorySpec spec = new MessageCategorySpec("Communication setup");

		spec.addMessageSpec(addBasicMsgWithAttributes("Multicast addresses", ROOT_TAG_SET_MULTICAST_ADDRESSES, true, ATTRIBUTE_ADDRESS_1, ATTRIBUTE_ADDRESS_2, ATTRIBUTE_ADDRESS_3));

		return spec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canHandle(MessageEntry messageEntry) {
		return isMessageTag(ROOT_TAG_SET_MULTICAST_ADDRESSES, messageEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final MessageResult execute(final MessageEntry messageEntry) throws IOException {
		final String messageContent = messageEntry.getContent();

		final String address1 = getAttributeValue(ATTRIBUTE_ADDRESS_1, messageContent);
		final String address2 = getAttributeValue(ATTRIBUTE_ADDRESS_2, messageContent);
		final String address3 = getAttributeValue(ATTRIBUTE_ADDRESS_3, messageContent);

		final OctetString valueToWrite = generateMulticastIDOctetString(address1, address2, address3);

		if (this.getLogger().isLoggable(Level.INFO)) {
			this.getLogger().log(Level.INFO, "Writing multicast addresses [" + address1 + ", " + address2 + ", " + address3 + "] to Device ID 6");
		}

		final Data deviceID6 = this.getSession().getCosemObjectFactory().getData(PrimeRegisters.MULTICAST_IDENTIFIER);

		if (deviceID6 != null) {
			deviceID6.setValueAttr(valueToWrite);
		} else {
			this.getLogger().log(Level.WARNING, "Device ID 6 data object not found @OBIS code [" + PrimeRegisters.MULTICAST_IDENTIFIER + "] !");

			return MessageResult.createFailed(messageEntry);
		}

		return MessageResult.createSuccess(messageEntry);
	}

	/**
	 * Generates the octet string we will write into the Device ID 6 object.
	 *
	 * @param 	address1		The first multicast address.
	 * @param 	address2		The second multicast address.
	 * @param 	address3		The third multicast address.
	 *
	 * @return
	 */
	private static final OctetString generateMulticastIDOctetString(final String address1, final String address2, final String address3) throws IOException {
		final byte[] octetStringData = new byte[24];

		System.arraycopy(convert(address1), 0, octetStringData, 0, 8);
		System.arraycopy(convert(address2), 0, octetStringData, 8, 8);
		System.arraycopy(convert(address3), 0, octetStringData, 16, 8);

		return new OctetString(octetStringData);
	}

	/**
	 * Converts the given address to a byte array.
	 *
	 * @param 		hexAddress		The hex address.
	 *
	 * @return		The converted address.
	 */
	private static final byte[] convert(final String hexAddress) {
		if (hexAddress != null && hexAddress.length() == 16) {
			return toByteArray(hexAddress);
		} else {
			return new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		}
	}
	/**
	 * Convert the given hex string to a byte array.
	 *
	 * @param 		hexString		The hex string to convert.
	 *
	 * @return		The byte array.
	 */
	private static final byte[] toByteArray(final String hexString) {
		final byte[] bytes = new byte[hexString.length() / 2];

		for (int i = 0; i < hexString.length(); i += 2) {
			final String currentByteHex = hexString.substring(i, i + 2);
			bytes[i / 2] = (byte)(Integer.parseInt(currentByteHex, 16) & 0xFF);
		}

		return bytes;
	}
}
