/*
 * CallPurpose.java
 *
 * Created on 23 februari 2006, 17:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class CallPurpose extends AbstractTable {
    
    
    private int callPurpose; // bitfield 2 bytes
    private byte[] endDeviceManufacturerStatus;
    private String identification;
    
    /** Creates a new instance of CallPurpose */
    public CallPurpose(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(96));
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CallPurpose:\n");
        strBuff.append("   callPurpose="+getCallPurpose()+"\n");
        strBuff.append("   endDeviceManufacturerStatus="+ ProtocolUtils.outputHexString(getEndDeviceManufacturerStatus())+"\n");
        strBuff.append("   identification="+getIdentification()+"\n");
        return strBuff.toString();
    }
    
    
    protected void parse(byte[] tableData) throws IOException { 
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualTelephoneTable att = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable();
        int offset=0;
        
        callPurpose = C12ParseUtils.getInt(tableData,offset,2,cfgt.getDataOrder());
        offset+=2;
        endDeviceManufacturerStatus = ProtocolUtils.getSubArray2(tableData, offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDimMfgStatusUsed());
        offset+=getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDimMfgStatusUsed();         

        if (getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getIdForm() == 1) {
            // BCD
            identification = new String(ProtocolUtils.convertAscii2Binary(tableData));
        }
        else {
            // String
            identification = new String(tableData);
        }        
        
    }         

    public int getCallPurpose() {
        return callPurpose;
    }

    public byte[] getEndDeviceManufacturerStatus() {
        return endDeviceManufacturerStatus;
    }

    public String getIdentification() {
        return identification;
    }
}
