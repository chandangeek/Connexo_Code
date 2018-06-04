package com.elster.jupiter.hsm.model.keys;

import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;

import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

public class TransportKey {

    private final String keyLabel;
    private final AsymmetricAlgorithm asymmetricAlgorithm;

    /**
     *
     * @param keyLabel that shall be used to decrypt device key that will be used together with this transport key

     * @param paddingAlgorithm for the asymmetric algorithm that was used to encrypt the transport key (to be used in correlation with keyLabel)
     */
    public TransportKey(String keyLabel,  AsymmetricAlgorithm paddingAlgorithm) {
        this.keyLabel = keyLabel;
        this.asymmetricAlgorithm = paddingAlgorithm;
    }


    public com.atos.worldline.jss.api.custom.energy.TransportKey toHsmFormat(DeviceKey dKey) throws EncryptBaseException {
        try {
            return new com.atos.worldline.jss.api.custom.energy.TransportKey(new KeyLabel(keyLabel), dKey.getKeyLength(), dKey.getEncryptedKey());
        } catch (UnsupportedKEKEncryptionMethodException e) {
            throw new EncryptBaseException("Could not transform transport key", e);
        }
    }


    public AsymmetricAlgorithm getAsymmetricAlgorithm() {
        return asymmetricAlgorithm;
    }
}
