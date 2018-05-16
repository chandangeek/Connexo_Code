package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.EncryptedAuthData;
import com.elster.jupiter.hsm.model.HsmException;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmAuthService {

    EncryptedAuthData authDataEncrypt(String keyLabel, String plainTxt) throws HsmException;

}
