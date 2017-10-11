/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by bvn on 2/14/17.
 */
public final class PlaintextDsaPrivateKey extends AbstractPlaintextPrivateKeyWrapperImpl {

    @Inject
    PlaintextDsaPrivateKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        super(dataVaultService, propertySpecService, dataModel, thesaurus);
    }

    @Override
    protected PrivateKey doGetPrivateKey() throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        byte[] decrypt = dataVaultService.decrypt(getEncryptedPrivateKey());
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decrypt));
    }

    @Override
    protected PublicKey doGenerateValue() throws InvalidKeyException, NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(getKeyType().getKeySize(), new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        setPrivateKey(privateKey);
        this.save();
        return keyPair.getPublic();
    }

    protected PublicKey doGetPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        DSAPrivateKey privateKey = (DSAPrivateKey)getPrivateKey().get();
        DSAParams dsaParams = privateKey.getParams();
        BigInteger p = dsaParams.getP();
        BigInteger q = dsaParams.getQ();
        BigInteger g = dsaParams.getG();
        BigInteger y = g.modPow(privateKey.getX(), p);

        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        KeySpec publicKeySpec = new DSAPublicKeySpec(y, p, q, g);
        return keyFactory.generatePublic(publicKeySpec);
    }

}
