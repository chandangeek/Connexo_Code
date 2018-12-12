package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Created by iulian on 4/8/2017.
 */
public class UTF8String extends AbstractDataType {

    private byte[] utfStr;
    private int size;
    private int offsetBegin, offsetEnd;
    private boolean fixed;

    /**
     * Creates a new instance of UTF8String from the raw BER encoded bytes
     *
     * @param berEncodedData the raw BER encoded byte array
     * @param offset         The offset in the BER bytes
     * @throws IOException If the berEncoded data is not an UTF8String
     */
    public UTF8String(byte[] berEncodedData, int offset) throws IOException {
        int workingOffset = offset;
        offsetBegin = workingOffset;
        if (berEncodedData[workingOffset] != AxdrType.UTF8_STRING.getTag()) {
            throw new ProtocolException("UTF8String, invalid identifier " + berEncodedData[workingOffset]);
        }
        workingOffset++;
        size = (int) DLMSUtils.getAXDRLength(berEncodedData, workingOffset);
        workingOffset += DLMSUtils.getAXDRLengthOffset(berEncodedData, workingOffset);
        utfStr = ProtocolUtils.getSubArray2(berEncodedData, workingOffset, size);
        workingOffset += size;
        offsetEnd = workingOffset;
        this.fixed = false;
    }

    /**
     * Creates a new instance of a fixed length UTF8String from the raw BER encoded bytes
     *
     * @param berEncodedData the raw BER encoded byte array
     * @param offset         The offset in the BER bytes
     * @param fixed          The length of the UTF8String
     * @throws IOException If the berEncoded data is not an UTF8String
     */
    public UTF8String(byte[] berEncodedData, int offset, boolean fixed) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != AxdrType.UTF8_STRING.getTag()) {
            throw new ProtocolException("UTF8String, invalid identifier " + berEncodedData[offset]);
        }
        size = berEncodedData.length - 1;
        offset += DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);
        utfStr = ProtocolUtils.getSubArray2(berEncodedData, offset, size);
        offset += size;
        offsetEnd = offset;
        this.fixed = fixed;
    }

    /**
     * Create a variable length UTF8String
     *
     * @param utfStr
     */
    public UTF8String(byte[] utfStr) {
        this(utfStr, utfStr.length, 0);
    }

    /**
     * It is possible to create a fixed length UTF8String
     *
     * @param utfStr
     * @param fixed  a boolean to indicate whether it is fixed or not
     */
    public UTF8String(byte[] utfStr, boolean fixed) {
        this(utfStr, utfStr.length, (fixed ? 1 : 0));
    }

    protected UTF8String(byte[] utfStr, int size, int dummy) {
        this.setUtfStr(utfStr);
        this.size = size;
        this.fixed = (dummy == 1);
        this.offsetBegin = 0;
        this.offsetEnd = size + (fixed ? 1 : 2);
    }

    public String stringValue() {
        return new String(getUtfStr());
    }

    protected byte[] doGetBEREncodedByteArray() {

        byte[] encodedLength;
        if (this.fixed) {
            encodedLength = new byte[0];
        } else {
            encodedLength = DLMSUtils.getAXDRLengthEncoding(size);
        }
        byte[] data = new byte[size + 1 + encodedLength.length];
        data[0] = AxdrType.UTF8_STRING.getTag();
        for (int i = 0; i < encodedLength.length; i++) {
            data[1 + i] = encodedLength[i];
        }
        for (int i = 0; i < (data.length - (1 + encodedLength.length)); i++) {
            if (i < getUtfStr().length) {
                data[(1 + encodedLength.length) + i] = getUtfStr()[i];
            } else {
                data[(1 + encodedLength.length) + i] = 0;
            }
        }
        return data;
    }

    protected int size() {
        return offsetEnd - offsetBegin;
    }

    public byte[] toByteArray() {
        return getUtfStr();
    }

    public byte[] getUtfStr() {
        return utfStr;
    }

    public void setUtfStr(byte[] utfStr) {
        this.utfStr = utfStr.clone();
    }

    public BigDecimal toBigDecimal() {
        return null;
    }

    public int intValue() {
        return -1;
    }

    public long longValue() {
        return -1;
    }

    public DateTime getDateTime(TimeZone tz) {
        return null;
    }

    /**
     * Create a new UTF8String from a given String.
     * If the string is 'null' you'll get an empty string.
     *
     * @param string The String that should be used to construct the UTF8String
     * @return The new com.energyict.dlms.axrdencoding.UTF8String
     */
    public static UTF8String fromString(String string) {
        if (string == null) {
            return UTF8String.fromByteArray(new byte[0]);
        } else {
            return UTF8String.fromByteArray(string.getBytes());
        }
    }

    /**
     * Create a new UTF8String from a given String.
     * If the string is 'null' you'll get an empty string.
     *
     * @param string The String that should be used to construct the UTF8String
     * @param size
     * @return The new com.energyict.dlms.axrdencoding.UTF8String
     */
    public static UTF8String fromString(String string, int size) {
        return new UTF8String(string.getBytes(), size, 0);
    }

    /**
     * @param string
     * @param size
     * @param fixed
     * @return
     */
    public static UTF8String fromString(String string, int size, boolean fixed) {
        return new UTF8String(string.getBytes(), size, (fixed ? 1 : 0));
    }


    /**
     * Create a new UTF8String with the given bytes as content,
     * from contentBytes[0] up to contentBytes[length-1]
     * <p/>
     * Ex: 01020304050607, with length 4 -> 090401020304
     *
     * @param contentBytes The raw bytes as content of the UTF8String
     * @param length       The number of bytes to use from the contentBytes.
     * @return new UTF8String with the given bytes as content
     */
    public static UTF8String fromByteArray(byte[] contentBytes, int length) {
        return new UTF8String(contentBytes, length, 0);
    }

    /**
     * Create a new UTF8String with the given bytes as content
     *
     * @param contentBytes The raw bytes as content of the UTF8String
     * @return new UTF8String with the given bytes as content
     */
    public static UTF8String fromByteArray(byte[] contentBytes) {
        return UTF8String.fromByteArray(contentBytes, contentBytes.length);
    }


    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i = 0; i < getLevel(); i++) {
            strBuffTab.append("  ");
        }
        return strBuffTab.toString() + "UTF8String=" + ProtocolUtils.outputHexString(getUtfStr()) + "\n";
    }

}
