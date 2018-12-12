package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.model.Message;


public class AuthDataEncryptRequest extends Message {

    private final String keyLabel;
    private final byte[] authData;
    private final byte[] initialVector;


    public AuthDataEncryptRequest(String keyLabel, byte[] data, byte[] authData, byte[] initialVector) {
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
