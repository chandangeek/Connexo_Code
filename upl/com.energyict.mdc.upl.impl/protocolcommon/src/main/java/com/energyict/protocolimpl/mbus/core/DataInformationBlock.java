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

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author kvds
 */
public class DataInformationBlock {
    
    private DataInformationfield dataInformationfield;
    private List dataInformationfieldExtensions;
    
    /** Creates a new instance of DataInformationBlock */
    public DataInformationBlock(byte[] data, int offset, TimeZone timeZone, final Logger logger) throws IOException {
        final int startDifPos = offset;
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
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "[DataInformationBlock] data [" + startDifPos + "] : " + ParseUtils.asHex(new byte[] {data[startDifPos]}) + "] [" + this.toString() + "]");
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
 
    public static void main(String[] args) {
        
        try {
        byte[] data = new byte[]{(byte)0x85,(byte)0x91,(byte)0x11,(byte)0x08,(byte)0x2c,(byte)0x1e,(byte)0x30,(byte)0x53};
        DataInformationBlock o = new DataInformationBlock(data,0, TimeZone.getTimeZone("ECT"), Logger.getLogger(DataInformationBlock.class.getName()));
        System.out.println(Long.toHexString(o.getStorageNumber()));
        System.out.println(Integer.toHexString(o.getTariffNumber()));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
}
