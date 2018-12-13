/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;


public final class KeyStoreImpl {

    private BigDecimal id;
    private Instant createTime;
    private Instant modTime;
    private String privateKey;
    private String publicKey;
    private final DataModel dataModel;

    @Inject
    KeyStoreImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    void init(DataVaultService dataVaultService) throws NoSuchAlgorithmException {
        Pair<String, String> keyMap = generateKeys(dataVaultService);
        this.id = BigDecimal.ONE;
        this.publicKey = keyMap.getFirst();
        this.privateKey = keyMap.getLast();
        save();
    }

    public BigDecimal getId() {
        return id;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void save() {
        dataModel.mapper(KeyStoreImpl.class).persist(this);
    }

    public void update() {
        dataModel.mapper(KeyStoreImpl.class).update(this);
    }

    public void delete() {
        dataModel.mapper(KeyStoreImpl.class).remove(this);
    }

    private Pair<String, String> generateKeys(DataVaultService dataVaultService) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyGenerator.initialize(1024, random);
        KeyPair keyPair = keyGenerator.genKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        String prvKey = DatatypeConverter.printBase64Binary(privateKey.getEncoded());
        String pubKey = DatatypeConverter.printBase64Binary(publicKey.getEncoded());
        String encryptedPubKey = dataVaultService.encrypt(pubKey.getBytes());
        String encryptedPrvKey = dataVaultService.encrypt(prvKey.getBytes());
        return Pair.of(encryptedPubKey, encryptedPrvKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyStoreImpl keyStore = (KeyStoreImpl) o;

        if (privateKey != null ? !privateKey.equals(keyStore.privateKey) : keyStore.privateKey != null) {
            return false;
        }
        return publicKey != null ? publicKey.equals(keyStore.publicKey) : keyStore.publicKey == null;

    }

    @Override
    public int hashCode() {
        int result = privateKey != null ? privateKey.hashCode() : 0;
        result = 31 * result + (publicKey != null ? publicKey.hashCode() : 0);
        return result;
    }
}

