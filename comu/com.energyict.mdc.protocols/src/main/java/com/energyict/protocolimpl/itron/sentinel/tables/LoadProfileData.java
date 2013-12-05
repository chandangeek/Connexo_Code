/*
 * WriteOnlyTable2049.java
 *
 * Created on 02112006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfileData extends AbstractTable {

    private int blockId;
    private int blocks;
    private boolean headerOnly;

    private LoadProfileBlock[] loadProfileBlocks;

    /** Creates a new instance of WriteOnlyTable2049 */
    public LoadProfileData(ManufacturerTableFactory manufacturerTableFactory) {
        //super(manufacturerTableFactory,new TableIdentification(1,true)); // alternative way of declaration
        super(manufacturerTableFactory,new TableIdentification(2055));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileData:\n");
        for (int i=0;i<getLoadProfileBlocks().length;i++) {
            strBuff.append("       loadProfileBlocks["+i+"]="+getLoadProfileBlocks()[i]+"\n");
        }
        strBuff.append("   blockId="+getBlockId()+"\n");
        strBuff.append("   blocks="+getBlocks()+"\n");
        strBuff.append("   headerOnly="+isHeaderOnly()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;
        int dataOrder = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int nrOfChannels = getManufacturerTableFactory().getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels();
        int blockSize = 264*nrOfChannels+260;
        setLoadProfileBlocks(new LoadProfileBlock[getBlocks()]);
        for (int block=0;block<getLoadProfileBlocks().length;block++) {
            getLoadProfileBlocks()[block] = new LoadProfileBlock(tableData, offset, getManufacturerTableFactory(), headerOnly);

            offset+=LoadProfileBlock.getSize(getManufacturerTableFactory());
        }
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    protected void prepareBuild() throws IOException {
        int blockSize=264 * getManufacturerTableFactory().getC12ProtocolLink().getNumberOfChannels()+260;
        int headerSize=8*getManufacturerTableFactory().getC12ProtocolLink().getNumberOfChannels()+4;
        PartialReadInfo partialReadInfo = new PartialReadInfo(blockSize*getBlockId(), isHeaderOnly()?headerSize:blockSize*getBlocks());
        setPartialReadInfo(partialReadInfo);
    }

    public LoadProfileBlock[] getLoadProfileBlocks() {
        return loadProfileBlocks;
    }

    public void setLoadProfileBlocks(LoadProfileBlock[] loadProfileBlocks) {
        this.loadProfileBlocks = loadProfileBlocks;
    }

    public int getBlocks() {
        return blocks;
    }

    public void setBlocks(int blocks) {
        this.blocks = blocks;
    }

    public boolean isHeaderOnly() {
        return headerOnly;
    }

    public void setHeaderOnly(boolean headerOnly) {
        this.headerOnly = headerOnly;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }




}
