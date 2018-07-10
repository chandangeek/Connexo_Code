package com.elster.jupiter.hsm.model.keys;

import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;

import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

public class TransportKey {

    private final byte[] keyValue;
    private final String keyLabel;
    private final AsymmetricAlgorithm asymmetricAlgorithm;
    private final int keyLength;

    /**
     *
     * @param keyLabel that shall be used to decrypt device key that will be used together with this transport key

     * @param paddingAlgorithm for the asymmetric algorithm that was used to encrypt the transport key (to be used in correlation with keyLabel)
     */
    public TransportKey(String keyLabel, AsymmetricAlgorithm paddingAlgorithm, byte[] keyValue, int keyLength) {
        this.keyValue =  keyValue;
        this.keyLabel = keyLabel;
        this.asymmetricAlgorithm = paddingAlgorithm;
        this.keyLength = keyLength;
    }


    public com.atos.worldline.jss.api.custom.energy.TransportKey toHsmFormat() throws EncryptBaseException {
        try {
            return new com.atos.worldline.jss.api.custom.energy.TransportKey(new KeyLabel(keyLabel), keyLength , keyValue);
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new EncryptBaseException("Could not transform transport key", e);
        }
    }


    public AsymmetricAlgorithm getAsymmetricAlgorithm() {
        return asymmetricAlgorithm;
    }

    public byte[] getKeyValue() {
        return keyValue;
    }
}
