package com.elster.jupiter.datavault;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

/**
 * Models a persistent {@link java.security.KeyStore} and is in fact a wrapper around a KeyStore.
 * In addition, it has a unique numerical identifier and a unique name.
 * The design is to expose the underlying KeyStore once you have provided the
 * password that was used at creation time to ensure the integrity of the KeyStore
 * The methods that modify the contents of the keyStore will however only be
 * done in memory and will not be persisted.
 * Updates, such as deleting or setting an entry is hidden behind an "Updater" interface
 * to be consitent with the Connexo coding style.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (10:07)
 */
public interface PersistentKeyStore extends HasId, HasName {

    /**
     * Provides access to the underlying KeyStore.
     * Remember that updates have no effect, you need to
     * start an update process first.
     *
     * @param password The password used to unlock and check the integrity of the keystore
     * @return The underlying KeyStore
     */
    KeyStore forReading(char[] password);

    /**
     * Starts the update process that you will
     * end by calling {@link Updater#save()}.
     *
     * @param password The password used to unlock and check the integrity of the keystore
     * @return The Updater
     */
    Updater forUpdating(char[] password);

    interface Updater {
        /**
         * @see KeyStore#setEntry(String, KeyStore.Entry, KeyStore.ProtectionParameter)
         */
        Updater setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException;

        /**
         * @see KeyStore#setCertificateEntry(String, Certificate)
         */
        Updater setCertificateEntry(String alias, Certificate cert) throws KeyStoreException;

        /**
         * @see KeyStore#setKeyEntry(String, byte[], Certificate[])
         */
        Updater setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException;

        /**
         * @see KeyStore#setKeyEntry(String, Key, char[], Certificate[])
         */
        Updater setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException;

        /**
         * Saves the changes that were applied.
         * This obsoletes this Updater, attempting to make
         * additional changes will throw an IllegalStateException.
         */
        void save();
    }
}