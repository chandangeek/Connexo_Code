/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfilePreliminaryDataRead extends AbstractDataRead {

    private int loadProfileIntervalLength; // in minutes
    private int loadProfileIntervalData; // Type (0 = UINT16, 1 = INT16)
    private int indexOfLastLoadProfileBlock;
    private int numberOfLoadProfileIntervalsInTheLastBlock;

    /** Creates a new instance of ConstantsDataRead */
    public LoadProfilePreliminaryDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }



    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfilePreliminaryDataRead:\n");
        strBuff.append("   indexOfLastLoadProfileBlock="+getIndexOfLastLoadProfileBlock()+"\n");
        strBuff.append("   loadProfileIntervalData="+getLoadProfileIntervalData()+"\n");
        strBuff.append("   loadProfileIntervalLength="+getLoadProfileIntervalLength()+"\n");
        strBuff.append("   numberOfLoadProfileIntervalsInTheLastBlock="+getNumberOfLoadProfileIntervalsInTheLastBlock()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setLoadProfileIntervalLength(C12ParseUtils.getInt(data,offset++));
        setLoadProfileIntervalData(C12ParseUtils.getInt(data,offset++));
        setIndexOfLastLoadProfileBlock(C12ParseUtils.getInt(data,offset++));
        setNumberOfLoadProfileIntervalsInTheLastBlock(C12ParseUtils.getInt(data,offset++));

    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("LP_INTERVAL_LENGTH").getId(),
                                 LogicalIDFactory.findLogicalId("LP_INTERVAL_DATA_TYPE").getId(),
                                 LogicalIDFactory.findLogicalId("LP_LAST_VALID_BLOCK_INDEX").getId(),
                                 LogicalIDFactory.findLogicalId("LP_LAST_BLOCK_INTERVALS").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x04, lids));

    } // protected void prepareBuild() throws IOException

    public int getLoadProfileIntervalLength() {
        return loadProfileIntervalLength;
    }

    public void setLoadProfileIntervalLength(int loadProfileIntervalLength) {
        this.loadProfileIntervalLength = loadProfileIntervalLength;
    }

    public int getLoadProfileIntervalData() {
        return loadProfileIntervalData;
    }

    public void setLoadProfileIntervalData(int loadProfileIntervalData) {
        this.loadProfileIntervalData = loadProfileIntervalData;
    }

    public int getIndexOfLastLoadProfileBlock() {
        return indexOfLastLoadProfileBlock;
    }

    public void setIndexOfLastLoadProfileBlock(int indexOfLastLoadProfileBlock) {
        this.indexOfLastLoadProfileBlock = indexOfLastLoadProfileBlock;
    }

    public int getNumberOfLoadProfileIntervalsInTheLastBlock() {
        return numberOfLoadProfileIntervalsInTheLastBlock;
    }

    public void setNumberOfLoadProfileIntervalsInTheLastBlock(int numberOfLoadProfileIntervalsInTheLastBlock) {
        this.numberOfLoadProfileIntervalsInTheLastBlock = numberOfLoadProfileIntervalsInTheLastBlock;
    }

} // public class ConstantsDataRead extends AbstractDataRead
