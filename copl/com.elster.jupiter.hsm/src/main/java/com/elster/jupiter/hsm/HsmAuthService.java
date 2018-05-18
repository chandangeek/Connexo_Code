package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmAuthService {

    AuthDataEncryptResponse encrypt(AuthDataEncryptRequest authDataEncRequest) throws EncryptBaseException;

}
