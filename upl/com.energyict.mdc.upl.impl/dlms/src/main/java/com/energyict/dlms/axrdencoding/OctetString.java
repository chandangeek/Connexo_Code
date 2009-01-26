/*
 * OctetString.java
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
 * Changes:
 * GNA |26012009| Added and changed some functions to make fixed OctetStrings. Size is not returned in the encodedData
 */
public class OctetString extends AbstractDataType {
    
    private byte[] octetStr;
    int size;
    private int offsetBegin,offsetEnd;
    private boolean fixed;
            
    /** Creates a new instance of OctetString */
    public OctetString(byte[] berEncodedData, int offset) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING)
            throw new IOException("OctetString, invalid identifier "+berEncodedData[offset]);
        offset++;
        size = (int)DLMSUtils.getAXDRLength(berEncodedData,offset);
        offset+=DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);
        setOctetStr(ProtocolUtils.getSubArray2(berEncodedData,offset, size));
        offset+=size;
        offsetEnd = offset;
        this.fixed = false;
    }
    
    /** Create a new instance of a fixed OctetString */
    public OctetString(byte[] berEncodedData, int offset, boolean fixed) throws IOException {
        offsetBegin = offset;
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING)
            throw new IOException("OctetString, invalid identifier "+berEncodedData[offset]);
        size = berEncodedData.length-1;
        offset+=DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);
        setOctetStr(ProtocolUtils.getSubArray2(berEncodedData,offset, size));
        offset+=size;
        offsetEnd = offset;
        this.fixed = fixed;
    }
    
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) 
            strBuffTab.append("  ");
        return strBuffTab.toString()+"OctetString="+ProtocolUtils.outputHexString(getOctetStr())+"\n";
    }
    
    /**
     * Create a variable length OctetString
     * @param octetStr
     */
    public OctetString(byte[] octetStr) {
        this(octetStr,octetStr.length,0);
    }
    /**
     * It is possible to create a fixed length OctetString
     * @param octetStr
     * @param fixed a boolean to indicate whether it is fixed or not
     */
    public OctetString(byte[] octetStr, boolean fixed){
    	this(octetStr, octetStr.length, (fixed?1:0));
    }
    
    static public OctetString fromString(String string) {
    	if (string==null)
    		return new OctetString(new byte[]{});
    	else
    		return new OctetString(string.getBytes());
    }
    static public OctetString fromString(String string, int size) {
        return new OctetString(string.getBytes(), size, 0);
    }
    static public OctetString fromByteArray(byte[] byteArray, int size) {
        return new OctetString(byteArray, size, 0);
    }
    static public OctetString fromString(String string, int size, boolean fixed){
    	return new OctetString(string.getBytes(), size, (fixed?1:0));
    }
    
    private OctetString(byte[] octetStr, int size, int dummy) {
        this.setOctetStr(octetStr);
        this.size=size;
        this.fixed = (dummy == 1);
    }
    
    public String stringValue() {
        return new String(getOctetStr());
    }
    
    protected byte[] doGetBEREncodedByteArray() {
    
    	byte[] encodedLength;
    	if(this.fixed){
    		encodedLength = new byte[0];
    	} else {
    		encodedLength = DLMSUtils.getAXDRLengthEncoding(size);
    	}
        byte[] data = new byte[size+1+encodedLength.length];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
        for (int i=0;i<encodedLength.length;i++)
            data[1+i]=encodedLength[i];
        for (int i=0;i<(data.length-(1+encodedLength.length));i++) {
            if (i < getOctetStr().length)
                data[(1+encodedLength.length)+i] = getOctetStr()[i];
            else
                data[(1+encodedLength.length)+i] = 0;
        }
        return data;
    }
    
    protected int size() {
        return offsetEnd-offsetBegin;
    }
    
    static public void main(String[]  artgs) {
        try {
           OctetString v = new OctetString(new byte[]{DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING,0x03,(byte)'A',(byte)'B',(byte)'C'}, 0);
           System.out.println(v);
           v = new OctetString(new byte[]{1,2,3,4,5,6},6, 0);
           System.out.println(ProtocolUtils.outputHexString(v.getBEREncodedByteArray()));
           v = new OctetString(new byte[]{1,2,3,4,5,6}, 6, 1);
           System.out.println(ProtocolUtils.outputHexString(v.getBEREncodedByteArray()));
           v = new OctetString(new byte[]{DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING,1,2,3,4,5,6}, 0, true);
           System.out.println(ProtocolUtils.outputHexString(v.getBEREncodedByteArray()));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
                
    }

    public byte[] getOctetStr() {
        return octetStr;
    }

    public void setOctetStr(byte[] octetStr) {
        this.octetStr = octetStr;
    }
    
    public BigDecimal toBigDecimal() {
        BigDecimal result = new BigDecimal( 0 );
        for( int i = 0; i < octetStr.length; i ++ ) {
            int idx = octetStr.length - i - 1;
            BigDecimal temp = new BigDecimal( octetStr[idx] ).movePointRight(i);
            result = result.add( temp );
        
        }
        return result;
    }
    
    public int intValue() {
        return -1;
    }
    
    public long longValue() {
        return -1;
    }        
}
