/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ActualSourcesLimitingTable.java
 *
 * Created on 26 oktober 2005, 14:50
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
public class ActualSourcesLimitingTable extends AbstractTable {

    private int sourceFlagsBitfield; // 1 byte

    private boolean powerfailExcludeFlag; // bit 0 end device is (=0 not) capable of excluding powerfail flag
    private boolean resetExcludeFlag; // bit 1 reset exclusion is (=0 not) supported by the end device
    private boolean blockDemand; // bit 2 block demand is (=0 not) supported by the end device
    private boolean slidingDemand; // bit 3 sliding demand is (=0 not) supported by the end device
    private boolean thermalDemand; // bit 4 thermal demand is (=0 not) supported by the end device
    private boolean set1Present; // bit 5 the end device does (=0 not) support the first set of optional constants in the electrical record of the constants table 15
    private boolean set2Present; // bit 6 the end device does (=0 not) support the second set of optional constants in the electrical record of the constants table 15
    // bit 7 reserved
    private int maxNrOfEntriesUOMEntry; // Max nr of entries in the UOM entry table 122;
    private int maxNrOfEntriesDemandControl; // Max nr of entries in the Demand control table 13;
    private int dataControlLength; // Manufacturer supplied value that determines the width in octets of an entry in the first array of the data control table 14
    private int maxNrOfEntriesDataControl; // Max nr of entries in the Data control table 14;
    private int maxNrOfEntriesConstants; // Max nr of entries in the Constants table 15;
    private int constantsSelector; // constants table 15 record structure 0=gas aga3, 1=gas aga7, 2=electrical
    private int maxNrOfEntriesSources; // Max nr of entries in the Sources table 16

    /** Creates a new instance of ActualSourcesLimitingTable */
    public ActualSourcesLimitingTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(11));
    }

    public String toString() {
        return "ActualSourcesLimitingTable: sourceFlagsBitfield=0x"+Integer.toHexString(getSourceFlagsBitfield())+
                ", maxNrOfEntriesUOMEntry="+getMaxNrOfEntriesUOMEntry()+
                ", maxNrOfEntriesDemandControl="+getMaxNrOfEntriesDemandControl()+
                ", dataControlLength="+getDataControlLength()+
                ", maxNrOfEntriesDataControl="+getMaxNrOfEntriesDataControl()+
                ", maxNrOfEntriesConstants="+getMaxNrOfEntriesConstants()+
                ", constantsSelector="+getConstantsSelector()+
                ", maxNrOfEntriesSources="+getMaxNrOfEntriesSources()+"\n";
    }

    protected void parse(byte[] tableData) throws IOException {

        setSourceFlagsBitfield(C12ParseUtils.getInt(tableData,0));

        setPowerfailExcludeFlag((getSourceFlagsBitfield()&0x01)==0x01);
        setResetExcludeFlag((getSourceFlagsBitfield()&0x02)==0x02);
        setBlockDemand((getSourceFlagsBitfield()&0x04)==0x04);
        setSlidingDemand((getSourceFlagsBitfield()&0x08)==0x08);
        setThermalDemand((getSourceFlagsBitfield()&0x10)==0x10);
        setSet1Present((getSourceFlagsBitfield()&0x20)==0x20);
        setSet2Present((getSourceFlagsBitfield()&0x40)==0x40);

        setMaxNrOfEntriesUOMEntry(C12ParseUtils.getInt(tableData,1));
        setMaxNrOfEntriesDemandControl(C12ParseUtils.getInt(tableData,2));
        setDataControlLength(C12ParseUtils.getInt(tableData,3));
        setMaxNrOfEntriesDataControl(C12ParseUtils.getInt(tableData,4));
        setMaxNrOfEntriesConstants(C12ParseUtils.getInt(tableData,5));
        setConstantsSelector(C12ParseUtils.getInt(tableData,6));
        setMaxNrOfEntriesSources(C12ParseUtils.getInt(tableData,7));

    }

    public int getSourceFlagsBitfield() {
        return sourceFlagsBitfield;
    }

    public void setSourceFlagsBitfield(int sourceFlagsBitfield) {
        this.sourceFlagsBitfield = sourceFlagsBitfield;
    }

    public int getMaxNrOfEntriesUOMEntry() {
        return maxNrOfEntriesUOMEntry;
    }

    public void setMaxNrOfEntriesUOMEntry(int maxNrOfEntriesUOMEntry) {
        this.maxNrOfEntriesUOMEntry = maxNrOfEntriesUOMEntry;
    }

    public int getMaxNrOfEntriesDemandControl() {
        return maxNrOfEntriesDemandControl;
    }

    public void setMaxNrOfEntriesDemandControl(int maxNrOfEntriesDemandControl) {
        this.maxNrOfEntriesDemandControl = maxNrOfEntriesDemandControl;
    }

    public int getDataControlLength() {
        return dataControlLength;
    }

    public void setDataControlLength(int dataControlLength) {
        this.dataControlLength = dataControlLength;
    }

    public int getMaxNrOfEntriesDataControl() {
        return maxNrOfEntriesDataControl;
    }

    public void setMaxNrOfEntriesDataControl(int maxNrOfEntriesDataControl) {
        this.maxNrOfEntriesDataControl = maxNrOfEntriesDataControl;
    }

    public int getMaxNrOfEntriesConstants() {
        return maxNrOfEntriesConstants;
    }

    public void setMaxNrOfEntriesConstants(int maxNrOfEntriesConstants) {
        this.maxNrOfEntriesConstants = maxNrOfEntriesConstants;
    }

    public int getConstantsSelector() {
        return constantsSelector;
    }

    public void setConstantsSelector(int constantsSelector) {
        this.constantsSelector = constantsSelector;
    }

    public int getMaxNrOfEntriesSources() {
        return maxNrOfEntriesSources;
    }

    public void setMaxNrOfEntriesSources(int maxNrOfEntriesSources) {
        this.maxNrOfEntriesSources = maxNrOfEntriesSources;
    }

    public boolean isPowerfailExcludeFlag() {
        return powerfailExcludeFlag;
    }

    public void setPowerfailExcludeFlag(boolean powerfailExcludeFlag) {
        this.powerfailExcludeFlag = powerfailExcludeFlag;
    }

    public boolean isResetExcludeFlag() {
        return resetExcludeFlag;
    }

    public void setResetExcludeFlag(boolean resetExcludeFlag) {
        this.resetExcludeFlag = resetExcludeFlag;
    }

    public boolean isBlockDemand() {
        return blockDemand;
    }

    public void setBlockDemand(boolean blockDemand) {
        this.blockDemand = blockDemand;
    }

    public boolean isSlidingDemand() {
        return slidingDemand;
    }

    public void setSlidingDemand(boolean slidingDemand) {
        this.slidingDemand = slidingDemand;
    }

    public boolean isThermalDemand() {
        return thermalDemand;
    }

    public void setThermalDemand(boolean thermalDemand) {
        this.thermalDemand = thermalDemand;
    }

    public boolean isSet1Present() {
        return set1Present;
    }

    public void setSet1Present(boolean set1Present) {
        this.set1Present = set1Present;
    }

    public boolean isSet2Present() {
        return set2Present;
    }

    public void setSet2Present(boolean set2Present) {
        this.set2Present = set2Present;
    }
}
