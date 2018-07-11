package com.elster.jupiter.hsm.model.response;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.Message;

import com.atos.worldline.jss.api.basecrypto.AuthDataDecryptionResponse;

public class AuthDataDecryptResponse extends Message {

    public AuthDataDecryptResponse(AuthDataDecryptionResponse response) throws HsmBaseException {
        super(response.getData());
        if (response.hasAuthenticationFailed()) {
            throw new HsmBaseException("Failed authentication on decrypt request");
        }
    }


}
