/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.assymetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
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
public class PlaintextDsaPrivateKey extends AbstractPlaintextPrivateKeyImpl {

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
    protected void doRenewValue() throws InvalidKeyException, NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(getKeyType().getKeySize(), new SecureRandom());
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();
        setEncryptedPrivateKey(dataVaultService.encrypt(privateKey.getEncoded()));
    }


}
