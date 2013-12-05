/*
 * MassMemory.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MassMemory extends NextViewableFileRecord {

    private MassMemoryRecord[] massMemoryRecords;

    /**
     * Creates a new instance of MassMemory
     */
    public MassMemory(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemory:\n");
        strBuff.append("   nextViewableFileRecord currentRecordNumber="+getCurrentRecordNumber()+"\n");
        strBuff.append("   nextViewableFileRecord numberRecords="+getNumberRecords()+"\n");
        for (int i=0;i<getMassMemoryRecords().length;i++) {
            strBuff.append("       massMemoryRecords["+i+"]="+getMassMemoryRecords()[i]+"\n");
        }

        return strBuff.toString();
    }

    protected void parseData(byte[] data) throws IOException {
        int offset=0;
        int nrOfChannels=0;
        setMassMemoryRecords(new MassMemoryRecord[(int)getNumberRecords()]);

        if (getDataDefinitionFactory().getViewInformation().getCurrentObjectID().isMassMemory1())
           nrOfChannels = getDataDefinitionFactory().getMassMemoryConfiguration(0).getMassMemoryConfigType().getNumberOfChannels();
        else if (getDataDefinitionFactory().getViewInformation().getCurrentObjectID().isMassMemory2())
           nrOfChannels = getDataDefinitionFactory().getMassMemoryConfiguration(1).getMassMemoryConfigType().getNumberOfChannels();
        else throw new IOException("MassMemory, parseData, invalid selected viewable file id "+getDataDefinitionFactory().getViewInformation().getCurrentObjectID());

        for (int i=0;i<getMassMemoryRecords().length;i++) {
            getMassMemoryRecords()[i] = new MassMemoryRecord(data,offset,getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone(),nrOfChannels);
            offset+=MassMemoryRecord.size(nrOfChannels);
        }
    }

    public MassMemoryRecord[] getMassMemoryRecords() {
        return massMemoryRecords;
    }

    public void setMassMemoryRecords(MassMemoryRecord[] massMemoryRecords) {
        this.massMemoryRecords = massMemoryRecords;
    }

}
