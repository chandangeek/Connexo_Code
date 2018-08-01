package com.elster.jupiter.hsm.model.request;


import java.util.Arrays;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RenewKeyRequest)) {
            return false;
        }
        RenewKeyRequest that = (RenewKeyRequest) o;
        return Arrays.equals(actualKey, that.actualKey) &&
                Objects.equals(actualLabel, that.actualLabel) &&
                Objects.equals(renewLabel, that.renewLabel);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(actualLabel, renewLabel);
        result = 31 * result + Arrays.hashCode(actualKey);
        return result;
    }
}
