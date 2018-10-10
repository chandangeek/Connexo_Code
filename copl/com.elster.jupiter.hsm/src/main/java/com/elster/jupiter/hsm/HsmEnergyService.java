package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.HsmRenewKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEnergyService {
    HsmEncryptedKey importKey(ImportKeyRequest importKeyRequest) throws HsmBaseException;

    HsmRenewKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException;
}
