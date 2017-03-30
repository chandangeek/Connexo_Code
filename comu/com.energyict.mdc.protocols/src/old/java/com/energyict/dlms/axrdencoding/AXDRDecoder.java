/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AXDRDecoder.java
 *
 * Created on 17 oktober 2007, 15:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;

import java.io.IOException;

/**
 * Util class to convert (parse) a byte array to a {@link AbstractDataType}
 *
 * @author kvds
 */
public final class AXDRDecoder {

    /**
     * Util class with static methods, so hide the constructor
     */
    private AXDRDecoder() {

    }

    /**
     * Decode the given byte array, and convert it to a valid {@link AbstractDataType} AXDR object
     *
     * @param data The raw bytes to parse
     * @return The {@link AbstractDataType} AXDR object
     * @throws ProtocolException If the given bytes could not be parsed
     */
    public static AbstractDataType decode(byte[] data) throws ProtocolException {
        return decode(data, 0);
    }

    /**
     * Decode the given byte array, and convert it to the expected {@link AbstractDataType} AXDR object.
     * This method validates the object after parsing the raw data if it matches the expected type
     *
     * @param data              The raw bytes to parse
     * @param expectedClassType The expected class type
     * @param <T>               The expected type of AXDR object, should extend {@link AbstractDataType}
     * @return The expected AXDR object that extends {@link AbstractDataType}
     * @throws ProtocolException If the given bytes could not be parsed, or the expected class type did not match with the actual type
     */
    public static <T extends AbstractDataType> T decode(byte[] data, Class<T> expectedClassType) throws ProtocolException {
        return decode(data, 0, expectedClassType);
    }

    public static AbstractDataType decode(byte[] data, int offset) throws ProtocolException {
        return decode(data, offset, 0);
    }

    /**
     * Decode the given byte array, starting from a given offset, and convert it to the expected {@link AbstractDataType} object.
     * This method validates the object after parsing the raw data if it matches the expected type
     *
     * @param data          The raw bytes to parse
     * @param offset        The offset in the raw data to start parsing from
     * @param expectedClass The expected class type
     * @param <T>           The expected type of AXDR object, should extend {@link AbstractDataType}
     * @return The expected AXDR object that extends {@link AbstractDataType}
     * @throws ProtocolException If the given bytes could not be parsed, or the expected class type did not match with the actual type
     */
    public static <T extends AbstractDataType> T decode(byte[] data, int offset, Class<T> expectedClass) throws ProtocolException {
        return decode(data, offset, 0, expectedClass);
    }

    /**
     * Decode the given byte array, starting from a given offset, taking the nesting level in account, and convert it to the
     * expected {@link AbstractDataType}. This method validates the object after parsing the raw data if it matches the expected type
     *
     * @param data              The raw bytes to parse
     * @param offset            The offset in the raw data to start parsing from
     * @param level             The nesting level of the current object
     * @param expectedClassType The expected class type
     * @param <T>               The expected type of AXDR object, should extend {@link AbstractDataType}
     * @return The expected AXDR object that extends {@link AbstractDataType}
     * @throws ProtocolException If the given bytes could not be parsed, or the expected class type did not match with the actual type
     */
    public static <T extends AbstractDataType> T decode(byte[] data, int offset, int level, Class<T> expectedClassType) throws ProtocolException {
        final AbstractDataType dataType = decode(data, offset, level);
        if (!expectedClassType.isInstance(dataType)) {
            throw new ProtocolException("Unable to decode raw axdr bytes. Expected [" + expectedClassType.getName() + "] but received [" + dataType.getClass().getName() + "]");
        }
        return (T) dataType;
    }

    public static AbstractDataType decode(byte[] data, int offset, int level) throws ProtocolException {
        try {
            final AxdrType axdrType = AxdrType.fromTag(data[offset]);
            switch (axdrType) {
                case NULL:
                    return new NullData(data, offset);
                case ARRAY:
                    return new Array(data, offset, level);
                case COMPACT_ARRAY:
                    return CompactArrayConverter.fromCompactArray(data, offset, level);
                case STRUCTURE:
                    return new Structure(data, offset, level);
                case INTEGER:
                    return new Integer8(data, offset);
                case LONG:
                    return new Integer16(data, offset);
                case DOUBLE_LONG:
                    return new Integer32(data, offset);
                case UNSIGNED:
                    return new Unsigned8(data, offset);
                case LONG_UNSIGNED:
                    return new Unsigned16(data, offset);
                case ENUM:
                    return new TypeEnum(data, offset);
                case BIT_STRING:
                    return new BitString(data, offset);
                case VISIBLE_STRING:
                    return new VisibleString(data, offset);
                case OCTET_STRING:
                    return new OctetString(data, offset);
                case DOUBLE_LONG_UNSIGNED:
                    return new Unsigned32(data, offset);
                case LONG64:
                    return new Integer64(data, offset);
                case LONG64_UNSIGNED:
                    return new Integer64(DLMSUtils.getUnsignedIntFromBytes(data, offset + 1, Integer64.LENGTH));  //+1 to skip the data type byte
                case BOOLEAN:
                    return new BooleanObject(data, offset);
                case FLOAT32:
                    return new Float32(data, offset);
                default:
                    throw new ProtocolException("AXDRDecoder, unknown datatype " + data[offset]);
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }
}