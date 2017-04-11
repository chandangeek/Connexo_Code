/*
 * RegisterData.java
 *
 * Created on 28 oktober 2005, 15:33
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
public class RegisterData {
    
    private int nrDemandResets;
    private DataBlock totDatablock;
    private DataBlock[] tierDataBlocks;

    public RegisterData(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        this(data, offset, tableFactory, true, true);
    }
    /** Creates a new instance of RegisterData */
    public RegisterData(byte[] data,int offset,TableFactory tableFactory, boolean readDemandsAndCoincidents, boolean readTiers) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        if (art.isDemandResetControlFlag()) {
            setNrDemandResets(C12ParseUtils.getInt(data,offset));
            offset++;
        }
        setTotDatablock(new DataBlock(data,offset, tableFactory, readDemandsAndCoincidents));
        offset+=DataBlock.getSize(tableFactory);

        if (readTiers) {
            setTierDataBlocks(new DataBlock[art.getNrOfTiers()]);
            for (int i = 0; i < getTierDataBlocks().length; i++) {
                //            if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
                //                if (((data.length-offset) + 1) >= DataBlock.getSize(tableFactory))
                //                    getTierDataBlocks()[i] = new DataBlock(data,offset, tableFactory);
                //                else
                //                    break;
                //            }
                //            else
                getTierDataBlocks()[i] = new DataBlock(data, offset, tableFactory, readDemandsAndCoincidents);
                offset += DataBlock.getSize(tableFactory);
            }
        }
    }


    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterData: \n");

        strBuff.append("    nrDemandResets="+getNrDemandResets()+"\n");
        strBuff.append("    totDatablock="+getTotDatablock()+"\n");
        for(int i=0;i<getTierDataBlocks().length;i++) {
            strBuff.append("    tierDataBlocks["+i+"]="+getTierDataBlocks()[i]+"\n");
        }

        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return getSize(tableFactory, false, true);
    }

    static public int getSize(TableFactory tableFactory, boolean limitRegisterReadSize, boolean readTiers) throws IOException {
        int size = 0;
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();

        if (art.isDemandResetControlFlag()) {
            size += 1;
        }
        if (!limitRegisterReadSize) {
            size += DataBlock.getSize(tableFactory);
        } else {
            size += 64;
        }
        if (readTiers) {
            size += (art.getNrOfTiers() * DataBlock.getSize(tableFactory));
        }
        return size;
    }

    public int getNrDemandResets() {
        return nrDemandResets;
    }

    public void setNrDemandResets(int nrDemandResets) {
        this.nrDemandResets = nrDemandResets;
    }

    public DataBlock getTotDatablock() {
        return totDatablock;
    }

    public void setTotDatablock(DataBlock totDatablock) {
        this.totDatablock = totDatablock;
    }

    public DataBlock[] getTierDataBlocks() {
        return tierDataBlocks;
    }

    public void setTierDataBlocks(DataBlock[] tierDataBlocks) {
        this.tierDataBlocks = tierDataBlocks;
    }
}
