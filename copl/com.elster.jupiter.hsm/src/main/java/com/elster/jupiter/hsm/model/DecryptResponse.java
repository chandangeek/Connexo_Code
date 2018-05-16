package com.elster.jupiter.hsm.model;

public class DecryptResponse {

    private byte[] data;

    public DecryptResponse(byte[] decrypt) {
        this.data = decrypt;
    }


    public byte[] getData() {
        return data;
    }
}
