package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.DecryptResponse;
import com.elster.jupiter.hsm.model.EncryptionResponse;
import com.elster.jupiter.hsm.model.HsmException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEncryptionService {

    EncryptionResponse encrypt(String label, String plainTextKey, String etype) throws HsmException;

    DecryptResponse decrypt(String label, String cipherTxt, String etype) throws HsmException;

}
