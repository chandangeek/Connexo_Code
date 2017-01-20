package com.energyict.mdc.engine.impl;

import com.elster.jupiter.datavault.KeyStoreService;
import com.elster.jupiter.datavault.PersistentKeyStore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static com.energyict.mdc.upl.crypto.KeyStoreService.StoreType.KEY;
import static com.energyict.mdc.upl.crypto.KeyStoreService.StoreType.TRUST;

/**
 * Represents the supported functionality to get and set private keys and certificates to the persisted keystore and truststore.
 * Note that the keystore only contains client information.
 * Note that the truststore only contains subCa and rootCA certificates.
 * The server end-device certificates are stored as {@link com.energyict.mdc.upl.security.CertificateWrapper}s, not in this truststore.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/01/2016 - 15:18
 */
class DLMSKeyStoreUserFile {

    static final char[] PARAMETERS = new char[]{'i', '#', '?', 'r', 'P', '1', '_', 'L', 'v', '/', 'T', '@', '>', 'k', 'h', '*'};
    private static final String KEY_STORE_EXTENSION = "p12";
    private static final String TRUST_STORE_EXTENSION = "JCEKS";
    private static final String KEY_STORE_NAME = "keystore";
    private static final String TRUST_STORE_NAME = "truststore";

    private final KeyStoreService keyStoreService;

    DLMSKeyStoreUserFile(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    /**
     * Load the existing keyStoreName.keyStoreExtension key store of type keyStoreType from the database if it exists.
     * If it does not exist yet, create a new, empty keystore and persist it as a userfile.
     */
    public KeyStore findOrCreateDLMSKeyStore() {
        return findOrCreateDLMSStore(KEY, KEY_STORE_NAME, KEY_STORE_EXTENSION).forReading(PARAMETERS);
    }

    public KeyStore findOrCreateDLMSTrustStore() {
        return findOrCreateDLMSStore(TRUST, TRUST_STORE_NAME, TRUST_STORE_EXTENSION).forReading(PARAMETERS);
    }


    private PersistentKeyStore findOrCreateDLMSStore(com.energyict.mdc.upl.crypto.KeyStoreService.StoreType type, String storeName, String storeExtension) {
        return this.keyStoreService
                    .findSystemDefined(toStoreName(storeName, storeExtension))
                    .orElseGet(() -> this.createDLMSStore(type, storeName, storeExtension));
    }

    private PersistentKeyStore createDLMSStore(com.energyict.mdc.upl.crypto.KeyStoreService.StoreType type, String name, String extension) {
        try {
            return this.keyStoreService
                    .newSystemDefinedKeyStore(toStoreName(name, extension), type.getStoreTypeValue())
                    .build(PARAMETERS);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to load the DLMS key store from user file", e);
        }
    }

    private String toStoreName(String storeName, String extension) {
        return storeName + "." + extension;
    }
}