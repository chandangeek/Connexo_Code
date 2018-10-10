package com.elster.jupiter.hsm.model.request;


import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;

import java.util.Arrays;
import java.util.Objects;

public class RenewKeyRequest {

    private final byte[] actualKey;
    private final String actualLabel;
    private final HsmKeyType hsmKeyType;


    public RenewKeyRequest(byte[] actualKey, String actualLabel, HsmKeyType hsmKeyType) {
        this.actualKey = actualKey;
        this.actualLabel = actualLabel;
        this.hsmKeyType =hsmKeyType;
    }

    public byte[] getActualKey() {
        return actualKey;
    }

    public String getActualLabel() {
        return actualLabel;
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
        return Arrays.equals(actualKey, that.actualKey) &&
                Objects.equals(actualLabel, that.actualLabel) &&
                Objects.equals(hsmKeyType, that.hsmKeyType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(actualLabel, hsmKeyType);
        result = 31 * result + Arrays.hashCode(actualKey);
        return result;
    }
}
