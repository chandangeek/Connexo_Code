package com.energyict.protocolimpl.utils;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.DataParseException;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class IPv6Utils {

    private static final int EXPECTED_IPV6_ADDRESS_LENGTH = 32;
    private static final int EXPECTED_IPV6_ADDRESS_BLOCK_SIZE = 4; //we consider an IPv6 to have 8 blocks of 4 hex chars each

    private static final Logger logger =Logger.getLogger(IPv6Utils.class.getName());

    /**
     * This method will parse the IPv6 address and extract all unwanted chars like ":" and "/".
     * It will also expand it add the leading 0's in order to make it a 128 bit size
     *
     * @param ipv6AddressCompressedWithPrefixLength compressed IPv6 including "/" length info
     * @return the IPv6 address extended in the format expected by the meter
     */
    public static String getFullyExtendedIPv6Address(final String ipv6AddressCompressedWithPrefixLength) {
        String ipv6AddressCompressed = removePrefixLength(ipv6AddressCompressedWithPrefixLength);
        String ipv6AddressDecompressed = decompressIPv6Address(ipv6AddressCompressed);
        String[] ipv6Blocks = ipv6AddressDecompressed.split(":");
        StringBuilder ipv6IncludingZeros = new StringBuilder();

        for (String ipv6Block : ipv6Blocks) {
            if (ipv6Block.length() != EXPECTED_IPV6_ADDRESS_BLOCK_SIZE) {
                //add leading zeros
                StringBuilder ipv6BlockWithLeadingZeros = addLeadingZerosToBlock(ipv6Block);
                ipv6IncludingZeros.append(ipv6BlockWithLeadingZeros);
            } else {
                ipv6IncludingZeros.append(ipv6Block);
            }
        }

        if (ipv6IncludingZeros.length() != EXPECTED_IPV6_ADDRESS_LENGTH) {
            throw DataParseException.generalParseException(new ProtocolException("Unable to expand the given IPv6: " + ipv6AddressCompressed + " to a 128-bit length value"));
        }

        return ipv6IncludingZeros.toString();
    }

    private static String decompressIPv6Address(String ipv6AddressCompressed) {
        try {
            final Inet6Address addr = (Inet6Address) InetAddress.getByName(ipv6AddressCompressed);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            throw DataParseException.generalParseException(e);

        }
    }

    private static String removePrefixLength(String ipv6AddressCompressedWithPrefixLength) {
        String ipv6AddressCompressed;
        if (!ipv6AddressCompressedWithPrefixLength.contains("/")) {
            throw DataParseException.generalParseException(new ProtocolException("Prefix length indicator is not included after IPv6 address:" + ipv6AddressCompressedWithPrefixLength));
        } else {
            ipv6AddressCompressed = ipv6AddressCompressedWithPrefixLength.split("/")[0];
        }
        return ipv6AddressCompressed;
    }

    public static int getPrefixLength(String ipv6AddressCompressedWithPrefixLength) {
        //prefix length is at the end, after "/" char
        String prefixLength;
        if (!ipv6AddressCompressedWithPrefixLength.contains("/")) {
            throw DataParseException.generalParseException(new ProtocolException("Prefix length indicator is not included after IPv6 address:" + ipv6AddressCompressedWithPrefixLength));
        } else {
            prefixLength = ipv6AddressCompressedWithPrefixLength.split("/")[1];
        }
        return Integer.parseInt(prefixLength);
    }

    private static StringBuilder addLeadingZerosToBlock(String ipv6Block) {
        StringBuilder ipv6BlockWithLeadingZeros = new StringBuilder(ipv6Block);
        for (int i = 0; i < EXPECTED_IPV6_ADDRESS_BLOCK_SIZE - ipv6Block.length(); i++) {
            ipv6BlockWithLeadingZeros.insert(0, "0");
        }
        return ipv6BlockWithLeadingZeros;
    }

    /**
     * Returns the IPv6 address for the given node as text with given prefix.
     *
     * @param 	panId			The PAN ID.
     * @param 	shortAddress	The short address.
     * @param	prefix			Prefix to be used.
     *
     * @return	The address with given prefix as a string.
     */
    public static String getAddressForNodeAsText(final int panId, final int shortAddress, final int scopeId, final byte[] prefix) {
        final byte[] addressBytes = getAddressForNodeAsByteArray(panId, shortAddress, prefix);

        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < addressBytes.length; i += 2) {
            final int current = ((addressBytes[i] & 0xFF) << 8) + (addressBytes[i + 1] & 0xFF);
            builder.append(Integer.toHexString(current));

            if (i < addressBytes.length - 2) {
                builder.append(":");
            }
        }

        if (scopeId>0) {
            builder.append("%").append(scopeId);
        }

        return builder.toString().toUpperCase();
    }

    /**
     * Gets the address for the particular node as a byte array.
     *
     * @param 	panId			The PAN ID.
     * @param 	shortAddress	The short address.
     * @param	prefix			Prefix to be used (max 8 bytes)
     *
     * @return	The byte array containing the address.
     */
    public static byte[] getAddressForNodeAsByteArray(final int panId, final int shortAddress, final byte[] prefix) {
        final int prefixLen = Integer.min(prefix.length, 8);
        final byte[] address = new byte[16];

        for (int i = 0; i < prefixLen; i++) {
            address[i] = prefix[i];
        }
        for (int i = prefixLen; i < 8; i++) {
            address[i] = 0x00;
        }
        address[8] = (byte) (((panId & 0xFF00) >> 8) & 0xFF);
        address[9] = (byte) (panId & 0xFF);
        address[10] = 0x00;
        address[11] = (byte) 0xFF;
        address[12] = (byte) 0xFE;
        address[13] = 0x00;
        address[14] = (byte) (((shortAddress & 0xFF00) >> 8) & 0xFF);
        address[15] = (byte) (shortAddress & 0xFF);

        return address;
    }

    /**
     * Rebuild a node IPv6 address given the prefix, length, panId and short-address
     * This is used in mainly in Beacon topology to reconstruct IPv6 slave addresses
     *
     * @param iPv6AddressAndPrefixLength    as configured in Beacon,  eg. fc11:cc60:ff26:1::/64
     * @param macPANId                      as collected from Beacon, eg. 0x94bf
     * @param shortAddress                  as collected from Beacon, eg. 0x74
     * @return  the complete IPv6 address constructed using parameters above, eg. fc11:cc60:ff26:1:94bf:ff:fe00:74
     */
    public static String getNodeAddress(String iPv6AddressAndPrefixLength, int macPANId, int shortAddress) {
        int scopeId = -1; // do not append scopeId
        byte[] prefix;


        if (iPv6AddressAndPrefixLength!=null && !iPv6AddressAndPrefixLength.isEmpty()) {
            String routingDestination = IPv6Utils.getFullyExtendedIPv6Address(iPv6AddressAndPrefixLength);
            prefix = ProtocolTools.getBytesFromHexString(routingDestination, "");
        } else {
            prefix = new byte[0]; // no prefix
        }

        String ipv6 = getAddressForNodeAsText(macPANId, shortAddress, scopeId, prefix);

        return ipv6.toLowerCase();
    }

    public static boolean isValid(String ipv6address){
        if (ipv6address == null){
            return false;
        }

        try {
            InetAddress ipv6 = Inet6Address.getByName(ipv6address);
            String reconvertedIpv6 = ipv6.getHostAddress();
            return ipv6address.equalsIgnoreCase( reconvertedIpv6 );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}

