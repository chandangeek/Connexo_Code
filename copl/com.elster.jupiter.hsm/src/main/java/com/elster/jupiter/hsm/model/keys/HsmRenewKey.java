package com.elster.jupiter.hsm.model.keys;

public class HsmRenewKey extends HsmEncryptedKey {

    private final byte[] smartMeterKey;

    public HsmRenewKey(byte[] smartMeterKey, byte[] encryptedKey, String keyLabel) {
        super(encryptedKey, keyLabel);
        this.smartMeterKey = smartMeterKey;
    }

    public byte[] getSmartMeterKey() {
        return smartMeterKey;
    }
}
