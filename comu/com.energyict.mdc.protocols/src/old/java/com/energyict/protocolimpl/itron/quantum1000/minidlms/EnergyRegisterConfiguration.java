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
public class EnergyRegisterConfiguration extends AbstractDataDefinition {

    private EnergyRegister[] registerConfig = new EnergyRegister[30];

    /** Creates a new instance of GeneralDiagnosticInfo */
    public EnergyRegisterConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EnergyRegisterConfiguration:\n");
        for (int i=0;i<getRegisterConfig().length;i++) {
            strBuff.append("       registerConfig["+i+"]="+getRegisterConfig()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x0019; // 25 DLMS_ENERGY_REGISTERS_CONFIG
    }

    public EnergyRegister findEnergyRegister(QuantityId qid) throws IOException {

        for (int register=0;register<registerConfig.length;register++) {
            if (registerConfig[register].getQuantityId().getId() == qid.getId())
                return registerConfig[register];
        }
        throw new IOException("EnergyRegisterConfiguration, findEnergyRegister, invalid quantity id "+qid);
    }


    protected void parse(byte[] data) throws IOException {

        MeterSetup meterSetup = getDataDefinitionFactory().getMeterSetup();

        int offset=0;
        for (int i=0;i<getRegisterConfig().length;i++) {
            getRegisterConfig()[i] = new EnergyRegister(data, offset, meterSetup);
            offset+=EnergyRegister.size();
        }

    }

    public EnergyRegister[] getRegisterConfig() {
        return registerConfig;
    }

    public void setRegisterConfig(EnergyRegister[] registerConfig) {
        this.registerConfig = registerConfig;
    }


}
