package com.elster.jupiter.hsm.impl;


public class EncryptionResponse {

    private final byte[] bytes;

    public EncryptionResponse(byte[] encrypt) {
        this.bytes = encrypt;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String toString(){
        return new String(bytes);
    }
}
