/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadProfileDataSet.java
 *
 * Created on 8 november 2005, 15:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfileDataSet {

    private LoadProfileBlockData[] loadProfileDataSets;



    /** Creates a new instance of LoadProfileDataSet */
    public LoadProfileDataSet(byte[] data,int offset,TableFactory tableFactory, int set, int nrOfBlocksRequested, boolean headerOnly,int nrOfIntervalsInBlock) throws IOException {
        this(data, offset, tableFactory, set,nrOfBlocksRequested,headerOnly?0:-1, nrOfIntervalsInBlock);
    }
    public LoadProfileDataSet(byte[] data,int offset,TableFactory tableFactory, int set, int nrOfBlocksRequested, int intervalsets,int nrOfIntervalsInBlock) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();

        //setLoadProfileDataSets(new LoadProfileBlockData[alpt.getLoadProfileSet().getNrOfBlocksSet()[set]]);
        setLoadProfileDataSets(new LoadProfileBlockData[nrOfBlocksRequested==0?1:nrOfBlocksRequested]);


        for(int i=0;i<getLoadProfileDataSets().length;i++) {
            getLoadProfileDataSets()[i]=new LoadProfileBlockData(data, offset, tableFactory, set, intervalsets, nrOfIntervalsInBlock);
            offset+=LoadProfileBlockData.getSize(tableFactory,set, intervalsets);

        }
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileDataSet: \n");
        for(int i=0;i<getLoadProfileDataSets().length;i++)
            strBuff.append("    loadProfileDataSets="+getLoadProfileDataSets()[i]+"\n");

        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory, int set, int nrOfBlocksRequested, boolean headerOnly) throws IOException {
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        return nrOfBlocksRequested*LoadProfileBlockData.getSize(tableFactory,set,headerOnly);
//        return alpt.getLoadProfileSet().getNrOfBlocksSet()[set]*LoadProfileBlockData.getSize(tableFactory,set);
    }

    public LoadProfileBlockData[] getLoadProfileDataSets() {
        return loadProfileDataSets;
    }

    public void setLoadProfileDataSets(LoadProfileBlockData[] loadProfileDataSets) {
        this.loadProfileDataSets = loadProfileDataSets;
    }
}
