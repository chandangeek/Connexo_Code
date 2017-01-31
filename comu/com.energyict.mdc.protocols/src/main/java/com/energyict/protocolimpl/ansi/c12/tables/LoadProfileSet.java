/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadProfileSet.java
 *
 * Created on 7 november 2005, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfileSet {

    private long loadProfileMemoryLength; // 32 bit

    private int loadProfileFlagsBitfield; // 16 bit load profile list management capabilities
    private boolean loadProfileSet1InhibitOverflowFlag;
    private boolean loadProfileSet2InhibitOverflowFlag;
    private boolean loadProfileSet3InhibitOverflowFlag;
    private boolean loadProfileSet4InhibitOverflowFlag;
    private boolean blockEndReadFlag;
    private boolean blockEndPulseFlag;
    private boolean scalarDivisorFlagSet1;
    private boolean scalarDivisorFlagSet2;
    private boolean scalarDivisorFlagSet3;
    private boolean scalarDivisorFlagSet4;
    private boolean extendedIntStatusFlag;
    private boolean simpleIntStatusFlag;

    private int loadProfileFormatsBitfield; // 8 bit load profile format
    private boolean intervalUINT8FormatFlag;
    private boolean intervalUINT16FormatFlag;
    private boolean intervalUINT32FormatFlag;
    private boolean intervalINT8FormatFlag;
    private boolean intervalINT16FormatFlag;
    private boolean intervalINT32FormatFlag;
    private boolean intervalNI1FormatFlag;
    private boolean intervalNI2FormatFlag;


    static public final int MAX_NR_OF_LP_SETS=4;
    private int[] nrOfBlocksSet; // 16 bits max nr of data blocks that can be contained in load profile set
    private int[] nrOfBlockIntervalsSet; // 16 bits nr of intervals per data block that can be contained in load profile set
    private int[] nrOfChannelsSet; // 8 bit max nr of channels of load profile data that can be contained in load profile set
    private int[] profileIntervalSet; // 8 bit profileinterval in minutes for load profile set


    /** Creates a new instance of LoadProfileSet */
    public LoadProfileSet(byte[] data,int offset,TableFactory tableFactory) throws IOException {
       // ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
       // ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setLoadProfileMemoryLength(C12ParseUtils.getLong(data,offset,4, dataOrder));
        offset+=4;
        setLoadProfileFlagsBitfield(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;

        setLoadProfileSet1InhibitOverflowFlag((getLoadProfileFlagsBitfield()&0x0001)==0x0001);
        setLoadProfileSet2InhibitOverflowFlag((getLoadProfileFlagsBitfield()&0x0002)==0x0002);
        setLoadProfileSet3InhibitOverflowFlag((getLoadProfileFlagsBitfield()&0x0004)==0x0004);
        setLoadProfileSet4InhibitOverflowFlag((getLoadProfileFlagsBitfield()&0x0008)==0x0008);
        setBlockEndReadFlag((getLoadProfileFlagsBitfield()&0x0010)==0x0010);
        setBlockEndPulseFlag((getLoadProfileFlagsBitfield()&0x0020)==0x0020);
        setScalarDivisorFlagSet1((getLoadProfileFlagsBitfield()&0x0040)==0x0040);
        setScalarDivisorFlagSet2((getLoadProfileFlagsBitfield()&0x0080)==0x0080);
        setScalarDivisorFlagSet3((getLoadProfileFlagsBitfield()&0x0100)==0x0100);
        setScalarDivisorFlagSet4((getLoadProfileFlagsBitfield()&0x0200)==0x0200);
        setExtendedIntStatusFlag((getLoadProfileFlagsBitfield()&0x0400)==0x0400);
        setSimpleIntStatusFlag((getLoadProfileFlagsBitfield()&0x0800)==0x0800);

        setLoadProfileFormatsBitfield(C12ParseUtils.getInt(data,offset));
        offset++;
        setIntervalUINT8FormatFlag((getLoadProfileFormatsBitfield()&0x01)==0x01);
        setIntervalUINT16FormatFlag((getLoadProfileFormatsBitfield()&0x02)==0x02);
        setIntervalUINT32FormatFlag((getLoadProfileFormatsBitfield()&0x04)==0x04);
        setIntervalINT8FormatFlag((getLoadProfileFormatsBitfield()&0x08)==0x08);
        setIntervalINT16FormatFlag((getLoadProfileFormatsBitfield()&0x10)==0x10);
        setIntervalINT32FormatFlag((getLoadProfileFormatsBitfield()&0x20)==0x20);
        setIntervalNI1FormatFlag((getLoadProfileFormatsBitfield()&0x40)==0x40);
        setIntervalNI2FormatFlag((getLoadProfileFormatsBitfield()&0x80)==0x80);

        setNrOfBlocksSet(new int[MAX_NR_OF_LP_SETS]);
        setNrOfBlockIntervalsSet(new int[MAX_NR_OF_LP_SETS]);
        setNrOfChannelsSet(new int[MAX_NR_OF_LP_SETS]);
        setProfileIntervalSet(new int[MAX_NR_OF_LP_SETS]);

        for (int i=0;i<MAX_NR_OF_LP_SETS;i++) {
            if ((cfgt.getStdTablesUsed()[8]&(0x01<<i))==(0x01<<i)) {
                getNrOfBlocksSet()[i] = C12ParseUtils.getInt(data,offset,2, dataOrder);
                offset+=2;
                getNrOfBlockIntervalsSet()[i] = C12ParseUtils.getInt(data,offset,2, dataOrder);
                offset+=2;
                getNrOfChannelsSet()[i] = C12ParseUtils.getInt(data,offset++);
                getProfileIntervalSet()[i] = C12ParseUtils.getInt(data,offset++);
            }
        }
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileSet: \n");
        strBuff.append("    loadProfileMemoryLength="+getLoadProfileMemoryLength()+", loadProfileFlagsBitfield=0x"+Integer.toHexString(getLoadProfileFlagsBitfield())+", loadProfileFormatsBitfield=0x"+getLoadProfileFormatsBitfield()+"\n");
        for (int i=0;i<MAX_NR_OF_LP_SETS;i++) {
            strBuff.append("nrOfBlocksSet["+i+"]="+getNrOfBlocksSet()[i]);
            strBuff.append(", nrOfBlockIntervalsSet["+i+"]="+getNrOfBlockIntervalsSet()[i]);
            strBuff.append(", nrOfChannelsSet["+i+"]="+getNrOfChannelsSet()[i]);
            strBuff.append(", profileIntervalSet["+i+"]="+getProfileIntervalSet()[i]+"\n");
        }
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int size=7;
        for (int i=0;i<MAX_NR_OF_LP_SETS;i++) {
            if ((cfgt.getStdTablesUsed()[8]&(0x01<<i))==(0x01<<i)) {
                size+=6;
            }
        }
        return size;
    }

    public long getLoadProfileMemoryLength() {
        return loadProfileMemoryLength;
    }

    public void setLoadProfileMemoryLength(long loadProfileMemoryLength) {
        this.loadProfileMemoryLength = loadProfileMemoryLength;
    }

    public int getLoadProfileFlagsBitfield() {
        return loadProfileFlagsBitfield;
    }

    public void setLoadProfileFlagsBitfield(int loadProfileFlagsBitfield) {
        this.loadProfileFlagsBitfield = loadProfileFlagsBitfield;
    }

    public int getLoadProfileFormatsBitfield() {
        return loadProfileFormatsBitfield;
    }

    public void setLoadProfileFormatsBitfield(int loadProfileFormatsBitfield) {
        this.loadProfileFormatsBitfield = loadProfileFormatsBitfield;
    }

    public int[] getNrOfBlocksSet() {
        return nrOfBlocksSet;
    }

    public void setNrOfBlocksSet(int[] nrOfBlocksSet) {
        this.nrOfBlocksSet = nrOfBlocksSet;
    }

    public int[] getNrOfBlockIntervalsSet() {
        return nrOfBlockIntervalsSet;
    }

    public void setNrOfBlockIntervalsSet(int[] nrOfBlockIntervalsSet) {
        this.nrOfBlockIntervalsSet = nrOfBlockIntervalsSet;
    }

    public int[] getNrOfChannelsSet() {
        return nrOfChannelsSet;
    }

    public void setNrOfChannelsSet(int[] nrOfChannelsSet) {
        this.nrOfChannelsSet = nrOfChannelsSet;
    }

    public int[] getProfileIntervalSet() {
        return profileIntervalSet;
    }

    public void setProfileIntervalSet(int[] profileIntervalSet) {
        this.profileIntervalSet = profileIntervalSet;
    }

    public boolean isLoadProfileSet1InhibitOverflowFlag() {
        return loadProfileSet1InhibitOverflowFlag;
    }

    public void setLoadProfileSet1InhibitOverflowFlag(boolean loadProfileSet1InhibitOverflowFlag) {
        this.loadProfileSet1InhibitOverflowFlag = loadProfileSet1InhibitOverflowFlag;
    }

    public boolean isLoadProfileSet2InhibitOverflowFlag() {
        return loadProfileSet2InhibitOverflowFlag;
    }

    public void setLoadProfileSet2InhibitOverflowFlag(boolean loadProfileSet2InhibitOverflowFlag) {
        this.loadProfileSet2InhibitOverflowFlag = loadProfileSet2InhibitOverflowFlag;
    }

    public boolean isLoadProfileSet3InhibitOverflowFlag() {
        return loadProfileSet3InhibitOverflowFlag;
    }

    public void setLoadProfileSet3InhibitOverflowFlag(boolean loadProfileSet3InhibitOverflowFlag) {
        this.loadProfileSet3InhibitOverflowFlag = loadProfileSet3InhibitOverflowFlag;
    }

    public boolean isLoadProfileSet4InhibitOverflowFlag() {
        return loadProfileSet4InhibitOverflowFlag;
    }

    public void setLoadProfileSet4InhibitOverflowFlag(boolean loadProfileSet4InhibitOverflowFlag) {
        this.loadProfileSet4InhibitOverflowFlag = loadProfileSet4InhibitOverflowFlag;
    }

    public boolean isBlockEndReadFlag() {
        return blockEndReadFlag;
    }

    public void setBlockEndReadFlag(boolean blockEndReadFlag) {
        this.blockEndReadFlag = blockEndReadFlag;
    }

    public boolean isBlockEndPulseFlag() {
        return blockEndPulseFlag;
    }

    public void setBlockEndPulseFlag(boolean blockEndPulseFlag) {
        this.blockEndPulseFlag = blockEndPulseFlag;
    }

    public boolean isScalarDivisorFlagSet1() {
        return scalarDivisorFlagSet1;
    }

    public void setScalarDivisorFlagSet1(boolean scalarDivisorFlagSet1) {
        this.scalarDivisorFlagSet1 = scalarDivisorFlagSet1;
    }

    public boolean isScalarDivisorFlagSet2() {
        return scalarDivisorFlagSet2;
    }

    public void setScalarDivisorFlagSet2(boolean scalarDivisorFlagSet2) {
        this.scalarDivisorFlagSet2 = scalarDivisorFlagSet2;
    }

    public boolean isScalarDivisorFlagSet3() {
        return scalarDivisorFlagSet3;
    }

    public void setScalarDivisorFlagSet3(boolean scalarDivisorFlagSet3) {
        this.scalarDivisorFlagSet3 = scalarDivisorFlagSet3;
    }

    public boolean isScalarDivisorFlagSet4() {
        return scalarDivisorFlagSet4;
    }

    public void setScalarDivisorFlagSet4(boolean scalarDivisorFlagSet4) {
        this.scalarDivisorFlagSet4 = scalarDivisorFlagSet4;
    }

    public boolean isExtendedIntStatusFlag() {
        return extendedIntStatusFlag;
    }

    public void setExtendedIntStatusFlag(boolean extendedIntStatusFlag) {
        this.extendedIntStatusFlag = extendedIntStatusFlag;
    }

    public boolean isSimpleIntStatusFlag() {
        return simpleIntStatusFlag;
    }

    public void setSimpleIntStatusFlag(boolean simpleIntStatusFlag) {
        this.simpleIntStatusFlag = simpleIntStatusFlag;
    }

    public boolean isIntervalUINT8FormatFlag() {
        return intervalUINT8FormatFlag;
    }

    public void setIntervalUINT8FormatFlag(boolean intervalUINT8FormatFlag) {
        this.intervalUINT8FormatFlag = intervalUINT8FormatFlag;
    }

    public boolean isIntervalUINT16FormatFlag() {
        return intervalUINT16FormatFlag;
    }

    public void setIntervalUINT16FormatFlag(boolean intervalUINT16FormatFlag) {
        this.intervalUINT16FormatFlag = intervalUINT16FormatFlag;
    }

    public boolean isIntervalUINT32FormatFlag() {
        return intervalUINT32FormatFlag;
    }

    public void setIntervalUINT32FormatFlag(boolean intervalUINT32FormatFlag) {
        this.intervalUINT32FormatFlag = intervalUINT32FormatFlag;
    }

    public boolean isIntervalINT8FormatFlag() {
        return intervalINT8FormatFlag;
    }

    public void setIntervalINT8FormatFlag(boolean intervalINT8FormatFlag) {
        this.intervalINT8FormatFlag = intervalINT8FormatFlag;
    }

    public boolean isIntervalINT16FormatFlag() {
        return intervalINT16FormatFlag;
    }

    public void setIntervalINT16FormatFlag(boolean intervalINT16FormatFlag) {
        this.intervalINT16FormatFlag = intervalINT16FormatFlag;
    }

    public boolean isIntervalINT32FormatFlag() {
        return intervalINT32FormatFlag;
    }

    public void setIntervalINT32FormatFlag(boolean intervalINT32FormatFlag) {
        this.intervalINT32FormatFlag = intervalINT32FormatFlag;
    }

    public boolean isIntervalNI1FormatFlag() {
        return intervalNI1FormatFlag;
    }

    public void setIntervalNI1FormatFlag(boolean intervalNI1FormatFlag) {
        this.intervalNI1FormatFlag = intervalNI1FormatFlag;
    }

    public boolean isIntervalNI2FormatFlag() {
        return intervalNI2FormatFlag;
    }

    public void setIntervalNI2FormatFlag(boolean intervalNI2FormatFlag) {
        this.intervalNI2FormatFlag = intervalNI2FormatFlag;
    }
}
