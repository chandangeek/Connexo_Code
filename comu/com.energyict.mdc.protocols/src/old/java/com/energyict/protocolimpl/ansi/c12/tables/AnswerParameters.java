/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AnswerParameters.java
 *
 * Created on 23 februari 2006, 16:43
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
public class AnswerParameters extends AbstractTable {

    private long answerBitRate;
    private int lockoutDelay;
    private int retryAttempts;
    private int retryLockoutTime;
    private int numberOfRings;
    private int numberOfRingsOutside;
    private String[] callerIds;
    private WindowRecord[] windows;


    /** Creates a new instance of AnswerParameters */
    public AnswerParameters(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(95));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AnswerParameters:\n");
        strBuff.append("   answerBitRate="+getAnswerBitRate()+"\n");
        strBuff.append("   callerIds="+getCallerIds()+"\n");
        strBuff.append("   lockoutDelay="+getLockoutDelay()+"\n");
        strBuff.append("   numberOfRings="+getNumberOfRings()+"\n");
        strBuff.append("   numberOfRingsOutside="+getNumberOfRingsOutside()+"\n");
        strBuff.append("   retryAttempts="+getRetryAttempts()+"\n");
        strBuff.append("   retryLockoutTime="+getRetryLockoutTime()+"\n");
        strBuff.append("   windows="+getWindows()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualTelephoneTable att = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable();
        int offset=0;

        if (att.getTelephoneRecord().getTelephoneFlagsBitfield().getBitRate()==2) {
            answerBitRate = C12ParseUtils.getLong(tableData,offset,4,cfgt.getDataOrder());
            offset+=4;
        }
        if (!att.getTelephoneRecord().getTelephoneFlagsBitfield().isNoLockoutParm()) {
            lockoutDelay = C12ParseUtils.getInt(tableData,offset++);
            retryAttempts = C12ParseUtils.getInt(tableData,offset++);
            retryLockoutTime = C12ParseUtils.getInt(tableData,offset++);
        }
        numberOfRings = C12ParseUtils.getInt(tableData,offset++);
        if (att.getTelephoneRecord().getNumberOfAnswerWindows() > 0) {
            numberOfRingsOutside = C12ParseUtils.getInt(tableData,offset++);
        }

        callerIds = new String[att.getTelephoneRecord().getNumberOfCallerIds()];
        for (int i=0;i<getCallerIds().length;i++) {
            getCallerIds()[i] = new String(ProtocolUtils.getSubArray2(tableData,offset,att.getTelephoneRecord().getCallerIdLength()));
            offset+=att.getTelephoneRecord().getCallerIdLength();
        }

        windows = new WindowRecord[att.getTelephoneRecord().getNumberOfAnswerWindows()];
        for (int i=0;i<getWindows().length;i++) {
            getWindows()[i] = new WindowRecord(tableData,offset,getTableFactory());
            offset+=WindowRecord.getSize(getTableFactory());
        }
    }

    public long getAnswerBitRate() {
        return answerBitRate;
    }

    public int getLockoutDelay() {
        return lockoutDelay;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public int getRetryLockoutTime() {
        return retryLockoutTime;
    }

    public int getNumberOfRings() {
        return numberOfRings;
    }

    public int getNumberOfRingsOutside() {
        return numberOfRingsOutside;
    }

    public String[] getCallerIds() {
        return callerIds;
    }

    public WindowRecord[] getWindows() {
        return windows;
    }
}
