/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EnergyRegistersReading.java
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
public class EnergyRegistersReading extends AbstractDataDefinition {

    private EnergyRegisterValue[] energyRegisterValues;

    /** Creates a new instance of EnergyRegistersReading */
    public EnergyRegistersReading(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergyRegistersReading:\n");
        for (int i=0;i<getEnergyRegisterValues().length;i++) {
            strBuff.append("       energyRegisterValues["+i+"]="+getEnergyRegisterValues()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 2;
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        int range=data.length/EnergyRegisterValue.size();
        setEnergyRegisterValues(new EnergyRegisterValue[range]);
        for (int i=0;i<getEnergyRegisterValues().length;i++) {
            getEnergyRegisterValues()[i] = new EnergyRegisterValue(data,offset);
            offset+=EnergyRegisterValue.size();
        }
    }

    public EnergyRegisterValue[] getEnergyRegisterValues() {
        return energyRegisterValues;
    }

    public void setEnergyRegisterValues(EnergyRegisterValue[] energyRegisterValues) {
        this.energyRegisterValues = energyRegisterValues;
    }
}
