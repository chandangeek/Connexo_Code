/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadProfileBlockData.java
 *
 * Created on 8 november 2005, 14:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class LoadProfileBlockData {

    private Date blockEndTime;
    private Readings[] endReadings;
    private int[] simpleIntStatus;
    private IntervalSet[] loadProfileInterval;
    private int nrOfIntervalsPerBlock;

    /** Creates a new instance of LoadProfileBlockData */
    public LoadProfileBlockData(byte[] data,int offset,TableFactory tableFactory, int set, boolean headerOnly, int nrOfIntervalsInBlock) throws IOException {
        this(data,offset,tableFactory,set,headerOnly?0:-1,nrOfIntervalsInBlock);
    }
    public LoadProfileBlockData(byte[] data,int offset,TableFactory tableFactory, int set, int intervalsets, int nrOfIntervalsInBlock) throws IOException {
        //ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setNrOfIntervalsPerBlock(alpt.getLoadProfileSet().getNrOfBlockIntervalsSet()[set]);

        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            setBlockEndTime(C12ParseUtils.getDateFromSTimeAndAdjustForTimeZone(data,offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(), dataOrder));
        else
            setBlockEndTime(C12ParseUtils.getDateFromSTime(data,offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(), dataOrder));

        offset+=C12ParseUtils.getSTimeSize(cfgt.getTimeFormat());
        setEndReadings(new Readings[alpt.getLoadProfileSet().getNrOfChannelsSet()[set]]);
        for (int i=0;i<getEndReadings().length;i++ ) {
            getEndReadings()[i] = new Readings(data,offset, tableFactory);
            offset+=Readings.getSize(tableFactory);
        }
        if (alpt.getLoadProfileSet().isSimpleIntStatusFlag()) {



            setSimpleIntStatus(new int[(getNrOfIntervalsPerBlock()+7)/8]);

            for (int i=0;i<getSimpleIntStatus().length;i++ ) {
                getSimpleIntStatus()[i]=C12ParseUtils.getInt(data,offset++);
            }
        }
        if (intervalsets!=0) {
            setLoadProfileInterval(new IntervalSet[intervalsets==-1?getNrOfIntervalsPerBlock():intervalsets]);
            for (int i=0;i<getLoadProfileInterval().length;i++ ) {

                int index =intervalsets==-1?i:(i+(nrOfIntervalsInBlock-intervalsets));
//System.out.println("KV_DEBUG> nrOfIntervalsInBlock = "+nrOfIntervalsInBlock+", index = "+index);
                getLoadProfileInterval()[i] = new IntervalSet(data,offset, tableFactory, set, isIntervalValid(index));
                offset+=IntervalSet.getSize(tableFactory,set);

            }
        }

    }

    public int nrOfValidIntervals() {
        int count=0;
        for (int i=0;i<loadProfileInterval.length;i++) {
            if (loadProfileInterval[i].isValid())
                count++;
        }
        return count;
    }

    public boolean isIntervalValid(int interval) {
        //int padding = getSimpleIntStatus().length*8-getNrOfIntervalsPerBlock();

//        int intervalByteIndex = (getSimpleIntStatus().length) - ((interval/8)+1);
//        int intervalBit = 1 << (7-(interval%8));

        int intervalByteIndex = (interval/8);
        int intervalBit = 1 << (interval%8);

        if (getSimpleIntStatus() != null) {
            if ((getSimpleIntStatus()[intervalByteIndex] & intervalBit) != 0)
                return true;
            else
                return false;
        }
        else return true;

    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileBlockData: \n");
        strBuff.append("    blockEndTime="+getBlockEndTime()+"\n");
        for (int i=0;i<getEndReadings().length;i++ )
            strBuff.append("    endReadings["+i+"]="+getEndReadings()[i]+"\n");
        if (getSimpleIntStatus() != null)
            for (int i=0;i<getSimpleIntStatus().length;i++ ) {
                strBuff.append("    simpleIntStatus["+i+"]=B"+Integer.toBinaryString(getSimpleIntStatus()[i])+"\n");
            }
        if (loadProfileInterval != null) {
            for (int i=0;i<getLoadProfileInterval().length;i++ ) {
                strBuff.append("    interval valid = "+isIntervalValid(i)+"\n");
                strBuff.append("    loadProfileInterval["+i+"]="+getLoadProfileInterval()[i]+"\n");
            }
        }
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory,int set, boolean headerOnly) throws IOException {
        return getSize(tableFactory,set,headerOnly?0:-1);
    }
    static public int getSize(TableFactory tableFactory,int set, int intervalsets) throws IOException {
        //ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int size=0;
        size+=C12ParseUtils.getSTimeSize(cfgt.getTimeFormat());
        size+=Readings.getSize(tableFactory)*alpt.getLoadProfileSet().getNrOfChannelsSet()[set];
        if (alpt.getLoadProfileSet().isSimpleIntStatusFlag())
            size+=(alpt.getLoadProfileSet().getNrOfBlockIntervalsSet()[set]+7)/8;
        if (intervalsets != 0)
           size+=IntervalSet.getSize(tableFactory,set)*(intervalsets==-1?alpt.getLoadProfileSet().getNrOfBlockIntervalsSet()[set]:intervalsets);
        return size;
    }

    public Date getBlockEndTime() {
        return blockEndTime;
    }

    public void setBlockEndTime(Date blockEndTime) {
        this.blockEndTime = blockEndTime;
    }

    public Readings[] getEndReadings() {
        return endReadings;
    }

    public void setEndReadings(Readings[] endReadings) {
        this.endReadings = endReadings;
    }

    public int[] getSimpleIntStatus() {
        return simpleIntStatus;
    }

    public void setSimpleIntStatus(int[] simpleIntStatus) {
        this.simpleIntStatus = simpleIntStatus;
    }

    public IntervalSet[] getLoadProfileInterval() {
        return loadProfileInterval;
    }

    public void setLoadProfileInterval(IntervalSet[] loadProfileInterval) {
        this.loadProfileInterval = loadProfileInterval;
    }

    public int getNrOfIntervalsPerBlock() {
        return nrOfIntervalsPerBlock;
    }

    public void setNrOfIntervalsPerBlock(int nrOfIntervalsPerBlock) {
        this.nrOfIntervalsPerBlock = nrOfIntervalsPerBlock;
    }
}
