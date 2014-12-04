package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 20-aug-2010
 * Time: 9:23:49
 */
public class Float32 extends AbstractDataType {

    private static final int SIZE = 5;
    private float value;

    public Float32(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != AxdrType.FLOAT32.getTag()) {
			throw new ProtocolException("Float32, invalid identifier "+berEncodedData[offset]);
		}
        int intBits = ProtocolUtils.getInt(berEncodedData, offset + 1, 4);
        this.value = Float.intBitsToFloat(intBits);
    }

    public Float32(float value) {
        this.value = new Float(value);
    }

    public Float32(Float value) {
        this.value = value;
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        int intBits = Float.floatToIntBits(value);
        byte[] data = new byte[SIZE];
        data[0] = AxdrType.FLOAT32.getTag();
        data[1] = (byte) ((intBits >> 24) & 0x0FF);
        data[2] = (byte) ((intBits >> 16) & 0x0FF);
        data[3] = (byte) ((intBits >> 8) & 0x0FF);
        data[4] = (byte) ((intBits >> 0) & 0x0FF);
        return data;
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"Float32="+getValue()+"\n";
    }
}
