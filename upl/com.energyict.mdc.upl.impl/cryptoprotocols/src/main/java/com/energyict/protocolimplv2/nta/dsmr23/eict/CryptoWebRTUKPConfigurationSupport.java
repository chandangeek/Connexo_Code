package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23ConfigurationSupport;

import java.util.List;

public class CryptoWebRTUKPConfigurationSupport extends CryptoDSMR23ConfigurationSupport {

    public CryptoWebRTUKPConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        return propertySpecs;
    }
}
