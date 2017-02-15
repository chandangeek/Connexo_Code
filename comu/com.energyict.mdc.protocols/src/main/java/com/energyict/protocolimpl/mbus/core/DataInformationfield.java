/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataInformationfield.java
 *
 * Created on 3 oktober 2007, 12:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class DataInformationfield {
    
    
    
    private boolean extension;
    private boolean lsbStorageNumber;
    private int functionField;
    private DataFieldCoding dataFieldCoding;
    
    String[] strFunctions=new String[]{"instantaneous value","maximum value","minimum value","value during error state"};
    
    /** Creates a new instance of DataInformationfield */
    public DataInformationfield(int data) throws IOException {
        setExtension((data & 0x80) == 0x80);
        setLsbStorageNumber((data & 0x40) == 0x40);
        setFunctionField((data & 0x30) >> 4);
        setDataFieldCoding(DataFieldCoding.findDataFieldCoding(data & 0x0F));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataInformationfield:\n");
        strBuff.append("   dataFieldCoding="+getDataFieldCoding()+"\n");
        strBuff.append("   extension="+isExtension()+"\n");
        strBuff.append("   functionField="+getFunctionField()+"\n");
        strBuff.append("   lsbStorageNumber="+isLsbStorageNumber()+"\n");
        return strBuff.toString();
    }
    

    public String getFunctionDescription() {
        return strFunctions[getFunctionField()];
    }
    
    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public boolean isLsbStorageNumber() {
        return lsbStorageNumber;
    }

    public void setLsbStorageNumber(boolean lsbStorageNumber) {
        this.lsbStorageNumber = lsbStorageNumber;
    }

    public int getFunctionField() {
        return functionField;
    }

    public void setFunctionField(int functionField) {
        this.functionField = functionField;
    }

    public DataFieldCoding getDataFieldCoding() {
        return dataFieldCoding;
    }

    public void setDataFieldCoding(DataFieldCoding dataFieldCoding) {
        this.dataFieldCoding = dataFieldCoding;
    }


    
}
