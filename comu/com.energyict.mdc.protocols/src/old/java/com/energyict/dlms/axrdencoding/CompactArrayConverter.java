/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSUtils;

import java.io.IOException;

public class CompactArrayConverter {

    /**
     * Static util class for {@link CompactArrayConverter#fromCompactArray(byte[], int, int)}
     */
    private CompactArrayConverter() {
    }

    /**
     * Convert a givan AXDR encoded CompactArray to a {@link Array} object
     *
     * @param axdrBytes The raw AXDR encoded bytes, containing the CompactArray
     * @param offset    The offset of the CompactArray tag (0x19)
     * @param level     The level of the created array
     * @return The new Array
     * @throws IOException
     */
    public static final Array fromCompactArray(byte[] axdrBytes, int offset, int level) throws IOException {
        final Array array = new Array();
        array.setLevel(level);

        int ptr = offset;
        byte typeTag = axdrBytes[ptr++];
        if (AxdrType.fromTag(typeTag) != AxdrType.COMPACT_ARRAY) {
            throw new ProtocolException("Expected CompactArray identifier [" + AxdrType.COMPACT_ARRAY.getTag() + "] but found [" + typeTag + "].");
        }
        final TypeDescription typeDescription = new TypeDescription(axdrBytes, ptr);
        ptr += typeDescription.getLength();
        final byte[] data = AXDRDecoder.decode(axdrBytes, ptr, OctetString.class).getOctetStr();

        int rawDataPtr = 0;
        while (rawDataPtr < data.length) {
            final AbstractDataType type = typeDescription.getDataType(data, rawDataPtr);
            rawDataPtr += type.doGetBEREncodedByteArray().length - typeDescription.getLength();
            array.addDataType(type);
        }
        return array;
    }

    private static class TypeDescription {

        private final byte typeTag;
        private final int numberOfElements;
        private final TypeDescription[] typeDescriptions;

        public TypeDescription(byte[] axdrBytes, int offset) throws IOException {
            int ptr = offset;
            this.typeTag = axdrBytes[ptr++];
            switch (this.typeTag) {
                case 0x01:
                    this.numberOfElements = new Unsigned16(axdrBytes, ptr++).getValue();
                    ptr += 2;
                    this.typeDescriptions = new TypeDescription[1];
                    this.typeDescriptions[0] = new TypeDescription(axdrBytes, ptr);
                    ptr += typeDescriptions[0].getLength();
                    break;
                case 0x02:
                    this.numberOfElements = ((int) axdrBytes[ptr++]) & 0x0FF;
                    this.typeDescriptions = new TypeDescription[numberOfElements];
                    for (int i = 0; i < typeDescriptions.length; i++) {
                        typeDescriptions[i] = new TypeDescription(axdrBytes, ptr);
                        ptr += typeDescriptions[i].getLength();
                    }
                    break;
                default:
                    this.numberOfElements = 0;
                    this.typeDescriptions = new TypeDescription[0];
            }
        }

        /**
         * Returns the actual number of bytes this type descriptor takes
         *
         * @return
         */
        public int getLength() {
            switch (typeTag) {
                case 0x01:
                    return 4 + typeDescriptions[0].getLength();
                case 0x02:
                    int length = 0;
                    for (TypeDescription typeDescription : typeDescriptions) {
                        length += typeDescription.getLength();
                    }
                    return 2 + length;
                default:
                    return 1;
            }
        }

        public AbstractDataType getDataType(byte[] rawCompactData, int offset) throws IOException {
            int ptr = offset;
            switch (typeTag) {
                case 0x01:
                case 0x02:
                    final Structure structure = new Structure();
                    for (TypeDescription description : typeDescriptions) {
                        final AbstractDataType type = description.getDataType(rawCompactData, ptr);
                        ptr += type.size() - description.getLength();
                        structure.addDataType(type);
                    }
                    return structure;
                default:
                    final byte[] tag = {typeTag};
                    final byte[] subArray = DLMSUtils.getSubArray(rawCompactData, ptr);
                    final byte[] axdrBytes = DLMSUtils.concatByteArrays(tag, subArray);
                    return AXDRDecoder.decode(axdrBytes);
            }
        }

    }

}