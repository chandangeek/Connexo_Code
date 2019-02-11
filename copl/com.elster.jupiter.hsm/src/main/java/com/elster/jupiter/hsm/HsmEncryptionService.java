package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.key.KeyLabel;

public interface HsmEncryptionService {


    byte[] encrypt(byte[] bytes, String label) throws HsmBaseException;

    byte[] decrypt(byte[] cipher, String label) throws HsmBaseException;

    byte[] decrypt(KeyLabel label, byte[] cipher, PaddingAlgorithm paddingAlgorithm) throws  HsmBaseException;
}
