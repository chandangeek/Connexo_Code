package com.elster.jupiter.hsm.model.response.protocols;

import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;

public interface EEKAgreeResponse {

    byte[] getEphemeralPublicKey();

    byte[] getSignature();

    HsmIrreversibleKey getEek();
}
