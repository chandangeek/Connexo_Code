package com.elster.jupiter.hsm.model;

import com.atos.worldline.jss.api.basecrypto.AuthDataDecryptionResponse;

public class AuthDataDecryptResponse extends Message {

    public AuthDataDecryptResponse(AuthDataDecryptionResponse response) {
        super(response.getData());
    }

}
