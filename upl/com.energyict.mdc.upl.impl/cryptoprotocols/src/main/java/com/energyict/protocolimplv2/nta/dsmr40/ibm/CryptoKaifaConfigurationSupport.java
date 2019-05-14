package com.energyict.protocolimplv2.nta.dsmr40.ibm;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr40.common.CryptoDSMR40ConfigurationSupport;

import java.util.List;

public class CryptoKaifaConfigurationSupport extends CryptoDSMR40ConfigurationSupport {


    public CryptoKaifaConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        return propertySpecs;
    }
}
