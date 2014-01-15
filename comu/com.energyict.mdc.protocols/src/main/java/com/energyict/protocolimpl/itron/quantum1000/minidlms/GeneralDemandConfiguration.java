/*
 * GeneralDemandConfiguration.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class GeneralDemandConfiguration extends AbstractDataDefinition {

    private int numberOfTOUSummaries;// unsigned8;
    private int numberOfMultiplePeaksOrMinimumsSummaries;// unsigned16;
    private int numberOfRatesPerTOUSummary;// unsigned8;
    private int normalModeIntervalLength;// unsigned16; --in seconds
    private int testModeIntervalLength;// unsigned16; --in seconds
    private int normalModeNumberOfSubintervals;// unsigned 8; --1..15
    private int testModeNumberOfSubintervals;// unsigned8; --1..15
    private int normalModeThermalIntervalLength;// unsigned16; --in seconds
    private int testModeThermalIntervalLength;// unsigned16; --in seconds
    private int coldLoadPickUpTime;// unsigned16; --in seconds
    private int minimumOutageTim;// unsigned16; --in seconds
    private int demandResetLockOutTime;// unsigned16; --in seconds
    private boolean useGlobalEOISource;// boolean;
    private int numberOfExtremaPerMPM;// unsigned16,


    /**
     * Creates a new instance of GeneralDemandConfiguration
     */
    public GeneralDemandConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GeneralDemandConfiguration:\n");
        strBuff.append("   coldLoadPickUpTime="+getColdLoadPickUpTime()+"\n");
        strBuff.append("   demandResetLockOutTime="+getDemandResetLockOutTime()+"\n");
        strBuff.append("   minimumOutageTim="+getMinimumOutageTim()+"\n");
        strBuff.append("   normalModeIntervalLength="+getNormalModeIntervalLength()+"\n");
        strBuff.append("   normalModeNumberOfSubintervals="+getNormalModeNumberOfSubintervals()+"\n");
        strBuff.append("   normalModeThermalIntervalLength="+getNormalModeThermalIntervalLength()+"\n");
        strBuff.append("   numberOfExtremaPerMPM="+getNumberOfExtremaPerMPM()+"\n");
        strBuff.append("   numberOfMultiplePeaksOrMinimumsSummaries="+getNumberOfMultiplePeaksOrMinimumsSummaries()+"\n");
        strBuff.append("   numberOfRatesPerTOUSummary="+getNumberOfRatesPerTOUSummary()+"\n");
        strBuff.append("   numberOfTOUSummaries="+getNumberOfTOUSummaries()+"\n");
        strBuff.append("   testModeIntervalLength="+getTestModeIntervalLength()+"\n");
        strBuff.append("   testModeNumberOfSubintervals="+getTestModeNumberOfSubintervals()+"\n");
        strBuff.append("   testModeThermalIntervalLength="+getTestModeThermalIntervalLength()+"\n");
        strBuff.append("   useGlobalEOISource="+isUseGlobalEOISource()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 15; // DLMS_GENERAL_DEMAND_CONFIG
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setNumberOfTOUSummaries(ProtocolUtils.getInt(data,offset++,1));
        setNumberOfMultiplePeaksOrMinimumsSummaries(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setNumberOfRatesPerTOUSummary(ProtocolUtils.getInt(data,offset++,1));
        setNormalModeIntervalLength(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setTestModeIntervalLength(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setNormalModeNumberOfSubintervals(ProtocolUtils.getInt(data,offset++,1));
        setTestModeNumberOfSubintervals(ProtocolUtils.getInt(data,offset++,1));
        setNormalModeThermalIntervalLength(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setTestModeThermalIntervalLength(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setColdLoadPickUpTime(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setMinimumOutageTim(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setDemandResetLockOutTime(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setUseGlobalEOISource((ProtocolUtils.getInt(data,offset++,1) == 1));
        setNumberOfExtremaPerMPM(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
    }

    public int getNumberOfTOUSummaries() {
        return numberOfTOUSummaries;
    }

    public void setNumberOfTOUSummaries(int numberOfTOUSummaries) {
        this.numberOfTOUSummaries = numberOfTOUSummaries;
    }

    public int getNumberOfMultiplePeaksOrMinimumsSummaries() {
        return numberOfMultiplePeaksOrMinimumsSummaries;
    }

    public void setNumberOfMultiplePeaksOrMinimumsSummaries(int numberOfMultiplePeaksOrMinimumsSummaries) {
        this.numberOfMultiplePeaksOrMinimumsSummaries = numberOfMultiplePeaksOrMinimumsSummaries;
    }

    public int getNumberOfRatesPerTOUSummary() {
        return numberOfRatesPerTOUSummary;
    }

    public void setNumberOfRatesPerTOUSummary(int numberOfRatesPerTOUSummary) {
        this.numberOfRatesPerTOUSummary = numberOfRatesPerTOUSummary;
    }

    public int getNormalModeIntervalLength() {
        return normalModeIntervalLength;
    }

    public void setNormalModeIntervalLength(int normalModeIntervalLength) {
        this.normalModeIntervalLength = normalModeIntervalLength;
    }

    public int getTestModeIntervalLength() {
        return testModeIntervalLength;
    }

    public void setTestModeIntervalLength(int testModeIntervalLength) {
        this.testModeIntervalLength = testModeIntervalLength;
    }

    public int getNormalModeNumberOfSubintervals() {
        return normalModeNumberOfSubintervals;
    }

    public void setNormalModeNumberOfSubintervals(int normalModeNumberOfSubintervals) {
        this.normalModeNumberOfSubintervals = normalModeNumberOfSubintervals;
    }

    public int getTestModeNumberOfSubintervals() {
        return testModeNumberOfSubintervals;
    }

    public void setTestModeNumberOfSubintervals(int testModeNumberOfSubintervals) {
        this.testModeNumberOfSubintervals = testModeNumberOfSubintervals;
    }

    public int getNormalModeThermalIntervalLength() {
        return normalModeThermalIntervalLength;
    }

    public void setNormalModeThermalIntervalLength(int normalModeThermalIntervalLength) {
        this.normalModeThermalIntervalLength = normalModeThermalIntervalLength;
    }

    public int getTestModeThermalIntervalLength() {
        return testModeThermalIntervalLength;
    }

    public void setTestModeThermalIntervalLength(int testModeThermalIntervalLength) {
        this.testModeThermalIntervalLength = testModeThermalIntervalLength;
    }

    public int getColdLoadPickUpTime() {
        return coldLoadPickUpTime;
    }

    public void setColdLoadPickUpTime(int coldLoadPickUpTime) {
        this.coldLoadPickUpTime = coldLoadPickUpTime;
    }

    public int getMinimumOutageTim() {
        return minimumOutageTim;
    }

    public void setMinimumOutageTim(int minimumOutageTim) {
        this.minimumOutageTim = minimumOutageTim;
    }

    public int getDemandResetLockOutTime() {
        return demandResetLockOutTime;
    }

    public void setDemandResetLockOutTime(int demandResetLockOutTime) {
        this.demandResetLockOutTime = demandResetLockOutTime;
    }

    public boolean isUseGlobalEOISource() {
        return useGlobalEOISource;
    }

    public void setUseGlobalEOISource(boolean useGlobalEOISource) {
        this.useGlobalEOISource = useGlobalEOISource;
    }

    public int getNumberOfExtremaPerMPM() {
        return numberOfExtremaPerMPM;
    }

    public void setNumberOfExtremaPerMPM(int numberOfExtremaPerMPM) {
        this.numberOfExtremaPerMPM = numberOfExtremaPerMPM;
    }
}
