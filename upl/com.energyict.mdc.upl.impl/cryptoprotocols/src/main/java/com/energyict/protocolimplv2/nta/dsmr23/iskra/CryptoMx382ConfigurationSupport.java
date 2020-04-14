package com.energyict.protocolimplv2.nta.dsmr23.iskra;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.Iskra.Mx382ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;

import java.util.List;

import static com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23ConfigurationSupport.CRYPTOSERVER_HLS_SECRET;

public class CryptoMx382ConfigurationSupport extends Mx382ConfigurationSupport {


    public CryptoMx382ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.cryptoServerUsageProperty());
        propertySpecs.add(this.cryptoServerHLSSecretProperty());
        return propertySpecs;
    }

    //TODO: the bellow methods are duplicated in multiple crypto protocols. Should be extracted in a common class.
    private PropertySpec cryptoServerUsageProperty() {
        return UPLPropertySpecFactory.specBuilder(CryptoDSMR23SecurityProvider.CRYPTOSERVER_USAGE, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_USAGE, getPropertySpecService()::booleanSpec)
                .finish();
    }

    private PropertySpec cryptoServerHLSSecretProperty() {
        return UPLPropertySpecFactory.specBuilder(CRYPTOSERVER_HLS_SECRET, false, PropertyTranslationKeys.V2_NTA_CRYPTO_SERVER_HLSSECRET, getPropertySpecService()::stringSpec).finish();
    }
}
