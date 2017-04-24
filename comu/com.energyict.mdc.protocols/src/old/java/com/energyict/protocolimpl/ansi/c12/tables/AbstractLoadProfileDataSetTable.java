/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractLoadProfileDataSetTable.java
 *
 * Created on 8 november 2005, 11:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractLoadProfileDataSetTable extends AbstractTable {

    abstract protected LoadProfileSetStatus getLoadProfileSetStatusCached() throws IOException;

    private LoadProfileDataSet loadProfileDataSet;
    private int nrOfBlocksToRequest;
    private int blockNrOffset;
    private boolean headerOnly;
    private int intervalsets;

    /** Creates a new instance of AbstractLoadProfileDataSetTable */
    public AbstractLoadProfileDataSetTable(StandardTableFactory tableFactory,int set) {
        super(tableFactory,new TableIdentification(set));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AbstractLoadProfileDataSetTable: \n");
        strBuff.append("    loadProfileDataSet="+getLoadProfileDataSet()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();

        int nrOfIntervalsInBlock;
        if (getBlockNrOffset() == getLoadProfileSetStatusCached().getLastBlockElement())
            nrOfIntervalsInBlock = getLoadProfileSetStatusCached().getNrOfValidIntervals();
        else
            nrOfIntervalsInBlock = alpt.getLoadProfileSet().getNrOfBlockIntervalsSet()[getTableIdentification().getTableId()-64];

        if (getIntervalsets() != -1)
           setLoadProfileDataSet(new LoadProfileDataSet(tableData, 0, getTableFactory(), getTableIdentification().getTableId()-64, getNrOfBlocksToRequest(), getIntervalsets(),nrOfIntervalsInBlock));
        else
           setLoadProfileDataSet(new LoadProfileDataSet(tableData, 0, getTableFactory(), getTableIdentification().getTableId()-64, getNrOfBlocksToRequest(), isHeaderOnly(),nrOfIntervalsInBlock));
    }

    public LoadProfileDataSet getLoadProfileDataSet() {
        return loadProfileDataSet;
    }

    public void setLoadProfileDataSet(LoadProfileDataSet loadProfileDataSet) {
        this.loadProfileDataSet = loadProfileDataSet;
    }

    public int getNrOfBlocksToRequest() {
        return nrOfBlocksToRequest;
    }

    public void setNrOfBlocksToRequest(int nrOfBlocksToRequest) {
        this.nrOfBlocksToRequest = nrOfBlocksToRequest;
    }

    protected void prepareBuild() throws IOException {

        ActualLoadProfileTable alpt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();

        int blockSize = LoadProfileBlockData.getSize(getTableFactory(), getTableIdentification().getTableId()-64, false);
        int blockSizeRest = blockSize;

        if (getIntervalsets() != -1)
            blockSizeRest = LoadProfileBlockData.getSize(getTableFactory(), getTableIdentification().getTableId()-64, getIntervalsets());

        int headerSize = LoadProfileBlockData.getSize(getTableFactory(), getTableIdentification().getTableId()-64, true);

        if (getNrOfBlocksToRequest() == -1) {
            setHeaderOnly(false);
            setNrOfBlocksToRequest(0);
            return;
        }
        else if (getNrOfBlocksToRequest() == 0)
            setHeaderOnly(true);
        else
            setHeaderOnly(false);

        if (getLoadProfileSetStatusCached().getIntervalOrder()==1) { // descending order
            PartialReadInfo partialReadInfo = new PartialReadInfo(blockSize*getBlockNrOffset(),isHeaderOnly()?headerSize:blockSizeRest*getNrOfBlocksToRequest());
            setPartialReadInfo(partialReadInfo);
        }
        else if (getLoadProfileSetStatusCached().getIntervalOrder()==0) { // ascending order
            if (isHeaderOnly() || (getIntervalsets() == -1)) {
                PartialReadInfo partialReadInfo = new PartialReadInfo(blockSize*getBlockNrOffset(),isHeaderOnly()?headerSize:blockSizeRest*getNrOfBlocksToRequest());
                setPartialReadInfo(partialReadInfo);
            }
            else {
                PartialReadInfo partialReadInfo = new PartialReadInfo(blockSize*getBlockNrOffset(),headerSize);
                setPartialReadInfo(partialReadInfo);

                // block
                //   end date
                //   simple status for each interval
                //   intervalset 0
                //   intervalset 1
                //   intervalset ...
                //   intervalset N-1

                int intervalSetSize = IntervalSet.getSize(getTableFactory(), getTableIdentification().getTableId()-64);
                int nrOfIntervalsInBlock;
                if (getBlockNrOffset() == getLoadProfileSetStatusCached().getLastBlockElement())
                    nrOfIntervalsInBlock = getLoadProfileSetStatusCached().getNrOfValidIntervals();
                else
                    nrOfIntervalsInBlock = alpt.getLoadProfileSet().getNrOfBlockIntervalsSet()[getTableIdentification().getTableId()-64];

                int skipIntervalSetsSize = (nrOfIntervalsInBlock - getIntervalsets())*intervalSetSize;
                // fill in also PartialReadInfo2 because we need to retrieve non contiguous blocks of data from the same table!
//System.out.println("SKIP OVER "+skipIntervalSetsSize+" bytes");
                partialReadInfo = new PartialReadInfo(blockSize*getBlockNrOffset()+headerSize+skipIntervalSetsSize,intervalSetSize*getIntervalsets());
                setPartialReadInfo2(partialReadInfo);
            }
        }
        else throw new IOException("AbstractLoadProfileDataSetTable, prepareBuild, invalid intervalOrder "+getLoadProfileSetStatusCached().getIntervalOrder());
    }

    public int getBlockNrOffset() {
        return blockNrOffset;
    }

    public void setBlockNrOffset(int blockNrOffset) {
        this.blockNrOffset = blockNrOffset;
    }

    public boolean isHeaderOnly() {
        return headerOnly;
    }

    private void setHeaderOnly(boolean headerOnly) {
        this.headerOnly = headerOnly;
    }

    public int getIntervalsets() {
        return intervalsets;
    }

    public void setIntervalsets(int intervalsets) {
        this.intervalsets = intervalsets;
    }
}
