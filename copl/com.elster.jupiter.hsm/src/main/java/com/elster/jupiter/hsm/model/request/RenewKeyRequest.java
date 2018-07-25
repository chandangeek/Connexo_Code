package com.elster.jupiter.hsm.model.request;


public class RenewKeyRequest {

    private final byte[] actualKey;
    private final String actualLabel;
    private final String renewLabel;


    public RenewKeyRequest(byte[] actualKey, String actualLabel, String renewLabel) {
        this.actualKey = actualKey;
        this.actualLabel = actualLabel;
        this.renewLabel = renewLabel;
    }

    public byte[] getActualKey() {
        return actualKey;
    }

    public String getActualLabel() {
        return actualLabel;
    }

    public String getRenewLabel() {
        return renewLabel;
    }


}
