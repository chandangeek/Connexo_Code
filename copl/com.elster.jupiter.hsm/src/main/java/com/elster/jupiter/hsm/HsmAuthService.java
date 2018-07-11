package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.request.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.request.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.HsmBaseException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmAuthService {

    AuthDataEncryptResponse encrypt(AuthDataEncryptRequest authDataEncRequest) throws HsmBaseException;

    AuthDataDecryptResponse decrypt(AuthDataDecryptRequest authDataEncRequest) throws HsmBaseException;

}
