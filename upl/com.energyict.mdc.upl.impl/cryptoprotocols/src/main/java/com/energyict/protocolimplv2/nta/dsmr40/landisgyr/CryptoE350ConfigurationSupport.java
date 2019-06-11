package com.energyict.protocolimplv2.nta.dsmr40.landisgyr;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.nta.dsmr40.common.CryptoDSMR40ConfigurationSupport;

import java.util.List;

public class CryptoE350ConfigurationSupport extends CryptoDSMR40ConfigurationSupport {

    public CryptoE350ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        return propertySpecs;
    }
}
