package com.elster.jupiter.hsm.model.keys;

public class HsmReversibleKey  implements HsmKey {

    private final byte[] key;
    private final String label;


    public HsmReversibleKey(byte[] key, String label) {
        this.key = key;
        this.label = label;
    }

    public byte[] getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean isReversible() {
        return true;
    }
}
