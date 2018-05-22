package com.elster.jupiter.hsm.model;

import javax.annotation.Nonnull;

public class AuthDataEncryptRequest extends Message {

    private final String keyLabel;
    private final byte[] authData;
    private final byte[] initialVector;


    public AuthDataEncryptRequest(@Nonnull String keyLabel,@Nonnull byte[] data, byte[] authData, byte[] initialVector) {
        super(data);
        this.keyLabel = keyLabel;
        this.authData = authData;
        this.initialVector = initialVector;
    }

    public String getKeyLabel() { return keyLabel; }

    public byte[] getAuthData() {
        return authData;
    }

    public byte[] getInitialVector() {
        return initialVector;
    }
}
