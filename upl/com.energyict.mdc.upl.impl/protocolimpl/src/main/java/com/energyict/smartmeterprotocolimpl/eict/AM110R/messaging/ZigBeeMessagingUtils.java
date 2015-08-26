package com.energyict.smartmeterprotocolimpl.eict.AM110R.messaging;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 31/08/11
 * Time: 12:09
 */
public class ZigBeeMessagingUtils {

    public static final int LINK_KEY_LENGTH = 16;
    public static final int IEEE_ADDRESS_LENGTH = 8;

    private ZigBeeMessagingUtils() {
        // Util class with only static methods. Should not be instantiated
    }

    public static String validateAndFormatIeeeAddress(String address) throws IOException {
        return validateAndFormatIeeeAddress(address, true);
    }

    public static String validateAndFormatIeeeAddress(String address, boolean required) throws IOException {
        if (address == null && required) {
            throw new IOException("IEEE address is required but was 'null'.");
        }

        if (address == null && !required) {
            return null;
        }

        String formattedAddress = address.toUpperCase(); // Make the address all upper case
        formattedAddress = formattedAddress.replaceAll("[^A-F0-9]", ""); // Remove all the non-hex characters

        // Check if the formatted hex string is exactly 16 characters long (8 bytes)
        if (formattedAddress.length() != IEEE_ADDRESS_LENGTH * 2) {
            throw new IOException("Address should have " + IEEE_ADDRESS_LENGTH + " bytes (" + IEEE_ADDRESS_LENGTH * 2 + " hex characters) but was [" + formattedAddress.length() + "]: [" + formattedAddress + "]");
        }

        // Check if we can parse the hex string to a byte array
        try {
            ProtocolTools.getBytesFromHexString(formattedAddress, "");
        } catch (Exception e) {
            throw new IOException("Unable to parse address [" + formattedAddress + "]: " + e.getMessage());
        }

        return formattedAddress;
    }

    public static String validateAndFormatLinkKey(String linkKey) throws IOException {
        if (linkKey == null) {
            throw new IOException("Link key is required but was 'null'.");
        }

        String formattedLinkKey = linkKey.toUpperCase(); // Make the link key all upper case
        formattedLinkKey = formattedLinkKey.replaceAll("[^A-F0-9]", ""); // Remove all the non-hex characters

        // Check if the formatted hex string is exactly 32 characters long (16 bytes)
        if (formattedLinkKey.length() != LINK_KEY_LENGTH * 2) {
            throw new IOException("Link key should have " + LINK_KEY_LENGTH + " bytes (" + LINK_KEY_LENGTH * 2 + " hex characters) but was [" + formattedLinkKey.length() + "]: [" + formattedLinkKey + "]");
        }

        // Check if we can parse the hex string to a byte array
        try {
            ProtocolTools.getBytesFromHexString(formattedLinkKey, "");
        } catch (Exception e) {
            throw new IOException("Unable to parse Link key [" + formattedLinkKey + "]: " + e.getMessage());
        }

        return formattedLinkKey;
    }

}
