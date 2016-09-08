/*
 * AbstractLoadProfileDataSetTable.java
 *
 * Created on 8 november 2005, 11:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;
import com.energyict.protocolimpl.ansi.c12.tables.*;

import java.io.IOException;

/**
 * @author James Fox
 */
public abstract class AbstractLoadProfileDataSetTable extends com.energyict.protocolimpl.ansi.c12.tables.AbstractLoadProfileDataSetTable {

    private boolean headerOnly;
    private int count;
    private int chunkSize;
            
    /** Creates a new instance of AbstractLoadProfileDataSetTable */
    public AbstractLoadProfileDataSetTable(StandardTableFactory tableFactory, int set) {
        super(tableFactory,set);
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    protected void prepareBuild() throws IOException {
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
                int intervalSetSize = IntervalSet.getSize(getTableFactory(), getTableIdentification().getTableId()-64);
                int additionalOffset = count * chunkSize * intervalSetSize;
                partialReadInfo = new PartialReadInfo(blockSize*getBlockNrOffset()+headerSize+additionalOffset,intervalSetSize*getIntervalsets());
                setPartialReadInfo2(partialReadInfo);
            }
        }
        else throw new IOException("AbstractLoadProfileDataSetTable, prepareBuild, invalid intervalOrder "+getLoadProfileSetStatusCached().getIntervalOrder());
    }

    public boolean isHeaderOnly() {
        return headerOnly;
    }

    private void setHeaderOnly(boolean headerOnly) {
        this.headerOnly = headerOnly;
    }
}
