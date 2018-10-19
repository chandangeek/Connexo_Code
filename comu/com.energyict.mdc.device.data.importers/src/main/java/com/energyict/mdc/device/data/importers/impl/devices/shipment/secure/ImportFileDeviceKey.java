package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;

public class ImportFileDeviceKey {

    private final byte[] encryptedKey;

    public ImportFileDeviceKey(byte[] encryptedKey){
        if (encryptedKey.length <= 16) {
            throw new ImportFailedException(MessageSeeds.INITIALIZATION_VECTOR_ERROR);
        }
        this.encryptedKey = encryptedKey;
    }


    public byte[] getInitializationVector(){
        byte[] initializationVector = new byte[16];
        System.arraycopy(encryptedKey, 0, initializationVector, 0, 16);
        return initializationVector;
    }

    public byte[] getCipher() {
        byte[] cipher = new byte[encryptedKey.length - 16];
        System.arraycopy(encryptedKey, 16, cipher, 0, encryptedKey.length - 16);
        return cipher;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

}
