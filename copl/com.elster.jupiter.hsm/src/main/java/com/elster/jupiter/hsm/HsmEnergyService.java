package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.keys.TransportKey;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEnergyService {
    HsmEncryptedKey importKey(TransportKey tKey, DeviceKey dKey, String deviceKeyLabel, SessionKeyCapability sessionKeyCapability) throws HsmBaseException;

    HsmEncryptedKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException;
}
