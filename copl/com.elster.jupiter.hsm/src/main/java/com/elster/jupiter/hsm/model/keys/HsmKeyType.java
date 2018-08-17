package com.elster.jupiter.hsm.model.keys;

public class HsmKeyType {

    private final String label;
    private final SessionKeyCapability importCapability;
    private final SessionKeyCapability renewCapability;
    private final short keySize;


    public HsmKeyType(String label, SessionKeyCapability importCapability, SessionKeyCapability renewCapability, short keySize) {
        this.label = label;
        this.importCapability = importCapability;
        this.renewCapability = renewCapability;
        this.keySize = keySize;
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

    public short getKeySize() {
        return keySize;
    }
}
