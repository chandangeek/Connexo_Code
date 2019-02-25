package com.elster.jupiter.hsm.model.keys;

import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyType;

import java.util.Objects;

public class HsmKeyType {

    private static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;

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

    public HsmJssKeyType getHsmJssKeyType() {  return hsmJssKeyType; }

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

    public ProtectedSessionKeyType getSessionKeyType() throws HsmBaseException {
        /**
         * This might be wrong but with no specs this is all I could do :)
         * Somehow it works E2E for 16 and 32 bytes. What we do for other sizes!? Hell knows!
         */
        if (HsmJssKeyType.AES.equals(this.hsmJssKeyType)) {
            if (keySize == AES_KEY_LENGTH) {
                return ProtectedSessionKeyType.AES;
            }
            if (keySize == AES256_KEY_LENGTH) {
                return ProtectedSessionKeyType.AES_256;
            }
            throw new HsmBaseException("Could not determine session key type for key length (expected 16 or 32):" + keySize);
        }
        throw new HsmBaseException("Only AES device key accepted:" + keySize);

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
}
