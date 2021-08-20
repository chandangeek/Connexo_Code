package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidParameterException;
import java.util.Objects;

public class UmiId extends LittleEndianData {
    public static final BigInteger MIN_UMI_ID = new BigInteger("0");
    public static final BigInteger MAX_UMI_ID = new BigInteger("18446744073709551615");
    public static int SIZE = 8;

    private BigInteger id;

    public UmiId(String id) {
        this(new BigInteger(id));
    }

    public UmiId(BigInteger id) {
        super(SIZE);
        this.id = id;
        if (this.id.compareTo(MIN_UMI_ID) < 0 || this.id.compareTo(MAX_UMI_ID) == 1)
            throw new InvalidParameterException("Invalid UMI ID string representation passed=" + id +
                    ", required min=" + MIN_UMI_ID + " max=" + MAX_UMI_ID);
        getRawBuffer().putLong(this.id.longValue());
    }

    public UmiId(byte[] rawUmiId, boolean littleEndian) {
        super(rawUmiId);
        if (!littleEndian) {
            byte[] clone = rawUmiId.clone();
            ArrayUtils.reverse(clone);
            setRaw(clone);
        }

        ByteBuffer temp = ByteBuffer.allocate(SIZE + 1).order(ByteOrder.BIG_ENDIAN);
        long value = getRawBuffer().getLong();
        temp.putLong(1, value);

        this.id = new BigInteger(temp.array());
        if (this.id.compareTo(MIN_UMI_ID) < 0 || this.id.compareTo(MAX_UMI_ID) == 1)
            throw new InvalidParameterException("Invalid raw UMI ID passed=" + id +
                    ", required min=" + MIN_UMI_ID + " max=" + MAX_UMI_ID);
    }

    public BigInteger getId() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UmiId)) return false;
        if (!super.equals(o)) return false;
        UmiId umiId = (UmiId) o;
        return id.equals(umiId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
