package com.energyict.common;

public class RenewKeyData {

    private final String keyAccessorName;
    private final String newKey;
    private final String newWrappedKey;

    public RenewKeyData(String keyAccessorName, String newKey, String newWrappedKey) {
        this.keyAccessorName = keyAccessorName;
        this.newKey = newKey;
        this.newWrappedKey = newWrappedKey;
    }

    public String getKeyAccessorName() {
        return keyAccessorName;
    }

    public String getNewKey() {
        return newKey;
    }

    public String getNewWrappedKey() {
        return newWrappedKey;
    }
}
