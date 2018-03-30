package com.energyict.mdc.upl;


import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

/**
 * Provides functionality for firmware signature check
 * Connexo core can call these methods in order to do the check before the firmware ends up in the device.
 * The actual check will be done by {@link DeviceProtocol}
 * <p/>
 * <p>
 * Created by H165680 on 3/13/2018.
 */
public interface FirmwareSignatureCheckSupport {

    default boolean firmwareSignatureCheckSupported() {
        return false;
    }

    default boolean verifyFirmwareSignature(File firmwareFile, PublicKey pubKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        //By default do not provide support for firmware signature check. Where needed overwrite the method and provide proper implementation for this method
        throw new IOException("NotImplementedException");
    }
}