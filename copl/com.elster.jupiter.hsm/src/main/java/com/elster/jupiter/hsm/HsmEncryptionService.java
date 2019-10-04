package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;

public interface HsmEncryptionService {

    byte[] symmetricEncrypt(byte[] bytes, String label) throws HsmBaseException, HsmNotConfiguredException;

    byte[] symmetricEncrypt(byte[] bytes, String label, byte[] icv, ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException;

    byte[] symmetricDecrypt(byte[] cipher, String label) throws HsmBaseException, HsmNotConfiguredException;

    byte[] symmetricDecrypt(byte[] cipher, String label, byte[] icv, ChainingMode chainingMode, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException;

    byte[] asymmetricDecrypt(byte[] cipher, String label, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException;

    byte[] asymmetricEncrypt(byte[] bytes, String label, PaddingAlgorithm paddingAlgorithm) throws HsmBaseException;
}
