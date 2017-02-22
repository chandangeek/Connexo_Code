/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.assymetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.Renewable;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Implements storage of a PrivateKey in the DataVault.
 */
abstract public class AbstractPlaintextPrivateKeyImpl implements PrivateKeyWrapper, Renewable {

    protected final DataVaultService dataVaultService;
    protected final PropertySpecService propertySpecService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    private long id;
    @Size(max = Table.MAX_STRING_LENGTH, message = "{"+MessageSeeds.Keys.FIELD_TOO_LONG+"}")
    private String encryptedPrivateKey;
    private Reference<KeyType> keyTypeReference = Reference.empty();
    private Instant expirationTime;

    public static final Map<String, Class<? extends PrivateKeyWrapper>> IMPLEMENTERS =
            ImmutableMap.of(
                    "RSA", PlaintextRsaPrivateKey.class,
                    "DSA", PlaintextDsaPrivateKey.class,
                    "EC", PlaintextEcdsaPrivateKey.class);

    AbstractPlaintextPrivateKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
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

    protected KeyType getKeyType() {
        return keyTypeReference.get();
    }

    @Override
    public Instant getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD;
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

    public void save() {
        Save.action(id).save(dataModel, this);
    }

    @Override
    public PrivateKey getPrivateKey() {
        try {
            return doGetPrivateKey();
        } catch (InvalidKeyException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (InvalidKeySpecException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY_SPECIFICATION, e);
        } catch (NoSuchProviderException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.UNKNOWN_PROVIDER, e);
        }
    }

    protected abstract PrivateKey doGetPrivateKey() throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException;

    @Override
    public void renewValue() {
        try {
            doRenewValue();
        } catch (InvalidKeyException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (InvalidKeySpecException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY_SPECIFICATION, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_ALGORITHM_PARAMETERS, e);
        } catch (NoSuchProviderException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.UNKNOWN_PROVIDER, e);
        }
    }

    protected abstract void doRenewValue() throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchProviderException, InvalidAlgorithmParameterException;

    public enum Fields {
        ENCRYPTED_KEY("encryptedPrivateKey"),
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

    public enum Properties {
        ENCRYPTED_PRIVATE_KEY("privateKey") {
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

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService);
        abstract void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey);
        abstract void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyImpl privateKey);

        String getPropertyName() {
            return propertyName;
        }
    }

}
