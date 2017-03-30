/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PhoneNumbersRecord.java
 *
 * Created on 23 februari 2006, 13:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PhoneNumbersRecord {

    private String prefix;
    private String[] phoneNumbers;

    /** Creates a new instance of PhoneNumbersRecord */
    public PhoneNumbersRecord(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualTelephoneTable att = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable();
        prefix = new String(ProtocolUtils.getSubArray2(data,offset,att.getTelephoneRecord().getPrefixLength()));
        offset+=att.getTelephoneRecord().getPrefixLength();
        phoneNumbers = new String[att.getTelephoneRecord().getNumberOfOriginateNumbers()];
        for (int i=0;i<phoneNumbers.length;i++) {
            getPhoneNumbers()[i] = new String(ProtocolUtils.getSubArray2(data,offset,att.getTelephoneRecord().getPhoneNumberLength()));
            offset+=att.getTelephoneRecord().getPhoneNumberLength();
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PhoneNumbersRecord:\n");
        for (int i=0;i<phoneNumbers.length;i++) {
            strBuff.append("   phoneNumbers["+i+"]="+getPhoneNumbers()[i]+"\n");
        }
        strBuff.append("   prefix="+getPrefix()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualTelephoneTable att = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTelephoneTable();
        int size=0;
        size+=att.getTelephoneRecord().getPrefixLength();
        for (int i=0;i<att.getTelephoneRecord().getNumberOfOriginateNumbers();i++) {
            size+=att.getTelephoneRecord().getPhoneNumberLength();
        }
        return size;
    }

    public String getPrefix() {
        return prefix;
    }

    public String[] getPhoneNumbers() {
        return phoneNumbers;
    }
}
