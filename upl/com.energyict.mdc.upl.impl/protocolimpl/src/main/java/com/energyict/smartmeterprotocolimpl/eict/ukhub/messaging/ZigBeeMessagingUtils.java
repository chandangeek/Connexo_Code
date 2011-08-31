package com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 31/08/11
 * Time: 12:09
 */
public class ZigBeeMessagingUtils {

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
        if (formattedAddress.length() != 16) {
            throw new IOException("Address should have 8 bytes (16 hex characters) but was [" + formattedAddress.length() + "]: [" + formattedAddress + "]");
        }

        // Check if we can parse the hex string to a byte array
        try {
            ProtocolTools.getBytesFromHexString(formattedAddress, "");
        } catch (Exception e) {
            throw new IOException("Unable to parse address [" + formattedAddress + "]: " + e.getMessage());
        }

        return formattedAddress;
    }

}
