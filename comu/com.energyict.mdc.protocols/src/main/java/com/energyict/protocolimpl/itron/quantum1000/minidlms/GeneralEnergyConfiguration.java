/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * GeneralEnergyConfiguration.java
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
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class GeneralEnergyConfiguration extends AbstractDataDefinition {

    private BigDecimal energyRollOver; // 8 bytes
    private int testModeOption; // 1 byte
    private int numberOfTOUSummaries; // 1 byte
    private int numberofRatesPerTOUSummaries; // 1 byte

    /**
     * Creates a new instance of GeneralEnergyConfiguration
     */
    public GeneralEnergyConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GeneralEnergyConfiguration:\n");
        strBuff.append("   energyRollOver="+getEnergyRollOver()+"\n");
        strBuff.append("   numberOfTOUSummaries="+getNumberOfTOUSummaries()+"\n");
        strBuff.append("   numberofRatesPerTOUSummaries="+getNumberofRatesPerTOUSummaries()+"\n");
        strBuff.append("   testModeOption="+getTestModeOption()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 34; // DLMS_GENERAL_ENERGY_CONFIG
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        setEnergyRollOver(new BigDecimal(Double.longBitsToDouble(ProtocolUtils.getLong(data,offset))));
        offset+=8;
        setTestModeOption(ProtocolUtils.getInt(data,offset++,1));
        setNumberOfTOUSummaries(ProtocolUtils.getInt(data,offset++,1));
        setNumberofRatesPerTOUSummaries(ProtocolUtils.getInt(data,offset++,1));


    }

    public BigDecimal getEnergyRollOver() {
        return energyRollOver;
    }

    public void setEnergyRollOver(BigDecimal energyRollOver) {
        this.energyRollOver = energyRollOver;
    }

    public int getTestModeOption() {
        return testModeOption;
    }

    public void setTestModeOption(int testModeOption) {
        this.testModeOption = testModeOption;
    }

    public int getNumberOfTOUSummaries() {
        return numberOfTOUSummaries;
    }

    public void setNumberOfTOUSummaries(int numberOfTOUSummaries) {
        this.numberOfTOUSummaries = numberOfTOUSummaries;
    }

    public int getNumberofRatesPerTOUSummaries() {
        return numberofRatesPerTOUSummaries;
    }

    public void setNumberofRatesPerTOUSummaries(int numberofRatesPerTOUSummaries) {
        this.numberofRatesPerTOUSummaries = numberofRatesPerTOUSummaries;
    }

}
