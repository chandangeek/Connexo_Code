package com.energyict.protocolimplv2.nta.dsmr40.common;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23ConfigurationSupport;

import java.util.List;

public class CryptoDSMR40ConfigurationSupport extends CryptoDSMR23ConfigurationSupport {
    public CryptoDSMR40ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        return propertySpecs;
    }

}
