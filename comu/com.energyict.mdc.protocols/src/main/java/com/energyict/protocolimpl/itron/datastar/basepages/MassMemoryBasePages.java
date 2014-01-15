/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class MassMemoryBasePages extends AbstractBasePage {



    private int endOfIntervalDataOffset;
    private int firstRamLocation;

    private int lastRamLocation;
    private int startOffsetOfCurrentRecord;
    private int currentIntervalNr;


    //Date lastInterrogateTimestamp;
    //Date auxInput1ClosureTimestamp;
    //Date auxInput2ClosureTimestamp;

    /** Creates a new instance of RealTimeBasePage */
    public MassMemoryBasePages(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryBasePages:\n");
        strBuff.append("   endOfIntervalDataOffset=0x"+Integer.toHexString(getEndOfIntervalDataOffset())+"\n");
        strBuff.append("   currentIntervalNr="+getCurrentIntervalNr()+"\n");
        strBuff.append("   firstRamLocation=0x"+Integer.toHexString(getFirstRamLocation())+"\n");
        strBuff.append("   lastRamLocation=0x"+Integer.toHexString(getLastRamLocation())+"\n");
        strBuff.append("   startOffsetOfCurrentRecord=0x"+Integer.toHexString(getStartOffsetOfCurrentRecord())+"\n");
        strBuff.append("   getMassMemoryStartOffset()=0x"+Integer.toHexString(getMassMemoryStartOffset())+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x70,39);
    }

    public int getMassMemoryStartOffset() {
        return 0x100;
    }

    public int getMassMemoryRecordLength() throws IOException {
        int nrOfChannels = ((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().getNrOfChannels();
        if (nrOfChannels == 1) return 125-18;
        if (nrOfChannels == 2) return 215-12;
        if (nrOfChannels == 4) return 395;
        throw new IOException("MassMemoryBasePages, getMassMemoryRecordLength, invalid nr of channels "+nrOfChannels);
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setEndOfIntervalDataOffset(ProtocolUtils.getInt(data,offset, 3)-getBasePagesFactory().getMemStartAddress());
        offset+=3;

        offset+=9; // reserved

        setFirstRamLocation(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        setLastRamLocation(ProtocolUtils.getInt(data,offset, 3));
        offset+=3;
        setStartOffsetOfCurrentRecord(ProtocolUtils.getInt(data,offset, 3)-getBasePagesFactory().getMemStartAddress());
        offset+=3;
        setCurrentIntervalNr(ProtocolUtils.getInt(data,offset, 1));
        offset++;;
        //lastInterrogateTimestamp;
        //auxInput1ClosureTimestamp;
        //auxInput2ClosureTimestamp;
    }

    private int getFirstRamLocation() {
        return firstRamLocation;
    }

    public void setFirstRamLocation(int firstRamLocation) {
        this.firstRamLocation = firstRamLocation;
    }

    private int getLastRamLocation() {
        return lastRamLocation;
    }

    public void setLastRamLocation(int lastRamLocation) {
        this.lastRamLocation = lastRamLocation;
    }

    public int getStartOffsetOfCurrentRecord() {
        return startOffsetOfCurrentRecord;
    }

    public void setStartOffsetOfCurrentRecord(int startOffsetOfCurrentRecord) {
        this.startOffsetOfCurrentRecord = startOffsetOfCurrentRecord;
    }

    public int getCurrentIntervalNr() {
        return currentIntervalNr;
    }

    public void setCurrentIntervalNr(int currentIntervalNr) {
        this.currentIntervalNr = currentIntervalNr;
    }

    public int getEndOfIntervalDataOffset() {
        return endOfIntervalDataOffset;
    }

    public void setEndOfIntervalDataOffset(int endOfIntervalDataOffset) {
        this.endOfIntervalDataOffset = endOfIntervalDataOffset;
    }



} // public class RealTimeBasePage extends AbstractBasePage
