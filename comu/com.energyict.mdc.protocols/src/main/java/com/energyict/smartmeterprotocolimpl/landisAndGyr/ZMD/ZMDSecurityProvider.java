package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 11:36
 */

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

import java.io.IOException;
import java.util.Properties;

/**
 * Default implementation of the securityProvider for the ZMD Meters.
 * Provides all the securityKeys, just for LOCAL purpose
 * Functionality is implemented according to the NTA specification
 *
 */
public class ZMDSecurityProvider extends NTASecurityProvider {

    private static final int MAN_SPEC_RESPONDING_VALUE_TOTAL_LENGTH = 8;
    private static final int MAN_SPEC_RESPONDING_VALUE_LENGTH = 7;
    private static final int MAN_SPEC_TYPE_INDEX = 7;

    private static final int VARIANT0_ADD = 0;
    private static final int VARIANT1_OR = 1;
    private static final int VARIANT2_XOR = 2;
    private static final int VARIANT3_ADD_OR = 3;
    private static final int VARIANT4_ADD_XOR = 4;
    private static final int VARIANT5_ADD = 5;

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public ZMDSecurityProvider(Properties properties) {
        super(properties);
    }

    @Override
    public byte[] getCallingAuthenticationValue() {

        switch (this.securityLevel) {
            case 0:
                return new byte[0];
            case 1: {
                return getHLSSecret();
            }
            case 2:
                return new byte[0];
            case 3: {    // this is a ClientToServer challenge for MD5
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 4: {    // this is a ClientToServer challenge for SHA -1
                generateClientToServerChallenge();
                return this.cTOs;
            }
            case 5: {    // this is a ClientToServer challenge for GMAC
                generateClientToServerChallenge();
                return this.cTOs;
            }
            default:
                return new byte[0];
        }
    }

    /**
     * Construct the content of the responseValue when a Manufacturer Specific encryption algorithm ({@link com.energyict.dlms.aso.AuthenticationTypes#MAN_SPECIFIC_LEVEL}) is applied.
     *
     * @param respondingAuthenticationValue the response value from the meter OR null
     * @return the encrypted Value to send back to the meter
     */
    @Override
    public byte[] associationEncryptionByManufacturer(final byte[] respondingAuthenticationValue) throws IOException {
        if (respondingAuthenticationValue == null) {
            return new byte[0];
        }
        if (respondingAuthenticationValue.length != MAN_SPEC_RESPONDING_VALUE_TOTAL_LENGTH) {
            throw new IOException("RespondingAuthenticationValue should be 8 bytes instead of " + respondingAuthenticationValue.length);
        }

        byte[] intArray = convertASCIIArrayToIntegerArray(respondingAuthenticationValue);

        int encryptionType = intArray[MAN_SPEC_TYPE_INDEX];
        byte[] encryptedAuthenticationValue = new byte[MAN_SPEC_RESPONDING_VALUE_LENGTH];
        System.arraycopy(intArray,0, encryptedAuthenticationValue, 0, MAN_SPEC_RESPONDING_VALUE_LENGTH);
        switch (encryptionType) {
            case VARIANT0_ADD:
                encryptedAuthenticationValue = bitWiseAdd(encryptedAuthenticationValue, convertASCIIArrayToIntegerArray(getHLSSecret()));break;
            case VARIANT1_OR:
                encryptedAuthenticationValue = bitWiseOr(encryptedAuthenticationValue, convertASCIIArrayToIntegerArray(getHLSSecret()));break;
            case VARIANT2_XOR:
                encryptedAuthenticationValue = bitWiseXor(encryptedAuthenticationValue, convertASCIIArrayToIntegerArray(getHLSSecret()));break;
            case VARIANT3_ADD_OR:
                encryptedAuthenticationValue = bitWiseAddOr(encryptedAuthenticationValue, convertASCIIArrayToIntegerArray(getHLSSecret()));break;
            case VARIANT4_ADD_XOR:
                encryptedAuthenticationValue = bitWiseAddXor(encryptedAuthenticationValue, convertASCIIArrayToIntegerArray(getHLSSecret()));break;
            case VARIANT5_ADD:
                encryptedAuthenticationValue = bitWiseAdd(encryptedAuthenticationValue, convertASCIIArrayToIntegerArray(getHLSSecret()));break;
        }
        return convertIntegerArrayToASCIIArray(DLMSUtils.concatByteArrays(encryptedAuthenticationValue, new byte[]{(byte) encryptionType}));
    }

    /**
     * Convert a given integerArray to ASCII
     *
     * @param integerArray
     * @return
     */
    public static byte[] convertIntegerArrayToASCIIArray(final byte[] integerArray) {
        if (integerArray == null) {
            return new byte[0];
        }
        byte[] asciiArray = new byte[integerArray.length];
        for (int i = 0; i < integerArray.length; i++) {
            asciiArray[i] = (byte) Integer.toString(integerArray[i], 16).toUpperCase().charAt(0);
        }
        return asciiArray;
    }

    /**
     * Convert a given ASCII array to an integerArray
     *
     * @param asciiArray
     * @return
     */
    public static byte[] convertASCIIArrayToIntegerArray(final byte[] asciiArray) {
        if (asciiArray == null) {
            return new byte[0];
        }
        byte[] integerArray = new byte[asciiArray.length];
        for (int i = 0; i < asciiArray.length; i++) {
            integerArray[i] = Integer.valueOf(String.valueOf((char) asciiArray[i]), 16).byteValue();
        }
        return integerArray;
    }

    /**
     * All elements of the second byteArray is bitWise <i>ADD</i> operated with the corresponding element in the first byteArray
     *
     * @param firstByte  the first byteArray
     * @param secondByte the second ByteArray
     * @return the ADD operated result
     * @throws java.io.IOException when either one of the arguments is null or when the length of both arguments is not the same
     */
    public static byte[] bitWiseAdd(final byte[] firstByte, final byte[] secondByte) throws IOException {
        if (firstByte == null || secondByte == null) {
            throw new IOException("Bitwise ADD-operation requires two not-null arguments.");
        }
        if (firstByte.length != secondByte.length) {
            throw new IOException("Bitwise ADD-operation requires arguments with the same length.");
        }

        byte[] addition = new byte[firstByte.length];
        for (int i = 0; i < firstByte.length; i++) {
            addition[i] = (byte) ((firstByte[i] + secondByte[i]) & 0x0F);
        }
        return addition;
    }

    /**
     * All elements of the second byteArray is bitWise <i>OR</i> operated with the corresponding element in the first byteArray
     *
     * @param firstByte  the first byteArray
     * @param secondByte the second ByteArray
     * @return the OR operated result
     * @throws java.io.IOException when either one of the arguments is null or when the length of both arguments is not the same
     */
    public static byte[] bitWiseOr(final byte[] firstByte, final byte[] secondByte) throws IOException {
        if (firstByte == null || secondByte == null) {
            throw new IOException("Bitwise OR-operation requires two not-null arguments.");
        }
        if (firstByte.length != secondByte.length) {
            throw new IOException("Bitwise OR-operation requires arguments with the same length.");
        }

        byte[] addition = new byte[firstByte.length];
        for (int i = 0; i < firstByte.length; i++) {
            addition[i] = (byte) (firstByte[i] | secondByte[i]);
        }
        return addition;
    }

    /**
     * All elements of the second byteArray is bitWise <i>XOR</i> operated with the corresponding element in the first byteArray
     *
     * @param firstByte  the first byteArray
     * @param secondByte the second ByteArray
     * @return the XOR operated result
     * @throws java.io.IOException when either one of the arguments is null or when the length of both arguments is not the same
     */
    public static byte[] bitWiseXor(final byte[] firstByte, final byte[] secondByte) throws IOException {
        if (firstByte == null || secondByte == null) {
            throw new IOException("Bitwise XOR-operation requires two not-null arguments.");
        }
        if (firstByte.length != secondByte.length) {
            throw new IOException("Bitwise XOR-operation requires arguments with the same length.");
        }

        byte[] addition = new byte[firstByte.length];
        for (int i = 0; i < firstByte.length; i++) {
            addition[i] = (byte) (firstByte[i] ^ secondByte[i]);
        }
        return addition;
    }

    /**
     * All <b>ODD</b> elements of the second byteArray are bitWise <i>ADD</i> operated with the corresponding element in the first byteArray.
     * All <b>EVEN</b> elements of the second byteArray are bitWise <i>OR</i> operated with the corresponding element in the first byteArray.
     *
     * @param firstByte  the first byteArray
     * @param secondByte the second ByteArray
     * @return the ADD/OR operated result
     * @throws java.io.IOException when either one of the arguments is null or when the length of both arguments is not the same
     */
    public static byte[] bitWiseAddOr(final byte[] firstByte, final byte[] secondByte) throws IOException {
        if (firstByte == null || secondByte == null) {
            throw new IOException("Bitwise Add/Or-operation requires two not-null arguments.");
        }
        if (firstByte.length != secondByte.length) {
            throw new IOException("Bitwise Add/Or-operation requires arguments with the same length.");
        }

        byte[] addition = new byte[firstByte.length];
        for (int i = 0; i < firstByte.length; i++) {
            if (i % 2 == 0) {
                addition[i] = (byte) ((firstByte[i] + secondByte[i]) & 0x0F);
            } else {
                addition[i] = (byte) (firstByte[i] | secondByte[i]);
            }
        }
        return addition;
    }

    /**
     * All <b>ODD</b> elements of the second byteArray are bitWise <i>ADD</i> operated with the corresponding element in the first byteArray.
     * All <b>EVEN</b> elements of the second byteArray are bitWise <i>XOR</i> operated with the corresponding element in the first byteArray.
     *
     * @param firstByte  the first byteArray
     * @param secondByte the second ByteArray
     * @return the ADD/XOR operated result
     * @throws java.io.IOException when either one of the arguments is null or when the length of both arguments is not the same
     */
    public static byte[] bitWiseAddXor(final byte[] firstByte, final byte[] secondByte) throws IOException {
        if (firstByte == null || secondByte == null) {
            throw new IOException("Bitwise Add/Xor-operation requires two not-null arguments.");
        }
        if (firstByte.length != secondByte.length) {
            throw new IOException("Bitwise Add/Xor-operation requires arguments with the same length.");
        }

        byte[] addition = new byte[firstByte.length];
        for (int i = 0; i < firstByte.length; i++) {
            if (i % 2 == 0) {
                addition[i] = (byte) ((firstByte[i] + secondByte[i]) & 0x0F);
            } else {
                addition[i] = (byte) (firstByte[i] ^ secondByte[i]);
            }
        }
        return addition;
    }
}