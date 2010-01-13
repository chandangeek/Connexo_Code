/*
 * BitString.java
 *
 * Created on 16 oktober 2007, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author kvds
 */
public class BitString extends AbstractDataType {

    private long value;
    int size;
    private int offsetBegin,offsetEnd;

    /** Creates a new instance of BitString */
    public BitString(byte[] berEncodedData, int offset) throws IOException {
        offsetBegin = offset;

        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_BITSTRING) {
			throw new IOException("BitString, invalid identifier "+berEncodedData[offset]);
		}

        offset++;
        size = (int)DLMSUtils.getAXDRLength(berEncodedData,offset);
        offset += DLMSUtils.getAXDRLengthOffset(berEncodedData,offset);

        // calc nr of bytes
        if ((size%8) == 0) {
			size = (size/8);
		} else {
			size = ((size/8)+1);
		}

        if (size>8) {
			throw new IOException("BitString, invalid length "+size);
		}

        value=0;
        for (int i=0;i<size;i++) {
			value += (((int)berEncodedData[offset+i]&0xff)<<(i*8));
		}

        offset+=size;

        offsetEnd = offset;
    }

    public String toString() {


        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuff.append("  ");
		}
        return strBuff.toString()+"BitString=0x"+Long.toHexString(value)+"\n";
    }

    public BitString(long value) {
        this(value,64);
    }

    public BitString(int value) {
        this(value,32);
    }


    public BitString(long value, int size) {
        this.value=value;
        this.size=size;
    }

    protected byte[] doGetBEREncodedByteArray() {

        byte[] encodedLength = DLMSUtils.getAXDRLengthEncoding(size);
        byte[] data = new byte[(size/8)+((size%8)>0?1:0)+1+encodedLength.length];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_BITSTRING;
        for (int i=0;i<encodedLength.length;i++) {
			data[1+i]=encodedLength[i];
		}
        for (int i=0;i<(data.length-(1+encodedLength.length));i++) {
			data[(data.length-1)-i] = (byte)(value>>(8*i));
		}
        return data;
    }

    protected int size() {
        return offsetEnd-offsetBegin;
    }

    static public void main(String[]  artgs) {
        try {
           BitString v = new BitString(new byte[]{0x04,0x03,(byte)0xff,(byte)'B',(byte)'C'}, 0);
           System.out.println(v);

           v = new BitString(0x55aa55aa, 32);
           System.out.println(ProtocolUtils.outputHexString(v.getBEREncodedByteArray()));
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal( value );
    }

    public int intValue() {
        return (int)(value);
    }

    public long longValue() {
        return value;
    }
}
