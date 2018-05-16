package com.elster.jupiter.hsm.impl;

public class DecryptResponse {

    private byte[] data;

    public DecryptResponse(byte[] decrypt) {
        this.data = decrypt;
    }


    public byte[] getData() {
        return data;
    }
}
