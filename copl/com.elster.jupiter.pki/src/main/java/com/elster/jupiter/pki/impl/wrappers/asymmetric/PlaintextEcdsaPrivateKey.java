/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.inject.Inject;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by bvn on 2/14/17.
 */
public final class PlaintextEcdsaPrivateKey extends AbstractPlaintextPrivateKeyWrapperImpl {

    @Inject
    PlaintextEcdsaPrivateKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        super(dataVaultService, propertySpecService, dataModel, thesaurus);
    }

    @Override
    protected PrivateKey doGetPrivateKey() throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchProviderException {
        byte[] decrypt = dataVaultService.decrypt(getEncryptedPrivateKey());
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decrypt));
    }

    @Override
    protected void doGenerateValue() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(getKeyType().getCurve());
        keyGen.initialize(parameterSpec, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        setPrivateKey(privateKey);
        this.save();
    }

    @Override
    protected PublicKey doGetPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        BCECPrivateKey privateKey = (BCECPrivateKey) getPrivateKey();
        BigInteger d = privateKey.getD();
//        BigInteger d = new BigInteger(getPrivateKey().getEncoded());
        X9ECParameters curve = SECNamedCurves.getByName(getKeyType().getCurve());
        ECParameterSpec ecParameterSpec = new ECParameterSpec(curve.getCurve(), curve.getG(), curve.getN(), curve.getH(), curve.getSeed());
        ECPoint q = ecParameterSpec.getG().multiply(d);
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        return keyFactory.generatePublic(new ECPublicKeySpec(q, ecParameterSpec));
    }
}
