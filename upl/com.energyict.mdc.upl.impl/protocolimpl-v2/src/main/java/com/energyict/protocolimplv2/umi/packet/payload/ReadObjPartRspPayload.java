package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

public class ReadObjPartRspPayload extends LittleEndianData {
    public static final int MIN_SIZE = 12;

    private final UmiObjectPart objectPart;

    /**
     * Set to 0x0. Ignore on receipt.
     * size = 1 byte
     */
    private final byte reserved;

    /**
     * The length of the value field, in bytes
     * size = 2 bytes
     */
    private final int valueLength;

    /**
     * Variable-length data representing the member(s) for the array elements specified.
     */
    private final byte[] value;

    public ReadObjPartRspPayload(UmiObjectPart objectPart, byte[] value) {
        super(ReadObjPartRspPayload.MIN_SIZE + value.length);
        this.objectPart = objectPart;
        this.value = value;
        this.valueLength = value.length;
        reserved = 0x0;
        getRawBuffer().put(this.objectPart.getRaw());
        getRawBuffer().put(this.reserved);
        getRawBuffer().putShort((short) this.valueLength);
        if (this.valueLength != 0) getRawBuffer().put(this.value);
    }

    public ReadObjPartRspPayload(IData data) {
        this(data.getRaw());
    }

    public ReadObjPartRspPayload(byte[] rawPayload) {
        super(rawPayload, MIN_SIZE, true);

        byte[] rawObjectPart = new byte[UmiObjectPart.SIZE];
        getRawBuffer().get(rawObjectPart);
        this.objectPart = new UmiObjectPart(rawObjectPart);
        this.reserved = getRawBuffer().get();
        this.valueLength = getRawBuffer().getShort();
        this.value = new byte[rawPayload.length - MIN_SIZE];
        if (this.valueLength != 0) getRawBuffer().get(this.value);
    }

    public byte[] getValue() {
        return value;
    }

    public UmiObjectPart getObjectPart() {
        return objectPart;
    }

    public int getValueLength() {
        return valueLength;
    }

    public byte getReserved() {
        return reserved;
    }
}
