package com.elster.jupiter.datavault;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;

/**
 * Provides services to persist and retrieve {@link java.security.KeyStore}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (10:05)
 */
public interface KeyStoreService {

    /**
     * Starts the building process of a new user defined {@link PersistentKeyStore}.
     *
     * @param name The name of the new PersistentKeyStore
     * @param type The type of the new PersistentKeyStore
     * @return The Builder
     * @throws KeyStoreException Thrown by the underlying java KeyStore
     * @throws CertificateException Thrown by the underlying java KeyStore
     * @throws NoSuchAlgorithmException Thrown by the underlying java KeyStore
     * @throws IOException Thrown by the underlying java KeyStore
     */
    Builder newUserDefinedKeyStore(String name, String type) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException;

    List<PersistentKeyStore> findUserDefinedKeyStores();

    List<PersistentKeyStore> findUserDefinedKeyStores(String type);

    /**
     * Starts the building process of a new system defined {@link PersistentKeyStore}.
     *
     * @param name The name of the new PersistentKeyStore
     * @param type The type of the new PersistentKeyStore
     * @return The Builder
     * @throws KeyStoreException Thrown by the underlying java KeyStore
     * @throws CertificateException Thrown by the underlying java KeyStore
     * @throws NoSuchAlgorithmException Thrown by the underlying java KeyStore
     * @throws IOException Thrown by the underlying java KeyStore
     */
    Builder newSystemDefinedKeyStore(String name, String type) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException;

    Optional<PersistentKeyStore> findSystemDefined(String name);

    /**
     * Supports various ways to initialize the KeyStore.
     */
    interface Builder {

        /**
         * @see KeyStore#setEntry(String, KeyStore.Entry, KeyStore.ProtectionParameter)
         */
        Builder setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException;

        /**
         * @see KeyStore#setCertificateEntry(String, Certificate)
         */
        Builder setCertificateEntry(String alias, Certificate cert) throws KeyStoreException;

        /**
         * @see KeyStore#setKeyEntry(String, byte[], Certificate[])
         */
        Builder setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException;

        /**
         * @see KeyStore#setKeyEntry(String, Key, char[], Certificate[])
         */
        Builder setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException;

        /**
         * Creates the KeyStore from the specifications
         * that were provided to this Builder.
         * This obsoletes this Builder, making any
         * additional calls will throw an IllegalStateException.
         *
         * @param password The password to generate the keystore integrity check
         * @throws CertificateException Thrown by the underlying java KeyStore
         * @throws NoSuchAlgorithmException Thrown by the underlying java KeyStore
         * @throws KeyStoreException Thrown by the underlying java KeyStore
         * @throws IOException Thrown by the underlying java KeyStore
         */
        PersistentKeyStore build(char[] password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException;
    }
}