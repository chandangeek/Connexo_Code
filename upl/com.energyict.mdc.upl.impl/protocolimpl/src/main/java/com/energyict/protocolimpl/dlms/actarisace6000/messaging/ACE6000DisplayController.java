package com.energyict.protocolimpl.dlms.actarisace6000.messaging;

import com.energyict.protocolimpl.dlms.actarisace6000.ACE6000;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class sends a message onto the display of the meter. This message has
 * the highest priority. This means that all other messages are overwritten by
 * this message in scrollmode.
 */
public class ACE6000DisplayController {
    private static final int OBIS_FIELD_LENGTH = 3;
    private static final int VALUE_FIELD_LENGTH = 7;
    private static final String DISPLAY_MESSAGE_REGISTER = "0.0.12.2.0.255";

    private static final char TAB = 0x09;
    private static final char LF = 0x0A;
    private static final char CR = 0x0D;

    private ACE6000 ace6000 = null;

    /**
     * Constructor for the AS220DisplayController
     * @param ACE6000 Protocol The current ACE6000 protocol
     */
    public ACE6000DisplayController(ACE6000 protocol) {
        this.ace6000 = protocol;
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
        getAce6000().setRegister(DISPLAY_MESSAGE_REGISTER, getPacketFromString(message));
    }

//    /**
//     * This command clears the message on the display of the meter.
//     * @throws IOException
//     */
//    public void clearDisplay() throws IOException {
//        getLogger().fine("Clearing message from device.");
//        getAce6000().setRegister(ZmdRegistry.DISPLAY_MESSAGE_REGISTER, CLEAR_MESSAGE);
//    }

    /**
     * Get the ascii value in hex from a given string
     * @param displayMessage
     * @return the hex value of the string ascii code
     */
    private static String toHexString(String displayMessage) {
        String message = displayMessage != null ? displayMessage : "";
        String output = "";
        for (int i = 0; i < message.length(); i++) {
            byte bt = message.getBytes()[i];
            output += ProtocolUtils.buildStringHex(bt, 2);
        }
        return output;
    }

    /**
     * Create a valid packet with a message to display on the LCD ready to write
     * to the device
     * @param displayMessage
     * @return The package, ready to send to the device
     */
    private String getPacketFromString(String displayMessage) {
        String message = formatMessageLength(cleanMessageValue(displayMessage));
        message = toHexString(message);
        return "010000" + message.substring(14, 20) + "000000" + message.substring(0, 14) + "0000";
    }

    /**
     * Changes the display message to a fixed length of 10. When the display
     * message is to long, the message is truncated at 10 characters. When the
     * message is to short, spaces are appended at the end of the message
     * @param displayMessage
     * @return The 10 chars long message
     */
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

    /**
     * Remove CR, LF and TABS from a string
     * @param attributeValue
     * @return The cleaned value of the string
     */
    private String cleanMessageValue(String attributeValue) {
        boolean isValidChar = true;
        String returnValue = "";

        for (int i = 0; i < attributeValue.length(); i++) {
            isValidChar = true;
            if (attributeValue.charAt(i) == CR) {
                isValidChar = false;
            }
            if (attributeValue.charAt(i) == LF) {
                isValidChar = false;
            }
            if (attributeValue.charAt(i) == TAB) {
                isValidChar = false;
            }
            if (isValidChar) {
                returnValue += attributeValue.charAt(i);
            }
        }
        return returnValue;
    }

    /**
     * Getter for the current ZMD protocol
     * @return The current ZMD protocol
     */
    private ACE6000 getAce6000() {
        return this.ace6000;
    }

    /**
     * Getter for the logger of the AS220 protocol
     * @return The logger of the AS220 protocol
     */
    private Logger getLogger() {
        return getAce6000().getLogger();
    }
}

