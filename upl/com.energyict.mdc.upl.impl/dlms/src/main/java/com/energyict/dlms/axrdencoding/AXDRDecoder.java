/*
 * AXDRDecoder.java
 *
 * Created on 17 oktober 2007, 15:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import java.io.IOException;

import com.energyict.dlms.DLMSCOSEMGlobals;

/**
 *
 * @author kvds
 */
public class AXDRDecoder {
    
    /** Creates a new instance of AXDRDecoder */
    public AXDRDecoder() {
        
    }
    
    static public AbstractDataType decode(byte[] data) throws IOException {
    	return decode(data,0,0);
    }
    static public AbstractDataType decode(byte[] data,int offset) throws IOException {
        AbstractDataType adt = decode(data,offset,0);
        return adt;
    }
    
    static protected AbstractDataType decode(byte[] data,int offset, int level) throws IOException {
        switch(data[offset]) {
            case DLMSCOSEMGlobals.TYPEDESC_NULL:
               return new NullData(data,offset);
            case DLMSCOSEMGlobals.TYPEDESC_ARRAY:
               return new Array(data,offset,level);
            case DLMSCOSEMGlobals.TYPEDESC_STRUCTURE:
               return new Structure(data,offset,level);
            case DLMSCOSEMGlobals.TYPEDESC_INTEGER:
               return new Integer8(data,offset);
            case DLMSCOSEMGlobals.TYPEDESC_LONG:
               return new Integer16(data,offset);
            case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG:
                return new Integer32(data,offset);
            case DLMSCOSEMGlobals.TYPEDESC_UNSIGNED:
               return new Unsigned8(data,offset); 
            case DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED:
               return new Unsigned16(data,offset); 
            case DLMSCOSEMGlobals.TYPEDESC_ENUM:
               return new TypeEnum(data,offset); 
            case DLMSCOSEMGlobals.TYPEDESC_BITSTRING:
               return new BitString(data,offset); 
            case DLMSCOSEMGlobals.TYPEDESC_VISIBLE_STRING:
               return new VisibleString(data,offset); 
            case DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING:
               return new OctetString(data,offset);
            case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED:
                return new Unsigned32(data,offset);
            case DLMSCOSEMGlobals.TYPEDESC_LONG64:
                return new Integer64(data, offset);
            case DLMSCOSEMGlobals.TYPEDESC_BOOLEAN:
            	return new BooleanObject(data, offset);
                
        } // switch(data[offset])
        throw new IOException("AXDRDecoder, unknown datatype "+data[offset]);
    } // static public AbstractDataType decode(byte[] data,int offset) throws IOException
    
}
