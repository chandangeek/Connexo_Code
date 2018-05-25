package com.elster.jupiter.hsm.model.request;

import javax.annotation.Nonnull;

public class AuthDataDecryptRequest extends AuthDataEncryptRequest {

    private final byte[] authTag;

    public AuthDataDecryptRequest(@Nonnull String keyLabel, @Nonnull byte[] data, byte[] authData, byte[] initialVector, byte[] authTag) {
        super(keyLabel, data, authData, initialVector);
        this.authTag = authTag;
    }

    public byte[] getAuthTag() { return authTag;  }
}
