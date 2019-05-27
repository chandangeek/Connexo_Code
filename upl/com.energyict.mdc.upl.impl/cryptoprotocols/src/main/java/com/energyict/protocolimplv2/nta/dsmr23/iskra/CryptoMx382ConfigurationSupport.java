package com.energyict.protocolimplv2.nta.dsmr23.iskra;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.common.CryptoDSMR40ConfigurationSupport;

import java.util.List;

public class CryptoMx382ConfigurationSupport extends CryptoDSMR23ConfigurationSupport {

    public CryptoMx382ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        return propertySpecs;
    }
}
