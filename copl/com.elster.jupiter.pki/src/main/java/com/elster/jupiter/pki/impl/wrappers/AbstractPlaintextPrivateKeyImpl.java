/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.collect.ImmutableMap;

import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Implements storage of a PrivateKey in the DataVault.
 */
abstract public class AbstractPlaintextPrivateKeyImpl implements PrivateKeyWrapper {

    protected final DataVaultService dataVaultService;
    protected final PropertySpecService propertySpecService;
    private final DataModel dataModel;

    private long id;
    private String encryptedPrivateKey;
    private Integer keySize;
    private String curve;

    static final Map<String, Class<? extends AbstractPlaintextPrivateKeyImpl>> IMPLEMENTERS =
            ImmutableMap.of(
                    "RSA", PlaintextRsaPrivateKey.class,
                    "DSA", PlaintextDsaPrivateKey.class,
                    "EC", PlaintextEcdsaPrivateKey.class);

    public AbstractPlaintextPrivateKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return PlaintextPrivateKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        getActualProperties().forEach(p -> p.copyFromMap(properties, this));
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        getActualProperties().forEach(p -> p.copyToMap(properties, this));
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getActualProperties().stream().map(properties -> properties.asPropertySpec(propertySpecService)).collect(toList());
    }

    protected void save() {
        Save.action(id).save(dataModel, this);
    }
    abstract EnumSet<Properties> getActualProperties();

    enum Properties {
        ENCRYPTED_PRIVATE_KEY("encryptedPrivateKey", "privateKey") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), "Encrypted key")
                        .describedAs("Encrypted version of private key")
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey) {
                if (properties.containsKey(getPropertyName())) {
                    byte[] decode = Base64.getDecoder().decode((String) properties.get(getPropertyName()));
                    privateKey.encryptedPrivateKey = privateKey.dataVaultService.encrypt(decode);
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey) {
                byte[] decrypt = privateKey.dataVaultService.decrypt(privateKey.encryptedPrivateKey);
                properties.put(getPropertyName(), Base64.getEncoder().encodeToString(decrypt));
            }
        },
        CURVE("curve", "curve") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec().named(getPropertyName(), "Curve").describedAs("Curve").finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey) {
                if (properties.containsKey(getPropertyName())) {
                    privateKey.curve = (String) properties.get(getPropertyName());
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey) {
                properties.put(getPropertyName(), privateKey.curve);
            }

        },
        KEYSIZE("keySize", "keySize") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec().named(getPropertyName(), "Algorithm").describedAs("Key algorithm").finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey) {
                if (properties.containsKey(getPropertyName())) {
                    privateKey.keySize = (Integer) properties.get(getPropertyName());
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey) {
                properties.put(getPropertyName(), privateKey.keySize);
            }
        };

        private final String fieldName;
        private final String propertyName;

        Properties(String fieldName, String propertyName) {
            this.fieldName = fieldName;
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService);
        abstract void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey);
        abstract void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey);

        String fieldName() {
            return fieldName;
        }

        String getPropertyName() {
            return propertyName;
        }
    }

}
