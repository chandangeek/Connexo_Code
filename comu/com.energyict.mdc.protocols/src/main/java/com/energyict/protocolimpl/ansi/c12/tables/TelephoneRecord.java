/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TelephoneRecord.java
 *
 * Created on 23 februari 2006, 11:30
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
public class TelephoneRecord {

    private TelephoneFlagsBitfield telephoneFlagsBitfield;
    private int numberOfOriginateWindows;
    private int numberOfSetupStrings;
    private int setupStringLength;
    private int prefixLength;
    private int numberOfOriginateNumbers;
    private int phoneNumberLength;
    private int numberOfRecurringDates;
    private int numberOfNonRecurringDates;
    private int numberOfEvents;
    private int numberOfWeeklySchedules;
    private int numberOfAnswerWindows;
    private int numberOfCallerIds;
    private int callerIdLength;

    /** Creates a new instance of TelephoneRecord */
    public TelephoneRecord(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        telephoneFlagsBitfield = new TelephoneFlagsBitfield(data,offset,tableFactory);
        offset+=TelephoneFlagsBitfield.getSize(tableFactory);
        numberOfOriginateWindows = C12ParseUtils.getInt(data,offset++);
        numberOfSetupStrings = C12ParseUtils.getInt(data,offset++);
        setupStringLength = C12ParseUtils.getInt(data,offset++);
        prefixLength = C12ParseUtils.getInt(data,offset++);
        numberOfOriginateNumbers = C12ParseUtils.getInt(data,offset++);
        phoneNumberLength = C12ParseUtils.getInt(data,offset++);
        numberOfRecurringDates = C12ParseUtils.getInt(data,offset++);
        numberOfNonRecurringDates = C12ParseUtils.getInt(data,offset++);
        numberOfEvents = C12ParseUtils.getInt(data,offset++);
        numberOfWeeklySchedules = C12ParseUtils.getInt(data,offset++);
        numberOfAnswerWindows = C12ParseUtils.getInt(data,offset++);
        numberOfCallerIds = C12ParseUtils.getInt(data,offset++);
        callerIdLength = C12ParseUtils.getInt(data,offset++);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TelephoneRecord:\n");
        strBuff.append("   callerIdLength="+getCallerIdLength()+"\n");
        strBuff.append("   numberOfAnswerWindows="+getNumberOfAnswerWindows()+"\n");
        strBuff.append("   numberOfCallerIds="+getNumberOfCallerIds()+"\n");
        strBuff.append("   numberOfEvents="+getNumberOfEvents()+"\n");
        strBuff.append("   numberOfNonRecurringDates="+getNumberOfNonRecurringDates()+"\n");
        strBuff.append("   numberOfOriginateNumbers="+getNumberOfOriginateNumbers()+"\n");
        strBuff.append("   numberOfOriginateWindows="+getNumberOfOriginateWindows()+"\n");
        strBuff.append("   numberOfRecurringDates="+getNumberOfRecurringDates()+"\n");
        strBuff.append("   numberOfSetupStrings="+getNumberOfSetupStrings()+"\n");
        strBuff.append("   numberOfWeeklySchedules="+getNumberOfWeeklySchedules()+"\n");
        strBuff.append("   phoneNumberLength="+getPhoneNumberLength()+"\n");
        strBuff.append("   prefixLength="+getPrefixLength()+"\n");
        strBuff.append("   setupStringLength="+getSetupStringLength()+"\n");
        strBuff.append("   telephoneFlagsBitfield="+getTelephoneFlagsBitfield()+"\n");
        return strBuff.toString();
    }


    static public int getSize(TableFactory tableFactory) throws IOException {
        return 13+TelephoneFlagsBitfield.getSize(tableFactory);
    }

    public TelephoneFlagsBitfield getTelephoneFlagsBitfield() {
        return telephoneFlagsBitfield;
    }

    public int getNumberOfOriginateWindows() {
        return numberOfOriginateWindows;
    }

    public int getNumberOfSetupStrings() {
        return numberOfSetupStrings;
    }

    public int getSetupStringLength() {
        return setupStringLength;
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    public int getNumberOfOriginateNumbers() {
        return numberOfOriginateNumbers;
    }

    public int getPhoneNumberLength() {
        return phoneNumberLength;
    }

    public int getNumberOfRecurringDates() {
        return numberOfRecurringDates;
    }

    public int getNumberOfNonRecurringDates() {
        return numberOfNonRecurringDates;
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public int getNumberOfWeeklySchedules() {
        return numberOfWeeklySchedules;
    }

    public int getNumberOfAnswerWindows() {
        return numberOfAnswerWindows;
    }

    public int getNumberOfCallerIds() {
        return numberOfCallerIds;
    }

    public int getCallerIdLength() {
        return callerIdLength;
    }
}
