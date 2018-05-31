package com.elster.jupiter.hsm.model.keys;

import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.impl.HsmFormatable;
import com.elster.jupiter.hsm.model.krypto.AsymmetricPaddingAlgorithm;

import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

public class TransportKey implements HsmFormatable<com.atos.worldline.jss.api.custom.energy.TransportKey> {

    private final String keyLabel;
    private final int keyLength;
    private final byte[] encryptedKey;

    private final AsymmetricPaddingAlgorithm paddingAlgorithm;

    public TransportKey(String keyLabel, int keyLength, byte[] encryptedKey, AsymmetricPaddingAlgorithm paddingAlgorithm) {
        this.keyLabel = keyLabel;
        this.keyLength = keyLength;
        this.encryptedKey = encryptedKey;
        this.paddingAlgorithm = paddingAlgorithm;
    }


    @Override
    public com.atos.worldline.jss.api.custom.energy.TransportKey toHsmFormat() throws EncryptBaseException {
        try {
            return new com.atos.worldline.jss.api.custom.energy.TransportKey(new KeyLabel(keyLabel), keyLength, encryptedKey);
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new EncryptBaseException("Could not transform transport key", e);
        }
    }


    public AsymmetricPaddingAlgorithm getPaddingAlgorithm() {
        return paddingAlgorithm;
    }
}
