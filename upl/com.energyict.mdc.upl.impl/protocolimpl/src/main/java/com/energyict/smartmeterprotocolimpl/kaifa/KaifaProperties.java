package com.energyict.smartmeterprotocolimpl.kaifa;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110RProperties;

class KaifaProperties extends AM110RProperties {
    public static final String PASSWORD = "Password";

    KaifaProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "0");   //Don't use get-with-list by default
    }

    public void setProperty(String propertyName, String propertyValue) {
        this.getProtocolProperties().setProperty(propertyName, propertyValue);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (this.securityProvider == null) {
            this.securityProvider = new KaifaSecurityProvider(getProtocolProperties());
        }
        return this.securityProvider;
    }

}