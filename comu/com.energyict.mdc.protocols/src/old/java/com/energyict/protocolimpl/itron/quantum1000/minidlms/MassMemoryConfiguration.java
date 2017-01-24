/*
 * GeneralDiagnosticInfo.java
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
public class MassMemoryConfiguration extends AbstractDataDefinition {

    private MassMemoryConfigType massMemoryConfigType;

    /** Creates a new instance of GeneralDiagnosticInfo */
    public MassMemoryConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryConfiguration:\n");
        strBuff.append("   massMemoryConfigType="+getMassMemoryConfigType()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x003d; // 61 MASS_MEMORY_CONFIGURATION
    }

    protected void parse(byte[] data) throws IOException {

        if (data.length == MassMemoryConfigType.size())
            setMassMemoryConfigType(new MassMemoryConfigType(data, 0, false));
        else
            setMassMemoryConfigType(new MassMemoryConfigType(data, 0, true));
    }

    public MassMemoryConfigType getMassMemoryConfigType() {
        return massMemoryConfigType;
    }

    public void setMassMemoryConfigType(MassMemoryConfigType massMemoryConfigType) {
        this.massMemoryConfigType = massMemoryConfigType;
    }
}
