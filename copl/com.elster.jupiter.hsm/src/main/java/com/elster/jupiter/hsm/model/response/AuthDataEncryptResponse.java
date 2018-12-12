package com.elster.jupiter.hsm.model.response;

import com.elster.jupiter.hsm.model.Message;

import com.atos.worldline.jss.api.basecrypto.AuthDataEncryptionResponse;

public class AuthDataEncryptResponse extends Message {

    private final byte[] authTag;
    private final byte[] initialVector;

    public AuthDataEncryptResponse(AuthDataEncryptionResponse authDataEncryptionResponse) {
        super(authDataEncryptionResponse.getData());
        this.authTag = authDataEncryptionResponse.getAuthTag();
        this.initialVector = authDataEncryptionResponse.getInitialVector();
    }

    public byte[] getAuthTag() {
        return authTag;
    }

    public byte[] getInitialVector() {
        return initialVector;
    }
}
