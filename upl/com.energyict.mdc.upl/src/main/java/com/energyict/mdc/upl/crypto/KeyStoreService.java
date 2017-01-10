package com.energyict.mdc.upl.crypto;

import java.security.KeyStore;

/**
 * Provides services for KeyStores as defined by the JDK.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-10 (12:29)
 */
public interface KeyStoreService {

    enum StoreType {
        KEY("PKCS12"),
        TRUST("JCEKS");

        private final String storeTypeValue;

        StoreType(String storeTypeValue) {
            this.storeTypeValue = storeTypeValue;
        }

        public String getStoreTypeValue() {
            return storeTypeValue;
        }
    }

    KeyStore findOrCreate(StoreType type, String name);

}