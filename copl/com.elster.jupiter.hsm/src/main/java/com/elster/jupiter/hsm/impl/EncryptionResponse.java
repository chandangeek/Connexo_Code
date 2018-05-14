package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.basecrypto.SymmetricResponse;

public class EncryptionResponse {

    private final byte[] bytes;

    public EncryptionResponse(byte[] encrypt) {
        this.bytes = encrypt;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
