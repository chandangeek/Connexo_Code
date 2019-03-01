package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.model.HsmBaseException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

public interface Algorithm {
    /**
     *
     * @return cipher like string format (like AES/CBC/NoPadding)
     */
    String getCipher();

    Type getType();

    HsmAlgorithmSpecs getHsmSpecs();


    default Cipher newCipher() throws HsmBaseException {
        try {
            return Cipher.getInstance(getCipher());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new HsmBaseException(e);
        }
    }
}
