/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CallStatus.java
 *
 * Created on 23 februari 2006, 17:25
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
public class CallStatus extends AbstractTable {

    private int[] callStatusArray;

    /** Creates a new instance of CallStatus */
    public CallStatus(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(97));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CallStatus:\n");
        strBuff.append("   callStatusArray="+getCallStatusArray()+"\n");
        return strBuff.toString();
    }


    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();

        int offset=0;

        callStatusArray = new int[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable().getTelephoneRecord().getNumberOfOriginateNumbers()];
        for(int i=0;i<getCallStatusArray().length;i++) {
            getCallStatusArray()[i]=C12ParseUtils.getInt(tableData,offset++);
        }

    }

    public int[] getCallStatusArray() {
        return callStatusArray;
    }
}
