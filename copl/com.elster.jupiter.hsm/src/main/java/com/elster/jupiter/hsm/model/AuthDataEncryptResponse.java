package com.elster.jupiter.hsm.model;

import com.atos.worldline.jss.api.basecrypto.AuthDataEncryptionResponse;

public class AuthDataEncryptResponse extends Message {


    public AuthDataEncryptResponse(AuthDataEncryptionResponse authDataEncryptionResponse) {
        super(authDataEncryptionResponse.getData());
    }

}
