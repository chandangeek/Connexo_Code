/*
 * DataInformationBlock.java
 *
 * Created on 3 oktober 2007, 15:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


/**
 *
 * @author kvds
 */
public class DataInformationBlock {

    private DataInformationfield dataInformationfield;
    private List dataInformationfieldExtensions;

    /** Creates a new instance of DataInformationBlock */
    public DataInformationBlock(byte[] data, int offset, TimeZone timeZone) throws IOException {
        setDataInformationfield(new DataInformationfield(ProtocolUtils.getInt(data,offset++,1)));
        setDataInformationfieldExtensions(new ArrayList());
        if (getDataInformationfield().isExtension()) {
            DataInformationfieldExtension de = new DataInformationfieldExtension(ProtocolUtils.getInt(data,offset++,1));
            getDataInformationfieldExtensions().add(de);
            while(de.isExtension()) {
                de = new DataInformationfieldExtension(ProtocolUtils.getInt(data,offset++,1));
                getDataInformationfieldExtensions().add(de);
            }
        }
    }


    // use dif's storage bit and all subsequent dife's if any to construct the storagenumber...
    public long getStorageNumber() {
        long storageNumber=0;
        if (dataInformationfield.isLsbStorageNumber())
            storageNumber |= 1;
        for (int i=0;i<dataInformationfieldExtensions.size();i++) {
            DataInformationfieldExtension dife = (DataInformationfieldExtension)dataInformationfieldExtensions.get(i);
            long temp = (long)dife.getStorageNumber();
            temp <<= (1+i*4);
            storageNumber |= temp;
        }
        return storageNumber;
    }

    // use all subsequent dife's if any to construct the tariff number...
    public int getTariffNumber() {
        int tariffNumber=0;
        for (int i=0;i<dataInformationfieldExtensions.size();i++) {
            DataInformationfieldExtension dife = (DataInformationfieldExtension)dataInformationfieldExtensions.get(i);
            int temp = dife.getTariff();
            temp <<= (i*2);
            tariffNumber |= temp;
        }
        return tariffNumber;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataInformationBlock:\n");
        strBuff.append("   dataInformationfield="+getDataInformationfield()+"\n");
        strBuff.append("   dataInformationfieldExtensions="+getDataInformationfieldExtensions()+"\n");
        return strBuff.toString();
    }

    public int size() {
        return 1+getDataInformationfieldExtensions().size();
    }

    public DataInformationfield getDataInformationfield() {
        return dataInformationfield;
    }

    public void setDataInformationfield(DataInformationfield dataInformationfield) {
        this.dataInformationfield = dataInformationfield;
    }

    public List getDataInformationfieldExtensions() {
        return dataInformationfieldExtensions;
    }

    public void setDataInformationfieldExtensions(List dataInformationfieldExtensions) {
        this.dataInformationfieldExtensions = dataInformationfieldExtensions;
    }

}
