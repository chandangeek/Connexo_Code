/*
 * OriginateParametersTable.java
 *
 * Created on 23 februari 2006, 13:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class OriginateParametersTable extends AbstractTable {

    private long originateBitRate;
    private int dialDelay;
    private PhoneNumbersRecord phoneNumbersRecord;
    private WindowRecord[] windowRecords;

    /** Creates a new instance of OriginateParametersTable */
    public OriginateParametersTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(93));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OriginateParametersTable:\n");
        strBuff.append("   dialDelay="+getDialDelay()+"\n");
        strBuff.append("   originateBitRate="+getOriginateBitRate()+"\n");
        strBuff.append("   phoneNumbersRecord="+getPhoneNumbersRecord()+"\n");
        for (int i=0;i<getWindowRecords().length;i++) {
            strBuff.append("   windowRecords["+i+"]="+getWindowRecords()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualTelephoneTable att = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable();
        int offset=0;
        if (att.getTelephoneRecord().getTelephoneFlagsBitfield().getBitRate()==2) {
            originateBitRate = C12ParseUtils.getLong(tableData,offset,4,cfgt.getDataOrder());
            offset+=4;
        }
        dialDelay = C12ParseUtils.getInt(tableData,offset++);
        phoneNumbersRecord = new PhoneNumbersRecord(tableData,offset,getTableFactory());
        offset += PhoneNumbersRecord.getSize(getTableFactory());
        windowRecords  = new WindowRecord[att.getTelephoneRecord().getNumberOfOriginateWindows()];
        for (int i=0;i<getWindowRecords().length;i++) {
            getWindowRecords()[i] = new WindowRecord(tableData,offset,getTableFactory());
            offset+=WindowRecord.getSize(getTableFactory());
        }

    }

    public long getOriginateBitRate() {
        return originateBitRate;
    }

    public int getDialDelay() {
        return dialDelay;
    }

    public PhoneNumbersRecord getPhoneNumbersRecord() {
        return phoneNumbersRecord;
    }

    public WindowRecord[] getWindowRecords() {
        return windowRecords;
    }
}
