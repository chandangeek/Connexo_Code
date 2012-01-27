package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 23/11/11
 * Time: 16:08
 */
public class NewOctetString extends AbstractDataType {

    private final byte[] value;

    public NewOctetString(byte[] berEncodedData) throws IOException {
        this(berEncodedData, 0);
    }

    public NewOctetString(byte[] berEncodedData, int offset) throws IOException {
        int ptr = offset;
        if (berEncodedData[ptr] != DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING) {
            throw new IOException("OctetString, invalid identifier " + berEncodedData[ptr]);
        }
        int valueLength = DLMSUtils.getAXDRLength(berEncodedData, ++ptr);
        value = new byte[valueLength];
        ptr += DLMSUtils.getAXDRLengthOffset(berEncodedData, ptr);
        System.arraycopy(berEncodedData, ptr, value, 0, valueLength);
    }

    @Override
    protected byte[] doGetBEREncodedByteArray() {
        byte[] axdrLengthEncoding = DLMSUtils.getAXDRLengthEncoding(value.length);
        byte[] berBytes = new byte[1 + axdrLengthEncoding.length + value.length];
        berBytes[0] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
        System.arraycopy(axdrLengthEncoding, 0, berBytes, 1, axdrLengthEncoding.length);
        System.arraycopy(value, 0, berBytes, 1 + axdrLengthEncoding.length, value.length);
        return berBytes;
    }

    @Override
    protected int size() {
        return 1 + DLMSUtils.getAXDRLengthOffset(value.length) + value.length;
    }

    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return null;
    }

    @Override
    public long longValue() {
        return 0;
    }
}
