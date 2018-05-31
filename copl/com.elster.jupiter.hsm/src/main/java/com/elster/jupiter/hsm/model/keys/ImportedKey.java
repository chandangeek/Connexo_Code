package com.elster.jupiter.hsm.model.keys;

public class ImportedKey {

    private final byte[] encryptedKey;
    private final String keyLabel;


    public ImportedKey(byte[] encryptedKey, String keyLabel) {
        this.encryptedKey = encryptedKey;
        this.keyLabel = keyLabel;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public String getKeyLabel() {
        return keyLabel;
    }
}
