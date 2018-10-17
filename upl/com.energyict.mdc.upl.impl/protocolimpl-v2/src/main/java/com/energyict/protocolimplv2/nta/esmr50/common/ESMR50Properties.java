package com.energyict.protocolimplv2.nta.esmr50.common;


import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;


public class ESMR50Properties extends DlmsProperties {

    private final PropertySpecService propertySpecService;

    public ESMR50Properties(PropertySpecService propertySpecService){
        this.propertySpecService = propertySpecService;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if(securityProvider == null){
            securityProvider = new ESMR50SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }
}
