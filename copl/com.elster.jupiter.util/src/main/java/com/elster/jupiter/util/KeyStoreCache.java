package com.elster.jupiter.util;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple cache for get keyStore. Please be aware that changing underlying keyStore will not be necessarily reflected in the behavior of this cache.
 * Meaning we simply cache keys by alias and retrieve them from cache (TreeMap) if found in cache, otherwise we go to underlying keyStore and if found we add it to our cache
 * before returning it.
 *
 * This implementation does not want to be a fully functional KeyStore cache but rather a very simplistic way for our use case (used only for the internally generated trust(key) store.
 *
 */
public class KeyStoreCache {

    private final KeyStore keyStore;
    private final int keyStoreSize;

    private final Map<String, Key>  cachedKeys;


    public KeyStoreCache(KeyStore keyStore) throws KeyStoreException {
        this.keyStore = keyStore;
        this.keyStoreSize = keyStore.size();
        this.cachedKeys = new TreeMap<>();
    }

    /**
     * Retrieves a key for the alias provided from cache (not using password), if found. Otherwise searching underlying keyStore for the requested key attached to alias and using password.
     * This implementation does not care about security issues since it is intended to be used only for the internally generated trust(key) store which anyway can be obtained.
     * @param alias
     * @param password
     * @return
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public Key getKey(String alias, char[] password) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        Key key = cachedKeys.get(alias);
        if (key == null) {
            key = keyStore.getKey(alias, password);
            cachedKeys.put(alias, key);
        }
        return key;
    }


    public int size() {
        return keyStoreSize;
    }
}
