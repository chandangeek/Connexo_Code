/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
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
    @Size(max = Table.MAX_STRING_LENGTH, message = "{"+MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String encryptedPrivateKey;
    private Reference<KeyType> keyTypeReference = Reference.empty();

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

    public AbstractPlaintextPrivateKeyImpl init(KeyAccessorType keyAccessorType) {
        keyTypeReference.set(keyAccessorType.getKeyType());
        return this;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public KeyType getKeyType() {
        return keyTypeReference.get();
    }

    @Override
    public String getKeyEncryptionMethod() {
        return PlaintextPrivateKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, this));
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, this));
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return EnumSet.allOf(Properties.class)
                .stream().map(properties -> properties.asPropertySpec(propertySpecService)).collect(toList());
    }

    protected void save() {
        Save.action(id).save(dataModel, this);
    }

    enum Properties {
        ENCRYPTED_PRIVATE_KEY("encryptedPrivateKey", "privateKey") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), "Private key")
                        .describedAs("Plaintext view of private key")
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
        ;

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
