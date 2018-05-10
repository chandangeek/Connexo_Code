package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.basecrypto.SymmetricResponse;

public class EncryptionResponse {

    private final byte[] bytes;

    public EncryptionResponse(SymmetricResponse encrypt) {
        this.bytes = encrypt.getData();
    }

    public byte[] getBytes() {
        return bytes;
    }
}
