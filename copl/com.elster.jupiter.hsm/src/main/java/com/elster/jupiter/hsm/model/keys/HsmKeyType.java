package com.elster.jupiter.hsm.model.keys;

import java.util.Objects;

public class HsmKeyType {

    private final HsmJssKeyType hsmJssKeyType;
    private final String label;
    private final SessionKeyCapability importCapability;
    private final SessionKeyCapability renewCapability;
    private final int keySize;
    private final boolean isReversible;


    public HsmKeyType(HsmJssKeyType hsmJssKeyType, String label, SessionKeyCapability importCapability, SessionKeyCapability renewCapability, int keySize, boolean isReversible) {
        this.hsmJssKeyType = hsmJssKeyType;
        this.label = label;
        this.importCapability = importCapability;
        this.renewCapability = renewCapability;
        this.keySize = keySize;
        this.isReversible = isReversible;
    }

    public HsmJssKeyType getHsmJssKeyType() {
        return hsmJssKeyType;
    }

    public String getLabel() {
        return label;
    }

    public SessionKeyCapability getImportCapability() {
        return importCapability;
    }

    public SessionKeyCapability getRenewCapability() {
        return renewCapability;
    }

    public int getKeySize() {
        return keySize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HsmKeyType)) {
            return false;
        }
        HsmKeyType that = (HsmKeyType) o;
        return keySize == that.keySize &&
                isReversible == that.isReversible &&
                hsmJssKeyType == that.hsmJssKeyType &&
                Objects.equals(label, that.label) &&
                importCapability == that.importCapability &&
                renewCapability == that.renewCapability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hsmJssKeyType, label, importCapability, renewCapability, keySize, isReversible);
    }

    public boolean isReversible() {
        return isReversible;
    }

    @Override
    public String toString() {
        return "HsmKeyType{" +
                "hsmJssKeyType=" + hsmJssKeyType +
                ", label='" + label + '\'' +
                ", importCapability=" + importCapability +
                ", renewCapability=" + renewCapability +
                ", keySize=" + keySize +
                ", isReversible=" + isReversible +
                '}';
    }
}
