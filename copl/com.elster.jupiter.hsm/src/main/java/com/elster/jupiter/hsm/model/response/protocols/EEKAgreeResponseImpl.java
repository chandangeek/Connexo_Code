package com.elster.jupiter.hsm.model.response.protocols;

import com.atos.worldline.jss.api.key.KeyLabel;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.IrreversibleKey;

public class EEKAgreeResponseImpl implements EEKAgreeResponse {

    IrreversibleKey ephemeralEncryptionKey;
    private byte[] ephemeralPublicKey;
    private byte[] signature;

    public EEKAgreeResponseImpl(com.atos.worldline.jss.api.custom.energy.EEKAgreeResponse eekAgreeResponse) {
        ephemeralPublicKey = eekAgreeResponse.getEphemeralKaKey();
        signature = eekAgreeResponse.getSignature();
        ephemeralEncryptionKey = new HsmEncryptedKey(eekAgreeResponse.getEek().getValue(), ((KeyLabel)eekAgreeResponse.getEek().getKek()).getValue());
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
