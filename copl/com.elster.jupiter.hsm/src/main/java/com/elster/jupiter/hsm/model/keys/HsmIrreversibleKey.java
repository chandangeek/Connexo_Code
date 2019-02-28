package com.elster.jupiter.hsm.model.keys;

public class HsmIrreversibleKey extends HsmReversibleKey implements HsmKey {


    public HsmIrreversibleKey(byte[] encryptedKey, String keyLabel) {
        super(encryptedKey, keyLabel);
    }

    @Override
    public boolean isReversible() {
        return false;
    }
}
