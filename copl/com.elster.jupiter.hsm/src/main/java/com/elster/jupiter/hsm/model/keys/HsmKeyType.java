package com.elster.jupiter.hsm.model.keys;

public class HsmKeyType {

    private final String label;
    private final SessionKeyCapability importCapability;
    private final SessionKeyCapability renewCapability;
    private final int keySize;


    public HsmKeyType(String label, SessionKeyCapability importCapability, SessionKeyCapability renewCapability, int keySize) {
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

    public int getKeySize() {
        return keySize;
    }
}
