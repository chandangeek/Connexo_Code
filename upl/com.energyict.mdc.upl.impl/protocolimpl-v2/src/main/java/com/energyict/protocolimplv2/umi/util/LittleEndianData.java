package com.energyict.protocolimplv2.umi.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for working with UMI protocol data.
 */
public class LittleEndianData implements IData {
    private ByteBuffer raw;

    public LittleEndianData(int size) {
        setRawBuffer(ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN));
    }

    public LittleEndianData(byte[] raw) {
        setRaw(raw);
    }

    public LittleEndianData(IData data) {
        this(data.getRaw());
    }

    public LittleEndianData(byte[] raw, int offset, int length) {
        setRaw(raw, offset, length);
    }

    public LittleEndianData(byte[] raw, int size, boolean variableSize) {
        this(raw);
        if ((variableSize && raw.length < size) || (!variableSize && raw.length != size)) {
            throw new java.security.InvalidParameterException(
                    "Invalid raw data size=" + raw.length + ", " + (variableSize ? "min" : "") + " required size=" + size
            );
        }
    }

    public LittleEndianData(ByteBuffer raw) {
        setRawBuffer(raw);
    }

    @Override
    public byte[] getRaw() {
        return raw.array();
    }

    @Override
    public int getLength() {
        return raw.capacity();
    }

    public ByteBuffer getRawBuffer() {
        return raw;
    }

    protected void setRaw(byte[] raw) {
        this.raw = ByteBuffer.wrap(raw.clone()).order(ByteOrder.LITTLE_ENDIAN);
    }

    protected void setRaw(byte[] raw, int offset, int length) {
        if (raw != null) {
            this.raw = ByteBuffer.wrap(raw.clone(), offset, length).order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    protected void setRawBuffer(ByteBuffer rawPayload) {
        this.raw = rawPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LittleEndianData)) return false;
        LittleEndianData data = (LittleEndianData) o;
        return Arrays.equals(raw.array(), data.raw.array());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(raw.array());
    }
}
