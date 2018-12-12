package com.elster.jupiter.hsm.model.request;


import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import java.util.Arrays;
import java.util.Objects;

public class RenewKeyRequest {

    private final byte[] masterKey;
    private final String masterKeyLabel;
    private final HsmKeyType hsmKeyType;


    public RenewKeyRequest(byte[] masterKey, String actualLabel, HsmKeyType hsmKeyType) {
        this.masterKey = masterKey;
        this.masterKeyLabel = actualLabel;
        this.hsmKeyType =hsmKeyType;
    }

    public byte[] getMasterKey() {
        return masterKey;
    }

    public String getMasterKeyLabel() {
        return masterKeyLabel;
    }




    public SessionKeyCapability getRenewCapability() {
        return hsmKeyType.getRenewCapability();
    }

    public String getRenewLabel() {
        return hsmKeyType.getLabel();
    }

    public HsmKeyType getHsmKeyType() {
        return hsmKeyType;
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
        return Arrays.equals(masterKey, that.masterKey) &&
                Objects.equals(masterKeyLabel, that.masterKeyLabel) &&
                Objects.equals(hsmKeyType, that.hsmKeyType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(masterKeyLabel, hsmKeyType);
        result = 31 * result + Arrays.hashCode(masterKey);
        return result;
    }
}
