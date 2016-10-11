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

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author kvds
 */
public class ValueInformationBlock {

    private ValueInformationfieldCoding valueInformationfieldCoding; // VIF
    private List valueInformationfieldCodings; // VIFEs
    private String plainTextVIF;
    private final Logger logger;
    int padding=0;
    /** Creates a new instance of ValueInformationBlock */
    public ValueInformationBlock(byte[] data, int offset, TimeZone timeZone, int dataField, final Logger logger) throws IOException {
        this.logger = logger;
        // plain text VIF
        if ((data[offset] == 0x7C) || (((int)data[offset]&0xff) == 0xFC)) {
            offset++;
            int len = data[offset++];
            byte[] plainTextVIF = ProtocolUtils.getSubArray2(data, offset, len);
            setPlainTextVIF(new String(ProtocolTools.getReverseByteArray(plainTextVIF)));
            offset+=len;
        }        
        else if (((int)data[offset]&0xff) == 0xFB) {
            offset++;
            padding++;
            setValueInformationfieldCoding(ValueInformationfieldCoding.findFBExtensionValueInformationfieldCoding((int)data[offset++]&0xff,dataField));
        }
        else if (((int)data[offset]&0xff) == 0xFD) {
            offset++;
            padding++;
            setValueInformationfieldCoding(ValueInformationfieldCoding.findFDExtensionValueInformationfieldCoding((int)data[offset++]&0xff,dataField));
        }
        else if (((int)data[offset]&0xff) == 0xFF) {
        	// manufacturer specific
            offset++;
            padding++;
            setValueInformationfieldCoding(ValueInformationfieldCoding.findCombinableExtensionValueInformationfieldCoding((int)data[offset++]&0xff,dataField));
        } else if (data[offset] == 0x7F) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "[ValueInformationBlock] data [" + offset + "] : " + ParseUtils.asHex(new byte[] {data[offset]}) + "] (manufacturer specific)");
            }
            // manufacturer specific
            // After a VIF with extension bit set to 0, the value information block is closed according the spec
            // It is up to the manufacturer to encode the data
            // The Data information block gives enough information to parse the data that follows
            // We fill in fixed
            setValueInformationfieldCoding(ValueInformationfieldCoding.findCombinableExtensionValueInformationfieldCoding(0x00,dataField));
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "[ValueInformationBlock] manufacturer specific ; dataField [" + dataField + "] [" + this.valueInformationfieldCoding + "]");
            }
        }
        else {
            setValueInformationfieldCoding(ValueInformationfieldCoding.findPrimaryValueInformationfieldCoding((int)data[offset++]&0xff,dataField));
        }
        
        if ((getValueInformationfieldCoding() != null) && getValueInformationfieldCoding().isCodingExtended()) {
            setValueInformationfieldCodings(new ArrayList());
            ValueInformationfieldCoding v=null;
            do {
                v = ValueInformationfieldCoding.findCombinableExtensionValueInformationfieldCoding((int)data[offset++]&0xff,dataField);
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
