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
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Checks;

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
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * A Plaintext symmetric key is stored encrypted in the DB (using DataVaultService), however, the secret value is shown
 * in plaintext to the user (base64 encoded).
 * This type is NOT secure and is to be used for development or debugging purposes only.
 */
public class PlaintextSymmetricKeyImpl implements PlaintextSymmetricKey {

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
    PlaintextSymmetricKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    PlaintextSymmetricKeyImpl init(KeyType keyType) {
        this.keyTypeReference.set(keyType);
        return this;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    public Optional<SecretKey> getKey() {
        if (Checks.is(this.encryptedKey).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        }
        byte[] decrypt = dataVaultService.decrypt(this.encryptedKey);
        return Optional.of(new SecretKeySpec(decrypt, getKeyType().getKeyAlgorithm()));
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
    public void generateValue() {
        try {
            doRenewValue();
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        }
    }

    private void doRenewValue() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(getKeyType().getKeyAlgorithm());
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

    @Override
    public void delete() {
        dataModel.remove(this);
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
            void copyFromMap(Map<String, Object> properties, PlaintextSymmetricKeyImpl key) {
                if (properties.containsKey(getPropertyName())) {
                    byte[] decode = Base64.getDecoder().decode((String) properties.get(getPropertyName()));
                    key.encryptedKey = key.dataVaultService.encrypt(decode);
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, PlaintextSymmetricKeyImpl key) {
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
        abstract void copyFromMap(Map<String, Object> properties, PlaintextSymmetricKeyImpl key);
        abstract void copyToMap(Map<String, Object> properties, PlaintextSymmetricKeyImpl key);

        String getPropertyName() {
            return propertyName;
        }
    }

}
