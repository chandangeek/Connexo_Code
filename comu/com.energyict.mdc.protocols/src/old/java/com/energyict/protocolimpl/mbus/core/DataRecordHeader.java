/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataRecordHeader.java
 *
 * Created on 3 oktober 2007, 12:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;


import java.io.IOException;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class DataRecordHeader {
    
    private DataInformationBlock dataInformationBlock;
    private ValueInformationBlock valueInformationBlock;
    
    /** Creates a new instance of DataRecordHeader */
    public DataRecordHeader(byte[] data, int offset, TimeZone timeZone) throws IOException {
        setDataInformationBlock(new DataInformationBlock(data, offset, timeZone));
        offset+=getDataInformationBlock().size();
        
        // We could leave out this condition !isTYPE_NODATA() but then, there is a VIF that does not contain any data!
        // Therefor, it is better not to include an "empty" VIF and just skip that Data Record Header.
        // In the CIField72 class, an offset skip of 1 byte is added to skip the VIF! in case of isTYPE_NODATA()
        if (!dataInformationBlock.getDataInformationfield().getDataFieldCoding().isTYPE_NODATA()) {
            if (dataInformationBlock.getDataInformationfield().getDataFieldCoding().isTYPE_SPECIALFUNCTIONS()) {
                valueInformationBlock = null;
            }
            else {
                setValueInformationBlock(new ValueInformationBlock(data, offset, timeZone, dataInformationBlock.getDataInformationfield().getDataFieldCoding().getId()));
                offset+=getValueInformationBlock().size();
            }
        }
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataRecordHeader:\n");
        strBuff.append("   dataInformationBlock="+getDataInformationBlock()+"\n");
        strBuff.append("   valueInformationBlock="+getValueInformationBlock()+"\n");
        return strBuff.toString();
    }    
    
    public int size() {
        
        return (getValueInformationBlock()==null?0:getValueInformationBlock().size())+
                (getDataInformationBlock()==null?0:getDataInformationBlock().size());
    }

    public DataInformationBlock getDataInformationBlock() {
        return dataInformationBlock;
    }

    public void setDataInformationBlock(DataInformationBlock dataInformationBlock) {
        this.dataInformationBlock = dataInformationBlock;
    }

    public ValueInformationBlock getValueInformationBlock() {
        return valueInformationBlock;
    }

    public void setValueInformationBlock(ValueInformationBlock valueInformationBlock) {
        this.valueInformationBlock = valueInformationBlock;
    }
}
