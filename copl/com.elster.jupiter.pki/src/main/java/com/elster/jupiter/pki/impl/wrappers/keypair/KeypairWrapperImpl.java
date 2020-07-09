/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.keypair;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.EventType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.pki.impl.UniqueAlias;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@UniqueAlias(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.ALIAS_UNIQUE + "}")
public class KeypairWrapperImpl implements KeypairWrapper {

    private final DataModel dataModel;
    private final EventService eventService;

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public enum Fields {
        PUBLIC_KEY("publicKey"),
        PRIVATE_KEY("privateKeyReference"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),
        ALIAS("alias"),
        ID("id")
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
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(blacklisted = {'<', '>'})
    private String alias;
    private byte[] publicKey;
    private Instant expirationTime;
    private Reference<KeyType> keyTypeReference = Reference.empty();
    private RefAny privateKeyReference;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public KeypairWrapperImpl(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, EventService eventService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.eventService = eventService;
    }

    public KeypairWrapperImpl init(KeyType keyType, PrivateKeyWrapper privateKeyWrapper) {
        init(keyType);
        this.privateKeyReference = dataModel.asRefAny(privateKeyWrapper);
        return this;
    }

    public KeypairWrapperImpl init(KeyType keyType) {
        this.keyTypeReference.set(keyType);
        return this;
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String getName() {
        return this.alias;
    }

    @Override
    public Optional<PublicKey> getPublicKey() {
        if (this.publicKey == null || this.publicKey.length==0) {
            return Optional.empty();
        }
        return Optional.ofNullable(getPublicKeyFromBytes(publicKey));
    }

    private PublicKey getPublicKeyFromBytes(byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(getKeyType().getKeyAlgorithm());
            KeySpec keySpec = new X509EncodedKeySpec(publicKey);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (InvalidKeySpecException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.PUBLIC_KEY_INVALID, e);
        }
    }

    @Override
    public void setPublicKey(byte[] key) {
        if (key == null || key.length==0) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY);
        }
        setPublicKey(getPublicKeyFromBytes(key));
    }

    @Override
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey.getEncoded();
        this.save();
    }

    @Override
    public Optional<PrivateKeyWrapper> getPrivateKeyWrapper() {
        if (this.privateKeyReference==null || !this.privateKeyReference.isPresent()) {
            return Optional.empty();
        }
        return Optional.of((PrivateKeyWrapper) privateKeyReference.get());
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void delete() {
//        this.eventService.postEvent(EventType.KEYPAIR_VALIDATE_DELETE.topic(), this);
        dataModel.remove(this);
//        this.eventService.postEvent(EventType.KEYPAIR_DELETED.topic(), this);
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, this));
        if (this.hasPrivateKey()) {
            ((PrivateKeyWrapper)this.privateKeyReference.get()).setProperties(properties);
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, this));
        if (this.hasPrivateKey()) {
            properties.putAll(((PrivateKeyWrapper)this.privateKeyReference.get()).getProperties());
        }
        return properties;
    }

    @Override
    public KeyType getKeyType() {
        return keyTypeReference.get();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> collect = EnumSet.allOf(Properties.class)
                .stream()
                .map(properties -> properties.asPropertySpec(propertySpecService, thesaurus))
                .collect(toList());
        if (this.hasPrivateKey()) {
            collect.addAll(((PrivateKeyWrapper)this.privateKeyReference.get()).getPropertySpecs());
        }
        return collect;
    }

    public enum Properties {
        KEYPAIR_ALIAS("alias") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), TranslationKeys.ALIAS).fromThesaurus(thesaurus)
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, KeypairWrapperImpl certificateWrapper) {
                if (properties.containsKey(getPropertyName())) {
                    certificateWrapper.setAlias((String) properties.get(getPropertyName()));
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, KeypairWrapperImpl certificateWrapper) {
                properties.put(getPropertyName(), certificateWrapper.getAlias());
            }
        },
        PUBLIC_KEY("publicKey") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), TranslationKeys.PUBLIC_KEY).fromThesaurus(thesaurus)
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, KeypairWrapperImpl certificateWrapper) {
                if (properties.containsKey(getPropertyName())) {
                    certificateWrapper.setPublicKey(Base64.getDecoder().decode((String) properties.get(getPropertyName())));
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, KeypairWrapperImpl certificateWrapper) {
                Object publicKeyAsString = certificateWrapper.getPublicKey().isPresent() ?
                        Base64.getEncoder().encode(certificateWrapper.getPublicKey().get().getEncoded()) :
                        "";
                properties.put(getPropertyName(), publicKeyAsString);
            }
        },
        ;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);
        abstract void copyFromMap(Map<String, Object> properties, KeypairWrapperImpl certificateWrapper);
        abstract void copyToMap(Map<String, Object> properties, KeypairWrapperImpl certificateWrapper);

        String getPropertyName() {
            return propertyName;
        }
    }


    public void save() {
        Save.action(id).save(dataModel, this);
    }

    @Override
    public boolean hasPrivateKey() {
        try {
            return this.privateKeyReference!=null
                    && this.privateKeyReference.isPresent()
                    && ((PrivateKeyWrapper)this.privateKeyReference.get()).getPrivateKey().isPresent();
        } catch (InvalidKeyException e) {
            return false;
        }
    }

    @Override
    public Optional<String> getKeyEncryptionMethod() {
        if (!hasPrivateKey()) {
            return Optional.empty();
        }
        return Optional.of(((PrivateKeyWrapper)this.privateKeyReference.get()).getKeyEncryptionMethod());
    }

    @Override
    public void generateValue() {
        if (this.privateKeyReference==null
                || !this.privateKeyReference.isPresent()) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CAN_NOT_GENERATE_PUBLIC);
        }
        PrivateKeyWrapper privateKeyWrapper = (PrivateKeyWrapper) this.privateKeyReference.get();
        PublicKey publicKey = privateKeyWrapper.generateValue();
        setPublicKey(publicKey);
    }


}
