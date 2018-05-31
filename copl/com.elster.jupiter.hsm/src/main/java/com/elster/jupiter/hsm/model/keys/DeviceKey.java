package com.elster.jupiter.hsm.model.keys;


import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.impl.HsmFormatable;
import com.elster.jupiter.hsm.model.krypto.ChainingMode;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricPaddingAlgorithm;

public class DeviceKey implements HsmFormatable<com.atos.worldline.jss.api.custom.energy.DeviceKey> {

    private final SymmetricAlgorithm alg;
    private final ChainingMode mode;
    private final SymmetricPaddingAlgorithm padding;

    private final int keyLength;
    private final byte[] encryptedKey;

    public DeviceKey(String encryptionSpec, int keyLength, byte[] encryptedKey) throws EncryptBaseException {
        this.alg = SymmetricAlgorithm.from(encryptionSpec);
        this.mode = ChainingMode.from(encryptionSpec);
        this.padding = SymmetricPaddingAlgorithm.from(encryptionSpec);
        this.keyLength = keyLength;
        this.encryptedKey = encryptedKey;
    }


    public int getKeyLength() {
        return keyLength;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }


    @Override
    public com.atos.worldline.jss.api.custom.energy.DeviceKey toHsmFormat() {
        return  null;
    }
}
