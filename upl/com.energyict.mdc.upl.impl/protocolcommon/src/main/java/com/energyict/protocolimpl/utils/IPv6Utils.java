package com.energyict.protocolimpl.utils;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.DataParseException;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6Utils {

    private static final int EXPECTED_IPV6_ADDRESS_LENGTH = 32;
    private static final int EXPECTED_IPV6_ADDRESS_BLOCK_SIZE = 4; //we consider an IPv6 to have 8 blocks of 4 hex chars each

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
}
