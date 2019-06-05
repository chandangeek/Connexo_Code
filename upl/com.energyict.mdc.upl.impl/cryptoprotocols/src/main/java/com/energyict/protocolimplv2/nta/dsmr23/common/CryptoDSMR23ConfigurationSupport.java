package com.energyict.protocolimplv2.nta.dsmr23.common;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.List;

public class CryptoDSMR23ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String CRYPTOSERVER_HLS_SECRET = "HlsSecret";

    public CryptoDSMR23ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.cryptoServerUsageProperty());
        propertySpecs.add(this.cryptoServerHLSSecretProperty());
        return propertySpecs;
    }

    protected PropertySpec cryptoServerUsageProperty(){
        return UPLPropertySpecFactory.specBuilder(CryptoDSMR23SecurityProvider.CRYPTOSERVER_USAGE, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_USAGE, getPropertySpecService()::stringSpec).finish();
    }

    protected PropertySpec cryptoServerHLSSecretProperty(){
        return UPLPropertySpecFactory.specBuilder(CRYPTOSERVER_HLS_SECRET, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_HLSSECRET, getPropertySpecService()::stringSpec).finish();
    }
}
