/*
 * DataInformationfieldExtension.java
 *
 * Created on 3 oktober 2007, 12:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

/**
 *
 * @author kvds
 */
public class DataInformationfieldExtension {
    
    private boolean extension;
    private boolean unit;
    private int tariff;
    private int storageNumber;
    
    /** Creates a new instance of DataInformationfieldExtension */
    public DataInformationfieldExtension(int data) {
        setExtension((data & 0x80) == 0x80);
        setUnit((data & 0x40) == 0x40);
        setTariff((data & 0x30) >> 4);
        setStorageNumber(data & 0x0F);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataInformationfieldExtension:\n");
        strBuff.append("   extension="+isExtension()+"\n");
        strBuff.append("   storageNumber="+getStorageNumber()+"\n");
        strBuff.append("   tariff="+getTariff()+"\n");
        strBuff.append("   unit="+isUnit()+"\n");
        return strBuff.toString();
    }    

    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public boolean isUnit() {
        return unit;
    }

    public void setUnit(boolean unit) {
        this.unit = unit;
    }

    public int getTariff() {
        return tariff;
    }

    public void setTariff(int tariff) {
        this.tariff = tariff;
    }

    public int getStorageNumber() {
        return storageNumber;
    }

    public void setStorageNumber(int storageNumber) {
        this.storageNumber = storageNumber;
    }
    
}
