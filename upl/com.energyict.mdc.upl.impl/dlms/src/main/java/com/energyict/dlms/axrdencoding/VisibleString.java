/*
 * VisibleString.java
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
public class VisibleString extends AbstractDataType {
    
    private String str;
    int size;
    private int offsetBegin,offsetEnd;
            
    /** Creates a new instance of VisibleString */
    public VisibleString(byte[] berEncodedData, int offset) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_VISIBLE_STRING)
            throw new IOException("VisibleString, invalid identifier "+berEncodedData[offset]);
        offset++;
        size = (int)DLMSUtils.getAXDRLength(berEncodedData,offset);
        offset += DLMSUtils.getAXDRLengthOffset(berEncodedData,offset);
        
        setStr(new String(ProtocolUtils.getSubArray2(berEncodedData,offset, size)));
        offset+=size;
        offsetEnd = offset;
    }
    
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) 
            strBuffTab.append("  ");
        return strBuffTab.toString()+"VisibleString="+getStr()+"\n";
    }
    
    public VisibleString(String str) {
        this(str,str.length());
    }
    public VisibleString(String str, int size) {
        this.setStr(str);
        this.size=size;
    }
    
    protected byte[] doGetBEREncodedByteArray() throws IOException {
        byte[] encodedLength = DLMSUtils.getAXDRLengthEncoding(size);
        byte[] data = new byte[size+1+encodedLength.length];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_VISIBLE_STRING;
        for (int i=0;i<encodedLength.length;i++)
            data[1+i]=encodedLength[i];
        byte[] strArray = getStr().getBytes();
        for (int i=0;i<(data.length-(1+encodedLength.length));i++)
            data[(1+encodedLength.length)+i] = 0x20;
        for (int i=0;i<strArray.length;i++)
            data[(1+encodedLength.length)+i] = strArray[i];
        return data;
    }
    
    protected int size() {
        return offsetEnd-offsetBegin;
    }
    
    static public void main(String[]  artgs) {
        try {
           VisibleString v = new VisibleString(new byte[]{0x0a,0x03,(byte)'A',(byte)'B',(byte)'C'}, 0);
           System.out.println(v);
           v = new VisibleString("LGZ93977708",20);
           System.out.println(ProtocolUtils.outputHexString(v.getBEREncodedByteArray()));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getStr() {
        return str;
    }

    private void setStr(String str) {
        this.str = str;
    }
    
    public BigDecimal toBigDecimal() {
        return new BigDecimal( str );
    }
    
    public int intValue() {
        return -1;
    }
    
    public long longValue() {
        return -1;
    }        
}
