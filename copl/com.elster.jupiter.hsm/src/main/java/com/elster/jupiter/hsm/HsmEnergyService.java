package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.IrreversibleKey;
import com.elster.jupiter.hsm.model.keys.KeyType;
import com.elster.jupiter.hsm.model.keys.TransportKey;

public interface HsmEnergyService {
    IrreversibleKey importKey(TransportKey tKey, DeviceKey dKey, String deviceKeyLabel, KeyType keyType) throws EncryptBaseException;
}
