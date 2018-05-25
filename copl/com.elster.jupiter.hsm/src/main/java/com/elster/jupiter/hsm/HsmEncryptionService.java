package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.request.DecryptRequest;
import com.elster.jupiter.hsm.model.response.DecryptResponse;
import com.elster.jupiter.hsm.model.request.EncryptRequest;
import com.elster.jupiter.hsm.model.response.EncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEncryptionService {

    EncryptResponse encrypt(EncryptRequest eRequest) throws EncryptBaseException;

    DecryptResponse decrypt(DecryptRequest dRequest) throws EncryptBaseException;

}
