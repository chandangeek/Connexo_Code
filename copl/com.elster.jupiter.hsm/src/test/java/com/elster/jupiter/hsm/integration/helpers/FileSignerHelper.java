package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * This is not a real test but rather a helper for testers since we had issues validating signature of files (CSRImporter)
 */
public class FileSignerHelper {

    public byte[] signFile(AsymmetricAlgorithm alg, String signMethod, byte[] bits, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(signMethod);
        signature.initSign(key);
        signature.update(bits);
        return signature.sign();
    }


}
