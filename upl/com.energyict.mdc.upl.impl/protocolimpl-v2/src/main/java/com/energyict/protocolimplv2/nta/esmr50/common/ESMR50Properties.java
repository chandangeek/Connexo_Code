package com.energyict.protocolimplv2.nta.esmr50.common;


import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

public class ESMR50Properties extends DlmsProperties {

    @Override
    public boolean isNtaSimulationTool() {
        return true;
    }
}
