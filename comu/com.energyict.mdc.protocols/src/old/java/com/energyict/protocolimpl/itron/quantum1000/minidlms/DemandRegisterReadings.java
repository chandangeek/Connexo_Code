/*
 * DemandRegisterReadings.java
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
public class DemandRegisterReadings extends AbstractDataDefinition {


    private DemandRegisterReadingsType[] demandRegisterReadingsTypes=null;

    /**
     * Creates a new instance of DemandRegisterReadings
     */
    public DemandRegisterReadings(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandRegisterReadings:\n");
        for (int i=0;i<getDemandRegisterReadingsTypes().length;i++) {
            strBuff.append("       demandRegisterReadingsTypes["+i+"]="+getDemandRegisterReadingsTypes()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 16; // DLMS_DEMAND_REGISTER_READINGS
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        int range = data.length/DemandRegisterReadingsType.size();

        demandRegisterReadingsTypes = new DemandRegisterReadingsType[range];

        for (int i=0;i<getDemandRegisterReadingsTypes().length;i++) {
            getDemandRegisterReadingsTypes()[i] = new DemandRegisterReadingsType(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone());
            offset += DemandRegisterReadingsType.size();
        }
    }

    public DemandRegisterReadingsType[] getDemandRegisterReadingsTypes() {
        return demandRegisterReadingsTypes;
    }

    public void setDemandRegisterReadingsTypes(DemandRegisterReadingsType[] demandRegisterReadingsTypes) {
        this.demandRegisterReadingsTypes = demandRegisterReadingsTypes;
    }
}
