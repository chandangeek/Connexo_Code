/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.assymetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by bvn on 2/14/17.
 */
public class PlaintextRsaPrivateKey extends AbstractPlaintextPrivateKeyImpl {


    @Inject
    public PlaintextRsaPrivateKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel) {
        super(dataVaultService, propertySpecService, dataModel);
    }

    @Override
    public PrivateKey getPrivateKey() throws InvalidKeyException {
        try {
            byte[] decrypt = dataVaultService.decrypt(getEncryptedPrivateKey());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decrypt));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidKeyException(e);
        }
    }

    @Override
    public PrivateKeyWrapper renewValue() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(getKeyType().getKeySize(), new SecureRandom());
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();
        setEncryptedPrivateKey(dataVaultService.encrypt(privateKey.getEncoded()));
        return this;
    }
}
