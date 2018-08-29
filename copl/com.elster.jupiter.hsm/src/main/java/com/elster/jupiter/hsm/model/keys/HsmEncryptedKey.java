package com.elster.jupiter.hsm.model.keys;

public class HsmEncryptedKey implements IrreversibleKey {

    private final byte[] encryptedKey;
    private final String keyLabel;


    public HsmEncryptedKey(byte[] encryptedKey, String keyLabel) {
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
