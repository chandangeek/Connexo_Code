/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.EnumSet;

/**
 * Created by bvn on 2/14/17.
 */
public class PlaintextEcdsaPrivateKey extends AbstractPlaintextPrivateKeyImpl {

    @Inject
    public PlaintextEcdsaPrivateKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel) {
        super(dataVaultService, propertySpecService, dataModel);
    }

    @Override
    EnumSet<Properties> getActualProperties() {
        return EnumSet.of(Properties.ENCRYPTED_PRIVATE_KEY, Properties.CURVE);
    }


    @Override
    public PrivateKey getPrivateKey() throws InvalidKeyException {
        try {
            byte[] decrypt = dataVaultService.decrypt(getEncryptedPrivateKey());
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decrypt));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new InvalidKeyException(e);
        }
    }

}
