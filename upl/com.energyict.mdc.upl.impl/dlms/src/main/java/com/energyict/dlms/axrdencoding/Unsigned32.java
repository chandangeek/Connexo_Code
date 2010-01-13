package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.ProtocolUtils;

public class Unsigned32 extends AbstractDataType {

    private long value;

    /** Creates a new instance of Enum */
    public Unsigned32(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED) {
			throw new IOException("Unsigned32, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        setValue(ProtocolUtils.getLong(berEncodedData,offset,4));
        offset+=4;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"Unsigned32="+getValue()+"\n";
    }

    public Unsigned32(long value) {
        this.value=value;
    }

    public Unsigned32(int value) {
        this.value=value;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[5];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED;
        data[1] = (byte)(getValue() >> 24 );
        data[2] = (byte)(getValue() >> 16 );
        data[3] = (byte)(getValue() >> 8 );
        data[4] = (byte)(getValue() );
        return data;
    }

    protected int size() {
        return 5;
    }

    static public void main(String[]  artgs) {
        try {
            Unsigned32 v = new Unsigned32(new byte[]{6,0,0,0,1}, 0);
           System.out.println(v);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal( value );
    }

    public int intValue() {
        return (int)value;
    }

    public long longValue() {
        return value;
    }
}
