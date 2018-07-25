package com.energyict.protocolimplv2.dlms.idis.am540;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.IDISSecurityProvider;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/10/2016 - 13:07
 */
public class CryptoAM540SecurityProvider extends IDISSecurityProvider {


//    private SecurityPropertyValueParser securityPropertyValueParser = new SecurityPropertyValueParser();

    public CryptoAM540SecurityProvider(TypedProperties properties, int authenticationLevel, short errorHandling) {
        super(properties, authenticationLevel, errorHandling);
        initializeKeys();
    }

    private void initializeKeys() {
        //TODO: get the keys directly from security accsesors?
//        setAuthenticationKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString()));
//        setEncryptionKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString()));
//        setHlsSecret(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.PASSWORD.toString()));
    }

//    private byte[] parseSecurityPropertyValue(String securityPropertyName) {
//        return securityPropertyValueParser.parseSecurityPropertyValue(securityPropertyName, properties.getTypedProperty(securityPropertyName, ""));
//    }

//    public void setUsingServiceKeys(boolean usingServiceKeys) {
//        this.usingServiceKeys = usingServiceKeys;
//    }
}