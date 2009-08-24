package com.energyict.protocolimpl.iec1107.as220;

import java.io.IOException;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

/**
 * @author jme
 * @since 20-aug-2009
 */
public class AS220MeterclassWriter {

	private static final int MAX_PACKETSIZE = 48;

	private AS220 aS220;

	/**
	 * @param aS220
	 */
	public AS220MeterclassWriter(AS220 aS220) {
		this.aS220 = aS220;
	}

	/**
	 * @param messageEntry
	 * @param messageType
	 * @throws IOException
	 */
	public void writeClassSettings(MessageEntry messageEntry, AS220MessageType messageType) throws IOException {
		String returnValue = "";
		String iec1107Command = "";

		int first = 0;
		int last = 0;
		int offset = 0;
		int length = 0;

		if (this.aS220.getISecurityLevel() < 1) {
			throw new IOException("Message " + messageType.getDisplayName() + " needs at least security level 1. Current level: " + this.aS220.getISecurityLevel());
		}

		String message = AS220Utils.getXMLAttributeValue(messageType.getTagName(), messageEntry.getContent());
		message = AS220Utils.cleanAttributeValue(message);
		if (message.length() != messageType.getLength()) {
			throw new IOException("Wrong length !!! Length should be " + messageType.getLength() + " but was " + message.length());
		}
		if (!AS220Utils.containsOnlyTheseCharacters(message.toUpperCase(), "0123456789ABCDEF")) {
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

			getAS220().getLogger().finest(	" classNumber: " + ProtocolUtils.buildStringHex(messageType.getClassnr(), 2) +
					" First: " + ProtocolUtils.buildStringHex(first, 4) +
					" Last: " + ProtocolUtils.buildStringHex(last, 4) +
					" Offset: " + ProtocolUtils.buildStringHex(offset, 4) +
					" Length: " + ProtocolUtils.buildStringHex(length, 4) +
					" Sending iec1107Command: [ W1." + iec1107Command + " ]"
			);

			returnValue = this.aS220.getFlagIEC1107Connection().sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1, iec1107Command.getBytes());
			if (returnValue != null) {
				throw new IOException(" Wrong response on iec1107Command: W1." + iec1107Command + "] expected 'null' but received " + ProtocolUtils.getResponseData(returnValue.getBytes()));
			}
			first = last;

		} while (first < message.length());

	}

	private AS220 getAS220() {
		return this.aS220;
	}

}
