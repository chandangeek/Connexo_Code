/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
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
import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * A Plaintext symmetric key is stored encrypted in the DB (using DataVaultService), however, the secret value can be shown
 * in plaintext to the user (hex string).
 * This type is provides security through implementation of a software security model, without use of an HSM
 **/
public final class PlaintextSymmetricKeyImpl extends KeyImpl implements PlaintextSymmetricKey {

    protected final DataVaultService dataVaultService;
    protected final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    PlaintextSymmetricKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        super(dataModel);
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    PlaintextSymmetricKeyImpl init(KeyType keyType, TimeDuration timeDuration) {
        super.getKeyTypeReference().set(keyType);
        this.calculateExpirationTime(timeDuration);
        return this;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    @Override
    public Optional<SecretKey> getKey() {
        if (Checks.is(this.getEncryptedKey()).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        }
        byte[] decrypt = dataVaultService.decrypt(this.getEncryptedKey());
        return Optional.of(new SecretKeySpec(decrypt, getKeyType().getKeyAlgorithm()));
    }

    private KeyType getKeyType() {
        return getKeyTypeReference().get();
    }

    @Override
    public void setKey(SecretKey key) {
        super.setEncryptedKey(dataVaultService.encrypt(key.getEncoded()));
        this.save();
    }

    private void calculateExpirationTime(TimeDuration timeDuration) {
         super.setExpirationTime(ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant());
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
        Save.UPDATE.validate(super.getDataModel(), propertySetter);
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

    public enum Properties {
        DECRYPTED_KEY("key") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), "Key")
                        .describedAs("Plain text key")
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
        },;

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
    @KeySize(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_KEY_SIZE + "}")
    public class PropertySetter {
        @HexStringKey(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALID_HEX_VALUE + "}")
        private String key; // field name must match property name

        PropertySetter(PlaintextSymmetricKeyImpl source) {
            byte[] decrypt = dataVaultService.decrypt(source.getEncryptedKey());
            this.key = DatatypeConverter.printHexBinary(decrypt);
        }

        void applyProperties() {
            byte[] decode = DatatypeConverter.parseHexBinary(key);
            PlaintextSymmetricKeyImpl.super.setEncryptedKey(dataVaultService.encrypt(decode));
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
