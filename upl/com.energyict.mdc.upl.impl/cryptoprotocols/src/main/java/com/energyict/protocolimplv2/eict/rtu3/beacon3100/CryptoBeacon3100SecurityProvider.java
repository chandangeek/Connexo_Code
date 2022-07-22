package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100SecurityProvider;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/11/2016 - 16:49
 */
public class CryptoBeacon3100SecurityProvider extends Beacon3100SecurityProvider {

    public CryptoBeacon3100SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel, int securitySuite, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(properties, authenticationDeviceAccessLevel, securitySuite, certificateWrapperExtractor);
        initializeKeys();
    }

    private void initializeKeys() {
        setAuthenticationKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString()));
        setEncryptionKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString()));
        setHlsSecret(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.PASSWORD.toString()));
    }

    private byte[] parseSecurityPropertyValue(String securityPropertyName) {
        IrreversibleKey irreversibleKey = new IrreversibleKeyImpl(properties.getTypedProperty(securityPropertyName, ""));
        return irreversibleKey.toBase64ByteArray();
    }

    public String getEekStorageLabel() {
        return properties.getTypedProperty(CryptoBeacon3100ConfigurationSupport.EEK_STORAGE_LABEL);
    }

}
