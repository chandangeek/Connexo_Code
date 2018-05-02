/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.ChainingValue;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.SecretKey;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;

public class HsmSecurityImpl  {

    public EncryptionResponse encrypt(SecretKey secretKey, KeyDerivation[] derivations, byte[] clear, ChainingValue icv,
                                      PaddingAlgorithm padding, ChainingMode chaining) throws FunctionFailedException {
        return new EncryptionResponse(Symmetric.encrypt(secretKey
                , derivations, clear, icv, padding, chaining).getData());
    }
}
