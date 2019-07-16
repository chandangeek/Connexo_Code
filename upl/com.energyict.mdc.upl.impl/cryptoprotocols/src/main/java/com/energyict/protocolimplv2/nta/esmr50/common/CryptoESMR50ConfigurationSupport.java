package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;

import java.util.List;

public class CryptoESMR50ConfigurationSupport extends ESMR50ConfigurationSupport {

    public static final String CRYPTOSERVER_HLS_SECRET = "HlsSecret";
    public static final String PROPERTY_INITIALIZE_CRYPTO_SERVER = "InitializeCryptoServer";
    public static final String PROPERTY_CRYPTO_SERVER_CONFIG_FILE = "CryptoServerConfigFile";

    public CryptoESMR50ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.cryptoServerUsageProperty());
        propertySpecs.add(this.cryptoServerHLSSecretProperty());
        propertySpecs.add(this.cryptoServerInitializeProperty());
        propertySpecs.add(this.cryptoServerConfigFileProperty());
        return propertySpecs;
    }

    private PropertySpec cryptoServerUsageProperty(){
        return UPLPropertySpecFactory.specBuilder(CryptoDSMR23SecurityProvider.CRYPTOSERVER_USAGE, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_USAGE, getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec cryptoServerHLSSecretProperty(){
        return UPLPropertySpecFactory.specBuilder(CRYPTOSERVER_HLS_SECRET, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_HLSSECRET, getPropertySpecService()::stringSpec).finish();
    }

    @Deprecated
    private PropertySpec cryptoServerInitializeProperty(){
        return UPLPropertySpecFactory.specBuilder(PROPERTY_INITIALIZE_CRYPTO_SERVER, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_INIT, getPropertySpecService()::stringSpec).finish();
    }

    @Deprecated
    private PropertySpec cryptoServerConfigFileProperty(){
        return UPLPropertySpecFactory.specBuilder(PROPERTY_CRYPTO_SERVER_CONFIG_FILE, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_CONFIG_FILE, getPropertySpecService()::stringSpec).finish();
    }
}
