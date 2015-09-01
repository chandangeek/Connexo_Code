package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    ENCRYPTION_FAILED(1, "EncryptionFailed", "Encryption failed: {0}"),
    KEYSTORE_CREATION_FAILED(2, "KeystoreCreationFailed", "Failed to create KeyStore: {0}"),
    DECRYPTION_FAILED(4, "DecryptionFailed", "Decryption failed: {0}"),
    KEYSTORE_LOAD_FILE(5, "ReadKeystoreFailed", "Failed to load key store from user file"),
    AMBIGUOUS_KEYSTORE(6, "AmbiguousKeyStore", "More than one key store was initialized"),
    NO_KEYSTORE(7, "NoKeyStore", "Key store was not initialized"),
    ;

    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DataVaultService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
