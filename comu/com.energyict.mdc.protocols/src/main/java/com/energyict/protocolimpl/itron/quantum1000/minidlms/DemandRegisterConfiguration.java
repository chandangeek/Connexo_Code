/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class DemandRegisterConfiguration extends AbstractDataDefinition {

    private DemandRegister[] demandRegisters=null;

    /** Creates a new instance of GeneralDiagnosticInfo */
    public DemandRegisterConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandRegisterConfiguration:\n");
        for (int i=0;i<getDemandRegisters().length;i++) {
            strBuff.append("       demandRegisters["+i+"]="+getDemandRegisters()[i]+"\n");
        }
        return strBuff.toString();
    }

    public DemandRegister findDemandRegister(QuantityId qid, int demandType) throws IOException {

        for (int register=0;register<demandRegisters.length;register++) {
            if ((demandRegisters[register].getQuantityId().getId() == qid.getId()) || (demandRegisters[register].getDemandType()==demandType))
                return demandRegisters[register];
        }
        throw new IOException("DemandRegisterConfiguration, findDemandRegister, invalid quantity id "+qid+" and/or demand type "+demandType);
    }

    protected int getVariableName() {
        return 0x0020; // 32 // DLMS_DEMAND_REGISTER_CONFIG
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        MeterSetup meterSetup = getDataDefinitionFactory().getMeterSetup();
        int range = data.length / DemandRegister.size();
        setDemandRegisters(new DemandRegister[range]);
        for (int i=0;i<getDemandRegisters().length;i++) {
            getDemandRegisters()[i] = new DemandRegister(data,offset,meterSetup);
            offset += DemandRegister.size();
        }

    } // protected void parse(byte[] data) throws IOException

    public DemandRegister[] getDemandRegisters() {
        return demandRegisters;
    }

    public void setDemandRegisters(DemandRegister[] demandRegisters) {
        this.demandRegisters = demandRegisters;
    }
}
