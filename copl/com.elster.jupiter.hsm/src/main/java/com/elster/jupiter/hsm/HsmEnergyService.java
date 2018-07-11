package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.KeyType;
import com.elster.jupiter.hsm.model.keys.TransportKey;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmEnergyService {
    HsmEncryptedKey importKey(TransportKey tKey, DeviceKey dKey, String deviceKeyLabel, KeyType keyType) throws HsmBaseException;

    HsmEncryptedKey renewKey(byte[] deviceKey, String signKeyLabel, String deviceKeyLabel) throws HsmBaseException;
}
