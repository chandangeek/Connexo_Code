/*
 * LoadProfileDecompressor.java
 *
 * Created on 9 januari 2008, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.common;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
/**
 *
 * @author kvds
 */
public class LoadProfileDecompressor {
    
    byte[] data;
    TimeZone timeZone;
    private List loadProfileEntries;
    
    /** Creates a new instance of LoadProfileDecompressor */
    public LoadProfileDecompressor(byte[] data, TimeZone timeZone) {
    	if(data != null){
    		this.data = data.clone(); 
    	} else {
    		this.data = null;
    	}
        this.timeZone=timeZone;
    }
    
    public void deCompress() throws IOException {
        int offset = 0;
        offset+=2; // skip structure of 2 elements
        DateTime dateTime = new DateTime(data,offset,timeZone);
        Calendar cal = dateTime.getValue();
        offset+=2; // skip octet string 12
        offset+=12; // skip octet string
        if (data[offset] != AxdrType.COMPACT_ARRAY.getTag()) {
			throw new IOException("LoadProfileDecompressor, invalid identifier for compact array! ("+data[offset]+")");
		}
        offset++; // skip compact array id
        if (data[offset] != 0) {
			throw new IOException("LoadProfileDecompressor, invalid identifier for compact array type descriptor! ("+data[offset]+")");
		}
        offset++; // skip compact array type descriptor identifier 0
        
        offset+=4; // skip struct with 2 implicit NULL elements
        
        if (data[offset] != 1) {
			throw new IOException("LoadProfileDecompressor, invalid identifier for compact array array contents! ("+data[offset]+")");
		}
        offset++; // skip compact array compact array array contents 1
        
        int length = (int)DLMSUtils.getAXDRLength(data,offset);
        offset += DLMSUtils.getAXDRLengthOffset(data,offset);        
        
        setLoadProfileEntries(new ArrayList());
        for (int index=0;index<(length/5);index++) {
            int status = ProtocolUtils.getInt(data,offset++, 1);
            long value = ProtocolUtils.getLong(data,offset, 4);
            offset+=4;
            
            LoadProfileEntry o = null;
            if (index==0) {
				o = new LoadProfileEntry(cal,status,BigDecimal.valueOf(value));
			} else {
				o = new LoadProfileEntry(null,status,BigDecimal.valueOf(value));
			}
            
            //System.out.println(o);
            getLoadProfileEntries().add(o);
        }
        
    }
    
    
    static public void main(String[] args) {
        
        byte[] data = new byte[]{(byte)0x02,(byte)0x02, // structure of datetime and compact array
                                 (byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD8,(byte)0x01,(byte)0x09,(byte)0xFF,(byte)0x0F,(byte)0x2D,(byte)0x00,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x00,
                                 (byte)0x13,  // compact array 
                                    (byte)0x00,(byte)0x02,(byte)0x02,(byte)0x11,(byte)0x06,  //content description as typedescription
                                    (byte)0x01,(byte)0x0F, // Array-contents as octetstring length 3
                                        (byte)0xA4,(byte)0x00,(byte)0x00,(byte)0x33,(byte)0xFD,  // element 1
                                        (byte)0x5,(byte)0x00,(byte)0x00,(byte)0x33,(byte)0xFE,  // element 2
                                        (byte)0xA6,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xFF}; // element 3
                       
                                        
         //$02$02$09$0C$07$D8$01$09$03$13$2D$00$FF$80$00$08$13$00$02$02$11$06$01$05$24$00$0A$8F$D4
                                        
        try {                                
            LoadProfileDecompressor o = new LoadProfileDecompressor(data,TimeZone.getTimeZone("ECT"));
            o.deCompress();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public List getLoadProfileEntries() {
        return loadProfileEntries;
    }

    public void setLoadProfileEntries(List loadProfileEntries) {
        this.loadProfileEntries = loadProfileEntries;
    }
}
