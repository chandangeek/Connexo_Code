/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by bvn on 2/14/17.
 */
public final class PlaintextRsaPrivateKey extends AbstractPlaintextPrivateKeyWrapperImpl {


    @Inject
    PlaintextRsaPrivateKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        super(dataVaultService, propertySpecService, dataModel, thesaurus);
    }

    @Override
    protected PrivateKey doGetPrivateKey() throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        byte[] decrypt = dataVaultService.decrypt(getEncryptedPrivateKey());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decrypt));
    }

    @Override
    protected PublicKey doGenerateValue() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(getKeyType().getKeySize(), new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        setPrivateKey(privateKey);
        this.save();
        return keyPair.getPublic();
    }

    protected PublicKey doGetPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey)getPrivateKey().get();
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicKeySpec);
    }
}
