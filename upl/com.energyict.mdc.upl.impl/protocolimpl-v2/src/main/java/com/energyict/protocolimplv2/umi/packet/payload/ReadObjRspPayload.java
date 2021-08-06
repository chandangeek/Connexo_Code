package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.Limits;

import java.nio.ByteOrder;

public class ReadObjRspPayload extends ReadObjCmdPayload {
    public static final int MIN_SIZE = 8;

    private static final int RESERVED_MAX_SIZE = 2;

    /**
     * Set to 0x00. Ignore on receipt.
     * size = 2 bytes
     */
    private final byte[] reserved;

    /**
     * The length of the value field, in bytes
     * size 2 bytes
     */
    private final int valueLength;

    /**
     * Variable-length object value.
     */
    private final byte[] value;

    public ReadObjRspPayload(IData data) {
        this(data.getRaw());
    }

    public ReadObjRspPayload(byte[] rawPayload) {
        super(rawPayload, MIN_SIZE, true);
        reserved = new byte[RESERVED_MAX_SIZE];

        getRawBuffer().position(UmiCode.SIZE);
        valueLength = getRawBuffer().get(reserved).getShort();

        final int VALUE_LENGTH = getRawBuffer().limit() - getRawBuffer().position();
        value = new byte[VALUE_LENGTH];
        if (VALUE_LENGTH != 0) {
            getRawBuffer().get(value);
        }
    }

    public ReadObjRspPayload(UmiCode code, byte[] value) {
        super(code, MIN_SIZE + value.length);
        if (value.length > Limits.MAX_UNSIGNED_SHORT) {
            throw new java.security.InvalidParameterException(
                    "Invalid value size=" + value.length + ", max size=" + Limits.MAX_UNSIGNED_SHORT
            );
        }
        this.value = value;
        valueLength = value.length;
        reserved = new byte[RESERVED_MAX_SIZE];
        getRawBuffer()
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(reserved)
                .putShort((short) valueLength);

        if (valueLength != 0) {
            getRawBuffer().put(value);
        }
    }

    public byte[] getReserved() {
        return reserved;
    }

    public byte[] getValue() {
        return value;
    }

    public int getValueLength() {
        return valueLength;
    }
}
