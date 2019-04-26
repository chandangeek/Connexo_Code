package com.elster.jupiter.hsm.model.response.protocols;

public class MacResponseImpl implements MacResponse {
    private final byte[] data;
    private final byte[] initVector;

    public MacResponseImpl(byte[] data, byte[] initVector) {
        this.data = data;
        this.initVector = initVector;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] getInitVector() {
        return initVector;
    }
}
