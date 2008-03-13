/*
 * ValueInformationBlock.java
 *
 * Created on 3 oktober 2007, 15:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocol.*;
import java.util.*;
import java.io.*;


/**
 *
 * @author kvds
 */
public class ValueInformationBlock {

    private ValueInformationfieldCoding valueInformationfieldCoding; // VIF
    private List valueInformationfieldCodings; // VIFEs
    private String plainTextVIF;
    int padding=0;
    /** Creates a new instance of ValueInformationBlock */
    public ValueInformationBlock(byte[] data, int offset, TimeZone timeZone, int dataField) throws IOException {
        // plain text VIF
        if ((data[offset] == 0x7C) || (((int)data[offset]&0xff) == 0xFC)) {
            offset++;
            int len = data[offset++];
            setPlainTextVIF(new String(ProtocolUtils.getSubArray2(data, offset, len)));
            offset+=len;
        }        
        else if (((int)data[offset]&0xff) == 0xFB) {
            offset++;
            padding++;
            setValueInformationfieldCoding(ValueInformationfieldCoding.findFBExtensionValueInformationfieldCoding(data[offset++],dataField));
        }
        else if (((int)data[offset]&0xff) == 0xFD) {
            offset++;
            padding++;
            setValueInformationfieldCoding(ValueInformationfieldCoding.findFDExtensionValueInformationfieldCoding(data[offset++],dataField));
        }
        else {
            setValueInformationfieldCoding(ValueInformationfieldCoding.findPrimaryValueInformationfieldCoding(data[offset++],dataField));
        }
        
        if (getValueInformationfieldCoding().isCodingExtended()) {
            setValueInformationfieldCodings(new ArrayList());
            ValueInformationfieldCoding v=null;
            do {
                v = ValueInformationfieldCoding.findCombinableExtensionValueInformationfieldCoding(data[offset++],dataField);
                getValueInformationfieldCodings().add(v);
            } while(v.isCodingExtended());
        }
        
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ValueInformationBlock:\n");
        strBuff.append("   valueInformationfieldCoding="+getValueInformationfieldCoding()+"\n");
        strBuff.append("   valueInformationfieldCodings="+getValueInformationfieldCodings()+"\n");
        strBuff.append("   plainTextVIF="+getPlainTextVIF()+"\n");
        return strBuff.toString();
    }        
    
    public int size() {
        if (getValueInformationfieldCodings()==null) {
            return 1+(getPlainTextVIF()==null?0:getPlainTextVIF().length()+1)+padding;
        }
        else {
            return 1+getValueInformationfieldCodings().size()+padding;
        }
    }

    public ValueInformationfieldCoding getValueInformationfieldCoding() {
        return valueInformationfieldCoding;
    }

    public void setValueInformationfieldCoding(ValueInformationfieldCoding valueInformationfieldCoding) {
        this.valueInformationfieldCoding = valueInformationfieldCoding;
    }

    public List getValueInformationfieldCodings() {
        return valueInformationfieldCodings;
    }

    public void setValueInformationfieldCodings(List valueInformationfieldCodings) {
        this.valueInformationfieldCodings = valueInformationfieldCodings;
    }

    public String getPlainTextVIF() {
        return plainTextVIF;
    }

    public void setPlainTextVIF(String plainTextVIF) {
        this.plainTextVIF = plainTextVIF;
    }


}
