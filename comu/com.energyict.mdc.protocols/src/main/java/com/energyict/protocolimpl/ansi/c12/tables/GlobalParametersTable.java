/*
 * GlobalParametersTable.java
 *
 * Created on 23 februari 2006, 11:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class GlobalParametersTable extends AbstractTable {

    private int psemIdentity;
    private long bitRate;
    private String[] modemSetupStrings;

    /** Creates a new instance of GlobalParametersTable */
    public GlobalParametersTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(92));
    }

     public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GlobalParametersTable:\n");
        strBuff.append("   bitRate="+getBitRate()+"\n");
        for (int i=0;i<getModemSetupStrings().length;i++) {
            strBuff.append("   modemSetupStrings["+i+"]="+getModemSetupStrings()[i]+"\n");
        }
        strBuff.append("   psemIdentity="+getPsemIdentity()+"\n");
        return strBuff.toString();
    }


    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();

        int offset=0;
        psemIdentity = C12ParseUtils.getInt(tableData,offset++);
        if (getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable().getTelephoneRecord().getTelephoneFlagsBitfield().getBitRate()==1) {
            bitRate = C12ParseUtils.getLong(tableData,offset,4,cfgt.getDataOrder());
            offset+=4;
        }
        modemSetupStrings = new String[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable().getTelephoneRecord().getNumberOfSetupStrings()];
        int length = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable().getTelephoneRecord().getSetupStringLength();
        for (int i=0;i<getModemSetupStrings().length;i++) {
            getModemSetupStrings()[i] = new String(ProtocolUtils.getSubArray2(tableData,offset,length));
            offset+=length;
        }


    }

    public int getPsemIdentity() {
        return psemIdentity;
    }

    public long getBitRate() {
        return bitRate;
    }

    public String[] getModemSetupStrings() {
        return modemSetupStrings;
    }
}
