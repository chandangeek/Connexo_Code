package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.DecryptRequest;
import com.elster.jupiter.hsm.model.DecryptResponse;
import com.elster.jupiter.hsm.model.EncryptRequest;
import com.elster.jupiter.hsm.model.EncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEncryptionService {

    EncryptResponse encrypt(EncryptRequest eRequest) throws EncryptBaseException;

    DecryptResponse decrypt(DecryptRequest dRequest) throws EncryptBaseException;

}
