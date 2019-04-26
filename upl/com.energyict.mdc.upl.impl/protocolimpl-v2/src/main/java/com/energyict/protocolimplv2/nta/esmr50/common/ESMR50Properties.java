package com.energyict.protocolimplv2.nta.esmr50.common;


import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;


public class ESMR50Properties extends DlmsProperties {

    private final PropertySpecService propertySpecService;
    private final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    public ESMR50Properties(PropertySpecService propertySpecService){
        this.propertySpecService = propertySpecService;
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(ESMR50ConfigurationSupport.READCACHE_PROPERTY, false);
    }

    public boolean usesPublicClient() {
        return getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if(securityProvider == null){
            securityProvider = new ESMR50SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    public String getDefaultKey() {
        return getProperties().getTypedProperty(ESMR50ConfigurationSupport.DEFAULT_KEY, null).toString();
    }

    public String getFUAK() {
        return getProperties().getTypedProperty(ESMR50ConfigurationSupport.FIRMWARE_UPGRADE_AUTHENTICATION_KEY, null).toString();
    }

    public String getWorkingKeyLabelPhase2() {
        return getProperties().getTypedProperty(ESMR50ConfigurationSupport.WORKING_KEY_LABEL_PHASE2, null).toString();
    }


}
