package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmAuthService {

    AuthDataEncryptResponse encrypt(AuthDataEncryptRequest authDataEncRequest) throws EncryptBaseException;

    AuthDataDecryptResponse decrypt(AuthDataDecryptRequest authDataEncRequest) throws EncryptBaseException;

}
