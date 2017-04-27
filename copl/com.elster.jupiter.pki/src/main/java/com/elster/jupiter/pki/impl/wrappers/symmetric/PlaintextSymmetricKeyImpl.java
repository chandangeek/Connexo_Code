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
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * A Plaintext symmetric key is stored encrypted in the DB (using DataVaultService), however, the secret value can be shown
 * in plaintext to the user (base64 encoded).
 * This type is provides security through implementation of a software security model, without use of an HSM
 **/
public final class PlaintextSymmetricKeyImpl implements PlaintextSymmetricKey {

    protected final DataVaultService dataVaultService;
    protected final PropertySpecService propertySpecService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final Clock clock;

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
    PlaintextSymmetricKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    PlaintextSymmetricKeyImpl init(KeyType keyType, TimeDuration timeDuration) {
        this.keyTypeReference.set(keyType);
        this.setExpirationTime(timeDuration);
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
        this.save();
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    private void setExpirationTime(TimeDuration timeDuration) {
        this.expirationTime = ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant();
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
        PropertySetter propertySetter = new PropertySetter(this);
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, propertySetter));
        Save.UPDATE.validate(dataModel, propertySetter);
        propertySetter.applyProperties();
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, new PropertySetter(this)));
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
        DECRYPTED_KEY("key") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.base64StringSpec()
                        .named(getPropertyName(), "Key")
                        .describedAs("Base64 encoded key")
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, PropertySetter propertySetter) {
                if (properties.containsKey(getPropertyName())) {
                    propertySetter.key = (String) properties.get(getPropertyName());
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, PropertySetter propertySetter) {
                properties.put(getPropertyName(), propertySetter.key);
            }
        },
        ;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService);
        abstract void copyFromMap(Map<String, Object> properties, PropertySetter key);
        abstract void copyToMap(Map<String, Object> properties, PropertySetter key);

        String getPropertyName() {
            return propertyName;
        }
    }

    /**
     * Intermediate class: properties are gotten and set by this class, allowing intermediate validation
     */
    @KeySize(groups = {Save.Create.class, Save.Update.class}, message = "{"+MessageSeeds.Keys.INVALID_KEY_SIZE+"}")
    public class PropertySetter  {
        @Base64EncodedKey(groups = {Save.Create.class, Save.Update.class}, message = "{"+MessageSeeds.Keys.INVALID_VALUE+"}")
        private String key; // field name must match property name

        PropertySetter(PlaintextSymmetricKeyImpl source) {
            byte[] decrypt = dataVaultService.decrypt(source.encryptedKey);
            this.key = Base64.getEncoder().encodeToString(decrypt);
        }

        void applyProperties() {
            byte[] decode = Base64.getDecoder().decode(key);
            PlaintextSymmetricKeyImpl.this.encryptedKey = dataVaultService.encrypt(decode);

            PlaintextSymmetricKeyImpl.this.save();
        }

        public String getKey() {
            return key;
        }

        public int getKeySize() {
            return getKeyType().getKeySize();
        }
    }


}
