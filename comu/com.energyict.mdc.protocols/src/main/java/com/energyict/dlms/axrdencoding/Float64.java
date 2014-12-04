package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * Copyrights EnergyICT
 * Date: 20-aug-2010
 * Time: 9:23:49
 */
public class Float64 extends AbstractDataType {

    private static final int SIZE = 9;
    private double value;

    public Float64(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != AxdrType.FLOAT64.getTag()) {
            throw new IOException("Float64, invalid identifier " + berEncodedData[offset]);
        }
        offset++;
        value = ByteBuffer.wrap(berEncodedData, offset, SIZE - 1).getDouble();
    }

    public Float64(double value) {
        this.value = value;
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        byte[] result = new byte[SIZE];
        ByteBuffer.wrap(result).put(AxdrType.FLOAT64.getTag()).putDouble(value);
        return result;
    }

    @Override
    protected int size() {
        return SIZE;
    }

    @Override
    public int intValue() {
        return new Float(value).intValue();
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(value);
    }

    @Override
    public long longValue() {
        return new Float(value).longValue();
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder strBuffTab = new StringBuilder();
        for (int i = 0; i < getLevel(); i++) {
            strBuffTab.append("  ");
        }
        return strBuffTab.toString() + "Float64=" + getValue() + "\n";
    }
}