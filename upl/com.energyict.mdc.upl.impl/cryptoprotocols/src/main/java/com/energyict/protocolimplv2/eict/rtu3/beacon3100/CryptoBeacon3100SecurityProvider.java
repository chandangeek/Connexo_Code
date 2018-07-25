package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100SecurityProvider;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/11/2016 - 16:49
 */
public class CryptoBeacon3100SecurityProvider extends Beacon3100SecurityProvider {


//    private SecurityPropertyValueParser securityPropertyValueParser = new SecurityPropertyValueParser();

    public CryptoBeacon3100SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel, int securitySuite, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(properties, authenticationDeviceAccessLevel, securitySuite, certificateWrapperExtractor);
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

}