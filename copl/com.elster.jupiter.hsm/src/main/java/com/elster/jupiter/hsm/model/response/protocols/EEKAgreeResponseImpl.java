package com.elster.jupiter.hsm.model.response.protocols;

import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;

public class EEKAgreeResponseImpl implements EEKAgreeResponse {

    private HsmIrreversibleKey ephemeralEncryptionKey;
    private byte[] ephemeralPublicKey;
    private byte[] signature;

    public EEKAgreeResponseImpl(HsmIrreversibleKey ephemeralEncryptionKey, byte[] ephemeralPublicKey, byte[] signature) {
        this.ephemeralPublicKey = ephemeralPublicKey;
        this.signature = signature;
        this.ephemeralEncryptionKey = ephemeralEncryptionKey;
    }

    @Override
    public byte[] getEphemeralPublicKey() {
        return ephemeralPublicKey;
    }

    @Override
    public byte[] getSignature() {
        return signature;
    }

    @Override
    public HsmIrreversibleKey getEek() {
        return ephemeralEncryptionKey;
    }
}
