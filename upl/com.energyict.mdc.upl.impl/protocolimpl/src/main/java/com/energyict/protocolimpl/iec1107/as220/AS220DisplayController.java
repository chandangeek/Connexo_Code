package com.energyict.protocolimpl.iec1107.as220;

import java.io.IOException;
import java.util.logging.Logger;

import com.energyict.protocol.ProtocolUtils;

/**
 * @author jme
 * @since 25-aug-2009
 */
public class AS220DisplayController {

	private AS220 as220 = null;

	private static final int OBIS_FIELD_LENGTH = 3;
	private static final int VALUE_FIELD_LENGTH = 7;

	public AS220DisplayController(AS220 as220Protocol) {
		this.as220 = as220Protocol;
	}

	private AS220 getAs220() {
		return this.as220;
	}

	private Logger getLogger() {
		return getAs220().getLogger();
	}

	/**
	 * This command sends a message onto the display of the meter. This message
	 * has the highest priority. This means that all other messages are
	 * overwritten by this message in scrollmode.
	 * @param message The message to dispay on the device
	 * @throws IOException
	 */
	public void writeMessage(String message) throws IOException {
		getLogger().fine("Writing message to device: " + message);
		getAs220().getAS220Registry().setRegister(AS220Registry.DISPLAY_MESSAGE_REGISTER, getPacketFromString(message));
	}

	/**
	 * This command clears the message on the display of the meter.
	 * @throws IOException
	 */
	public void clearDisplay() throws IOException {
		getLogger().fine("Clearing message from device.");
		getAs220().getAS220Registry().setRegister(AS220Registry.DISPLAY_MESSAGE_REGISTER, "00");
	}

	private static String toHexString(String displayMessage) {
		String message = displayMessage != null ? displayMessage : "";
		String output = "";
		for (int i = 0; i < message.length(); i++) {
			byte bt = message.getBytes()[i];
			output += ProtocolUtils.buildStringHex(bt, 2);
		}
		return output;
	}

	private String getPacketFromString(String displayMessage) {
		String message = formatMessageLength(cleanMessageValue(displayMessage));
		message = toHexString(message);
		return "010000" + message.substring(14, 20) + "000000" + message.substring(0, 14) + "0000";
	}

	private String formatMessageLength(String displayMessage) {
		String message = displayMessage != null ? displayMessage : "";
		if (message.length() > (OBIS_FIELD_LENGTH + VALUE_FIELD_LENGTH)) {
			message = message.substring(0, OBIS_FIELD_LENGTH + VALUE_FIELD_LENGTH);
		} else {
			while (message.length() < OBIS_FIELD_LENGTH + VALUE_FIELD_LENGTH) {
				message += " ";
			}
		}
		return message;
	}

	private String cleanMessageValue(String attributeValue) {
		final char TAB = 0x09;
		final char LF = 0x0A;
		final char CR = 0x0D;

		boolean isValidChar = true;
		String returnValue = "";

		for (int i = 0; i < attributeValue.length(); i ++) {
			isValidChar = true;
			if (attributeValue.charAt(i) == CR) { isValidChar = false; }
			if (attributeValue.charAt(i) == LF) { isValidChar = false; }
			if (attributeValue.charAt(i) == TAB) { isValidChar = false; }
			if (isValidChar) {
				returnValue += attributeValue.charAt(i);
			}
		}
		return returnValue;
	}

}
