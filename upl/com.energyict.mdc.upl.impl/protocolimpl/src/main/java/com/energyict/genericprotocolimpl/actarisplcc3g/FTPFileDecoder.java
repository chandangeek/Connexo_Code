/*
 * FTPFileDecoder.java
 *
 * Created on 9 januari 2008, 17:08
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocol.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author kvds
 */
public class FTPFileDecoder {
    
    
    byte[] data;
    TimeZone timeZone;
            
    /** Creates a new instance of FTPFileDecoder */
    public FTPFileDecoder(byte[] data, TimeZone timeZone) {
        this.data=data;
        this.timeZone=timeZone;
    }
    
    public void decode() throws IOException {
        
        System.out.println(ProtocolUtils.outputHexString(data));
        
        AbstractDataType abstractDataType = AXDRDecoder.decode(data);
        if (abstractDataType instanceof Array) {
            Array array = abstractDataType.getArray();
            //array = 
            
            System.out.println(array);
        }
        else throw new IOException("FTPFileDecoder, invalid abstractDataType!");
//        int offset = 0;
//        if (data[offset] != DLMSCOSEMGlobals.TYPEDESC_ARRAY) 
//            throw new IOException("FTPFileDecoder, invalid identifier for array! ("+data[offset]+")");
//        offset++; // skip array identifier
//        int length = (int)DLMSUtils.getAXDRLength(data,offset);
//        offset += DLMSUtils.getAXDRLengthOffset(data,offset); 
//        if (data[offset] != DLMSCOSEMGlobals.TYPEDESC_STRUCTURE) 
//            throw new IOException("FTPFileDecoder, invalid identifier for structure! ("+data[offset]+")");        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        try {
            System.out.println("*************************************************************************************");
            File file = new File("070798001234_0-0-10-0-130-255_02_20070827-1527121.cos");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            
            FTPFileDecoder o = new FTPFileDecoder(data, TimeZone.getTimeZone("ECT"));
            o.decode();
            
            System.out.println("*************************************************************************************");
            file = new File("070798001234_0-0-10-0-131-255_04_20070827-1527121.cos");
            fis = new FileInputStream(file);
            data = new byte[(int)file.length()];
            fis.read(data);
            
            o = new FTPFileDecoder(data, TimeZone.getTimeZone("ECT"));
            o.decode();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
