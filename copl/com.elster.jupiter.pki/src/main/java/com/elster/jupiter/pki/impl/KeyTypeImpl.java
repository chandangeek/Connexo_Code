package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;

/**
 * Created by bvn on 1/18/17.
 */
public class KeyTypeImpl implements KeyType {
    private long id;
    private String name;
    private CryptographicType cryptographicType;
    private String algorithm;
    private Integer keySize;
    private String curve;

    enum Fields {
        NAME("name"),
        CRYPTOGRAPHIC_TYPE("cryptographicType"),
        ALGORITHM("algorithm"),
        KEY_SIZE("keySize"),
        CURVE("curve"),
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public CryptographicType getCryptographicType() {
        return cryptographicType;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    @Override
    public String getCurve() {
        return curve;
    }

    public void setCurve(String curve) {
        this.curve = curve;
    }
}
