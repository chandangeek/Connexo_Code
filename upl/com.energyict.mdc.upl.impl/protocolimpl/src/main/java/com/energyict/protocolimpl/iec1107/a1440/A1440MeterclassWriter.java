package com.energyict.protocolimpl.iec1107.a1440;

import java.io.IOException;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

/**
 * @author jme
 * @since 20-aug-2009
 */
public class A1440MeterclassWriter {

	private A1440 a1440;

	/**
	 * @param a1440
	 */
	public A1440MeterclassWriter(A1440 a1440) {
		this.a1440 = a1440;
	}

	/**
	 * @param messageEntry
	 * @param messageType
	 * @throws IOException
	 */
	public void writeClassSettings(MessageEntry messageEntry, A1440MessageType messageType) throws IOException {
		final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
		final int MAX_PACKETSIZE = 48;

		String returnValue = "";
		String iec1107Command = "";

		int first = 0;
		int last = 0;
		int offset = 0;
		int length = 0;

		if (this.a1440.getISecurityLevel() < 1) {
			throw new IOException("Message " + messageType.getDisplayName() + " needs at least security level 1. Current level: " + this.a1440.getISecurityLevel());
		}

		String message = A1440Utils.getXMLAttributeValue(messageType.getTagName(), messageEntry.getContent());
		message = A1440Utils.cleanAttributeValue(message);
		if (message.length() != messageType.getLength()) {
			throw new IOException("Wrong length !!! Length should be " + messageType.getLength() + " but was " + message.length());
		}
		if (!A1440Utils.containsOnlyTheseCharacters(message.toUpperCase(), "0123456789ABCDEF")) {
			throw new IOException("Invalid characters in message. Only the following characters are allowed: '0123456789ABCDEFabcdef'");
		}

		do {
			last = first + MAX_PACKETSIZE;
			if (last >= message.length()) {
				last = message.length();
			}
			String rawdata = message.substring(first, last);

			length = rawdata.length() / 2;
			offset = first / 2;

			iec1107Command = "C" + ProtocolUtils.buildStringHex(messageType.getClassnr(), 2);
			iec1107Command += ProtocolUtils.buildStringHex(length, 4);
			iec1107Command += ProtocolUtils.buildStringHex(offset, 4);
			iec1107Command += "(" + rawdata + ")";

			getA1440().getLogger().finest(	" classNumber: " + ProtocolUtils.buildStringHex(messageType.getClassnr(), 2) +
					" First: " + ProtocolUtils.buildStringHex(first, 4) +
					" Last: " + ProtocolUtils.buildStringHex(last, 4) +
					" Offset: " + ProtocolUtils.buildStringHex(offset, 4) +
					" Length: " + ProtocolUtils.buildStringHex(length, 4) +
					" Sending iec1107Command: [ W1." + iec1107Command + " ]"
			);

			returnValue = this.a1440.getFlagIEC1107Connection().sendRawCommandFrameAndReturn(WRITE1, iec1107Command.getBytes());
			if (returnValue != null) {
				throw new IOException(" Wrong response on iec1107Command: W1." + iec1107Command + "] expected 'null' but received " + ProtocolUtils.getResponseData(returnValue.getBytes()));
			}
			first = last;

		} while (first < message.length());

	}

	private A1440 getA1440() {
		return this.a1440;
	}

}
