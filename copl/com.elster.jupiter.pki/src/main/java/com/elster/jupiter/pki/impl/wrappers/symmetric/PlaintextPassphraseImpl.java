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
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.SecretFactory;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * A Plaintext passphrase is stored encrypted in the DB (using DataVaultService), however, the passphrase can be shown
 * in plaintext to the user.
 * This type is provides security through implementation of a software security model, without use of an HSM
 */
public final class PlaintextPassphraseImpl implements PlaintextPassphrase {

    protected DataVaultService dataVaultService;
    protected PropertySpecService propertySpecService;
    private DataModel dataModel;
    private Thesaurus thesaurus;
    private Clock clock;

    public enum Fields {
        PASSPHRASE("encryptedPassphrase"),
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
    private String encryptedPassphrase;
    private Reference<KeyType> keyTypeReference = Reference.empty();
    private Instant expirationTime;

    PlaintextPassphraseImpl() {
        super();
    }

    @Inject
    PlaintextPassphraseImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.clock = clock;

    }

    PlaintextPassphraseImpl init(KeyType keyType, TimeDuration timeDuration) {
        this.keyTypeReference.set(keyType);
        this.setExpirationTime(timeDuration);
        return this;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultPassphraseFactory.KEY_ENCRYPTION_METHOD;
    }

    @Override
    @XmlElement
    public Optional<String> getEncryptedPassphrase() {
        if (dataVaultService == null) {
            return Optional.of(encryptedPassphrase);
        }
        if (Checks.is(this.encryptedPassphrase).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        }
        byte[] decrypt = dataVaultService.decrypt(this.encryptedPassphrase);
        return Optional.of(new String(decrypt));
    }

    private KeyType getKeyType() {
        return keyTypeReference.get();
    }

    @Override
    public void setEncryptedPassphrase(String plainTextPassphrase) {
        if (dataVaultService == null) {
            encryptedPassphrase = plainTextPassphrase;
        } else {
            this.encryptedPassphrase = dataVaultService.encrypt(plainTextPassphrase.getBytes());
            this.save();
        }
    }

    @Override
    @XmlTransient
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    private void setExpirationTime(TimeDuration timeDuration) {
        this.expirationTime = ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant();
    }

    @Override
    public void generateValue() {
        try {
            doRenewValue(getKeyType());
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        }
    }

    private void doRenewValue(KeyType keyType) throws NoSuchAlgorithmException {
        SecretFactory secretFactory = new SecretFactory();
        String password = secretFactory.generatePassword(keyType);
        setEncryptedPassphrase(password);
        this.save();
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, this));
        this.save();
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, this));
        return properties;
    }

    @Override
    @XmlTransient
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
        DECRYPTED_PASSPHRASE("passphrase") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), "Passphrase")
                        .describedAs("Plaintext passphrase")
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, PlaintextPassphraseImpl plaintextPassphrase) {
                if (properties.containsKey(getPropertyName())) {
                    plaintextPassphrase.setEncryptedPassphrase((String) properties.get(getPropertyName()));
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, PlaintextPassphraseImpl plaintextPassphrase) {
                properties.put(getPropertyName(), plaintextPassphrase.getEncryptedPassphrase().orElse(null));
            }
        },
        ;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService);
        abstract void copyFromMap(Map<String, Object> properties, PlaintextPassphraseImpl plaintextPassphrase);
        abstract void copyToMap(Map<String, Object> properties, PlaintextPassphraseImpl plaintextPassphrase);

        String getPropertyName() {
            return propertyName;
        }
    }

}
