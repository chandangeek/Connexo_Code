package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300SecurityProvider;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.CryptoBeacon3100ConfigurationSupport;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

public class CryptoHS3300SecurityProvider extends HS3300SecurityProvider {

    public CryptoHS3300SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel, int securitySuite, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(properties, authenticationDeviceAccessLevel, securitySuite, certificateWrapperExtractor);
        initializeKeys();
    }

    private void initializeKeys() {
        setAuthenticationKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString()));
        setEncryptionKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString()));
    }

    private byte[] parseSecurityPropertyValue(String securityPropertyName) {
        IrreversibleKey irreversibleKey = new IrreversibleKeyImpl(properties.getTypedProperty(securityPropertyName, ""));
        return irreversibleKey.toBase64ByteArray();
    }

    public String getEekStorageLabel() {
        return properties.getTypedProperty(CryptoBeacon3100ConfigurationSupport.EEK_STORAGE_LABEL);
    }

}
