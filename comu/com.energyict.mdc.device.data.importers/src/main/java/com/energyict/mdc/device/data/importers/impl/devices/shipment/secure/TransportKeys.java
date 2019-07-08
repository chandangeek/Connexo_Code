package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.Shipment;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;

import java.util.List;
import java.util.Objects;

public class TransportKeys {

    private final List<WrapKey> keys;

    public TransportKeys(Shipment shipment) {
        if (Objects.isNull(shipment) || Objects.isNull(shipment.getHeader()) || Objects.isNull(shipment.getHeader().getWrapKey())) {
            throw new ImportFailedException(MessageSeeds.IMPORT_FAILED_NO_TRANSPORT_KEYS_DEFINED);
        }
        List<WrapKey> wrapKeys = shipment.getHeader().getWrapKey();
        for (WrapKey key: wrapKeys) {
            if (Objects.isNull(key.getSymmetricKey()) || Objects.isNull(key.getSymmetricKey().getCipherData()) ||
                    Objects.isNull(key.getSymmetricKey().getCipherData().getCipherValue())) {
                throw new ImportFailedException(MessageSeeds.INVALID_WRAPPER_KEY, key.getLabel());
            }
        }
        this.keys = wrapKeys;
    }

    public byte[] getBytes(String wrapKeyLabel) {
        WrapKey wrapKey = get(wrapKeyLabel);
        return wrapKey.getSymmetricKey().getCipherData().getCipherValue();
    }

    public WrapKey get(String wrapKeyLabel) {
        for (WrapKey key: keys) {
            if (key.getLabel().equals(wrapKeyLabel)) {
                return key;
            }
        }
        throw new ImportFailedException(MessageSeeds.UNKNOWN_WRAPPER_KEY, wrapKeyLabel);
    }
}
