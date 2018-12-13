package com.elster.jupiter.hsm.model.response.protocols;

import com.elster.jupiter.hsm.model.keys.IrreversibleKey;

public interface EEKAgreeResponse {

    byte[] getEphemeralPublicKey();

    byte[] getSignature();

    IrreversibleKey getEek();
}
