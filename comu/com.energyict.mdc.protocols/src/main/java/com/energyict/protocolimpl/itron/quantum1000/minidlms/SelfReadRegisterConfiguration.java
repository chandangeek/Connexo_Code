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
public class SelfReadRegisterConfiguration extends AbstractDataDefinition {

    private SelfReadRegisterConfigurationType[] selfReadRegisterConfigurationTypes;

    /** Creates a new instance of GeneralDiagnosticInfo */
    public SelfReadRegisterConfiguration(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    protected int getVariableName() {
        return 0x0047; // 71 DLMS_SELF_READ_REG_CONFIGURATION
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        int range = data.length / SelfReadRegisterConfigurationType.size();
        setSelfReadRegisterConfigurationTypes(new SelfReadRegisterConfigurationType[range]);
        for (int i=0;i<getSelfReadRegisterConfigurationTypes().length;i++) {
            getSelfReadRegisterConfigurationTypes()[i] = new SelfReadRegisterConfigurationType(data,offset);
            offset+=SelfReadRegisterConfigurationType.size();
        }
    }

    public SelfReadRegisterConfigurationType[] getSelfReadRegisterConfigurationTypes() {
        return selfReadRegisterConfigurationTypes;
    }

    public void setSelfReadRegisterConfigurationTypes(SelfReadRegisterConfigurationType[] selfReadRegisterConfigurationTypes) {
        this.selfReadRegisterConfigurationTypes = selfReadRegisterConfigurationTypes;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadRegisterConfiguration:\n");
        for (int i=0;i<getSelfReadRegisterConfigurationTypes().length;i++) {
            strBuff.append("       selfReadRegisterConfigurationTypes["+i+"]="+getSelfReadRegisterConfigurationTypes()[i]+"\n");
        }
        return strBuff.toString();
    }
}
