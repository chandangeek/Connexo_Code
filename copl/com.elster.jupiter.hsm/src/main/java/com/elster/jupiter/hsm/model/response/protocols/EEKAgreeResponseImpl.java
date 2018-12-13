package com.elster.jupiter.hsm.model.response.protocols;

import com.elster.jupiter.hsm.model.keys.IrreversibleKey;

public class EEKAgreeResponseImpl implements EEKAgreeResponse {

    private IrreversibleKey ephemeralEncryptionKey;
    private byte[] ephemeralPublicKey;
    private byte[] signature;

    public EEKAgreeResponseImpl(IrreversibleKey ephemeralEncryptionKey, byte[] ephemeralPublicKey, byte[] signature) {
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
    public IrreversibleKey getEek() {
        return ephemeralEncryptionKey;
    }
}
