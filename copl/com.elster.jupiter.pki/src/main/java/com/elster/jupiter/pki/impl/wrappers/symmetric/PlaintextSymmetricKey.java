/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.Renewable;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 2/16/17.
 */
public class PlaintextSymmetricKey implements SymmetricKeyWrapper, Renewable {

    protected final DataVaultService dataVaultService;
    protected final PropertySpecService propertySpecService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    public enum Fields {
        ENCRYPTED_KEY("encryptedKey"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),
        ;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private long id;
    @Size(max = Table.MAX_STRING_LENGTH, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String encryptedKey;
    private Reference<KeyType> keyTypeReference = Reference.empty();
    private Instant expirationTime;

    @Inject
    PlaintextSymmetricKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    PlaintextSymmetricKey init(KeyType keyType) {
        this.keyTypeReference.set(keyType);
        return this;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    public SecretKey getKey() {
        byte[] decrypt = dataVaultService.decrypt(this.encryptedKey);
        return new SecretKeySpec(decrypt, getKeyType().getAlgorithm());
    }

    private KeyType getKeyType() {
        return keyTypeReference.get();
    }

    public void setKey(SecretKey key) {
        this.encryptedKey=dataVaultService.encrypt(key.getEncoded());
    }

    @Override
    public Instant getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public void renewValue() {
        try {
            doRenewValue();
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        }
    }

    private void doRenewValue() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(getKeyType().getAlgorithm());
        keyGenerator.init(getKeyType().getKeySize());
        setKey(keyGenerator.generateKey());
        this.save();
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

    public enum Properties {
        ENCRYPTED_KEY("key") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), "key")
                        .describedAs("Plaintext view of key")
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, PlaintextSymmetricKey key) {
                if (properties.containsKey(getPropertyName())) {
                    byte[] decode = Base64.getDecoder().decode((String) properties.get(getPropertyName()));
                    key.encryptedKey = key.dataVaultService.encrypt(decode);
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, PlaintextSymmetricKey key) {
                byte[] decrypt = key.dataVaultService.decrypt(key.encryptedKey);
                properties.put(getPropertyName(), Base64.getEncoder().encodeToString(decrypt));
            }
        },
        ;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService);
        abstract void copyFromMap(Map<String, Object> properties, PlaintextSymmetricKey key);
        abstract void copyToMap(Map<String, Object> properties, PlaintextSymmetricKey key);

        String getPropertyName() {
            return propertyName;
        }
    }

}
