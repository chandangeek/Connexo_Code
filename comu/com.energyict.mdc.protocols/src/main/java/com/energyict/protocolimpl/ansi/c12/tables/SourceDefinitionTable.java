/*
 * SourceDefinitionTable.java
 *
 * Created on 27 oktober 2005, 15:07
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
public class SourceDefinitionTable extends AbstractTable {

    private int[] sourceLinks;
    private boolean[] uomEntryFlag; // the next uom entry table 12  is accociated with this source
    private boolean[] demandControlFlag; // demand control table 13 is accociated with this source
    private boolean[] dataControlFlag; // data control table 14 is accociated with this source
    private boolean[] constantsFlag; // constants table 15 is accociated with this source
    private boolean[] pulseEngineeringFlag; // true: source is in engineering units, false: source is in pulse units
    private boolean[] constantToBeApplied; // false: the entry in the constants table 15 if present, has been applied to the source
                                 // true: the entry in the constants table 15 if present, has not been applied to the source

    /** Creates a new instance of SourceDefinitionTable */
    public SourceDefinitionTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(16));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SourceDefinitionTable: \n");
        for (int i=0;i<getSourceLinks().length;i++) {
            strBuff.append("    sourceLinks["+i+"]=0x"+Integer.toHexString(sourceLinks[i])+
                           ", uomEntryFlag["+i+"]="+getUomEntryFlag()[i]+
                           ", demandControlFlag["+i+"]="+getDemandControlFlag()[i]+
                           ", dataControlFlag["+i+"]="+getDataControlFlag()[i]+
                           ", constantsFlag["+i+"]="+getConstantsFlag()[i]+
                           ", pulseEngineeringFlag["+i+"]="+getPulseEngineeringFlag()[i]+
                           ", constantToBeApplied["+i+"]="+getConstantToBeApplied()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int nrOfEntries = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesSources();
        setSourceLinks(new int[nrOfEntries]);
        setUomEntryFlag(new boolean[nrOfEntries]);
        setDemandControlFlag(new boolean[nrOfEntries]);
        setDataControlFlag(new boolean[nrOfEntries]);
        setConstantsFlag(new boolean[nrOfEntries]);
        setPulseEngineeringFlag(new boolean[nrOfEntries]);
        setConstantToBeApplied(new boolean[nrOfEntries]);
        int offset=0;
        for (int i=0;i<getSourceLinks().length;i++) {
            getSourceLinks()[i] = C12ParseUtils.getInt(tableData,offset);
            getUomEntryFlag()[i] = (getSourceLinks()[i] & 0x01) == 0x01;
            getDemandControlFlag()[i] = (getSourceLinks()[i] & 0x02) == 0x02;
            getDataControlFlag()[i] = (getSourceLinks()[i] & 0x04) == 0x04;
            getConstantsFlag()[i] = (getSourceLinks()[i] & 0x08) == 0x08;
            getPulseEngineeringFlag()[i] = (getSourceLinks()[i] & 0x10) == 0x10;
            getConstantToBeApplied()[i] = (getSourceLinks()[i] & 0x20) == 0x20;
            offset++;
        }
    }

    public int[] getSourceLinks() {
        return sourceLinks;
    }

    public void setSourceLinks(int[] sourceLinks) {
        this.sourceLinks = sourceLinks;
    }

    public boolean[] getUomEntryFlag() {
        return uomEntryFlag;
    }

    public void setUomEntryFlag(boolean[] uomEntryFlag) {
        this.uomEntryFlag = uomEntryFlag;
    }

    public boolean[] getDemandControlFlag() {
        return demandControlFlag;
    }

    public void setDemandControlFlag(boolean[] demandControlFlag) {
        this.demandControlFlag = demandControlFlag;
    }

    public boolean[] getDataControlFlag() {
        return dataControlFlag;
    }

    public void setDataControlFlag(boolean[] dataControlFlag) {
        this.dataControlFlag = dataControlFlag;
    }

    public boolean[] getConstantsFlag() {
        return constantsFlag;
    }

    public void setConstantsFlag(boolean[] constantsFlag) {
        this.constantsFlag = constantsFlag;
    }

    public boolean[] getPulseEngineeringFlag() {
        return pulseEngineeringFlag;
    }

    public void setPulseEngineeringFlag(boolean[] pulseEngineeringFlag) {
        this.pulseEngineeringFlag = pulseEngineeringFlag;
    }

    public boolean[] getConstantToBeApplied() {
        return constantToBeApplied;
    }

    public void setConstantToBeApplied(boolean[] constantToBeApplied) {
        this.constantToBeApplied = constantToBeApplied;
    }

}
