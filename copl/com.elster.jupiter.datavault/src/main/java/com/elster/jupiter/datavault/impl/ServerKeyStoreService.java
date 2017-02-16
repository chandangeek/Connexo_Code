package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.KeyStoreService;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Adds behavior to the {@link KeyStoreService}
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (10:26)
 */
public interface ServerKeyStoreService extends KeyStoreService {
    /**
     * Creates a new DataVaultKeyStore with the specified type.
     * Note that there should only be one per type.
     *
     * @param type The KeyStore type
     * @return The Builder
     * @throws KeyStoreException Thrown by the underlying java KeyStore
     * @throws CertificateException Thrown by the underlying java KeyStore
     * @throws NoSuchAlgorithmException Thrown by the underlying java KeyStore
     * @throws IOException Thrown by the underlying java KeyStore
     */
    Builder newDataVaultKeyStore(String type) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException;

    DataVault getDataVaultInstance();

}