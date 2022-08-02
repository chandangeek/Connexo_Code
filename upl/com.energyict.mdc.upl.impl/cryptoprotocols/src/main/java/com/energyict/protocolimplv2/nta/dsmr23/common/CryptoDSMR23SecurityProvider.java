package com.energyict.protocolimplv2.nta.dsmr23.common;

import com.energyict.mdc.upl.crypto.IrreversibleKey;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.common.IrreversibleKeyImpl;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.io.IOException;

public class CryptoDSMR23SecurityProvider extends NTASecurityProvider {
    //stolen from CryptoBeacon3100ConfigurationSupport
    public static final String EEK_STORAGE_LABEL = "EphemeralEncryptionKeyStorageLabel";

    /**
     * Property value of the CryptoServer
     */
    public static final String CRYPTOSERVER_USAGE = "CryptoServer";
    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public CryptoDSMR23SecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
        initializeKeys();
    }

//    private void initializeKeys() {
//        try {
//            String rawEncryptionKey = properties.getProperty(DATATRANSPORT_ENCRYPTIONKEY, "");
//            String rawAuthenticationKey = properties.getProperty(DATATRANSPORT_AUTHENTICATIONKEY, "");
//            String rawMasterKey = properties.getProperty(MASTERKEY, "");
//            String rawHlsKey = properties.getProperty(CRYPTOSERVER_HLS_SECRET, "");
//
//            //Store the base64 encoded property values. The getters of the keys return the bytes of the base64 encoded values!
//            setEncryptionKey(getBase64EncoderDecoder().encode(rawEncryptionKey.getBytes()).getBytes());
//            setAuthenticationKey(getBase64EncoderDecoder().encode(rawAuthenticationKey.getBytes()).getBytes());
//            setMasterKey(getBase64EncoderDecoder().encode(rawMasterKey.getBytes()).getBytes());
//            setHlsSecret(getBase64EncoderDecoder().encode(rawHlsKey.getBytes()));
//        } catch (IOException e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
//    }
    //TODO: check if this workaround really works; it's the implementation from beacon;
    // the above from eiserver do not seem to match connexo framework
    private void initializeKeys() {
        setAuthenticationKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString()));
        setEncryptionKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString()));
        setHlsSecret(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.PASSWORD.toString()));
        try {
            changeMasterKey(parseSecurityPropertyValue(SecurityPropertySpecTranslationKeys.MASTER_KEY.toString()));
        }catch(IOException ex){
           //nothing as changeMasterKey will never throw the exception
        }
    }

    private byte[] parseSecurityPropertyValue(String securityPropertyName) {
        IrreversibleKey irreversibleKey = new IrreversibleKeyImpl(properties.getTypedProperty(securityPropertyName, ""));
        return irreversibleKey.toBase64ByteArray();
    }

    public String getEekStorageLabel() {
        return properties.getTypedProperty(EEK_STORAGE_LABEL);
    }
}
