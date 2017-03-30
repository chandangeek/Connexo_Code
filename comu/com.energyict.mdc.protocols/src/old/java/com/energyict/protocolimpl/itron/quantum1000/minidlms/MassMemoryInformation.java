/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MassMemoryInformation.java
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
public class MassMemoryInformation extends AbstractDataDefinition {

    private MassMemoryInfoType massMemoryInfoType;

    /**
     * Creates a new instance of MassMemoryInformation
     */
    public MassMemoryInformation(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryInformation:\n");
        strBuff.append("   massMemoryInfoType="+getMassMemoryInfoType()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x003e; // 62 DLMS_MASS_MEM_INFO
    }

    protected void parse(byte[] data) throws IOException {
        setMassMemoryInfoType(new MassMemoryInfoType(data, 0, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
    }

    public MassMemoryInfoType getMassMemoryInfoType() {
        return massMemoryInfoType;
    }

    public void setMassMemoryInfoType(MassMemoryInfoType massMemoryInfoType) {
        this.massMemoryInfoType = massMemoryInfoType;
    }
}
