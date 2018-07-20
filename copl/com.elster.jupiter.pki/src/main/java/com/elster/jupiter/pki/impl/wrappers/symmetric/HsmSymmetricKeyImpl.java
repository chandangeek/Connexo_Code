/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.HsmSymmetricKey;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class HsmSymmetricKeyImpl implements HsmSymmetricKey{

    private final DataVaultService dataVaultService;
    private final PropertySpecService propertySpecService;
    private final DataModel dataModel;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final HsmEnergyService hsmEnergyService;

    public enum Fields {
        ENCRYPTED_KEY("encryptedKey"),
        LABEL("label"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private long id;
    @Size(max = Table.MAX_STRING_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String encryptedKey;
    private String label;
    private Reference<KeyType> keyTypeReference = Reference.empty();
    private Instant expirationTime;
    private HsmPropertySetter propertySetter;

    @Inject
    HsmSymmetricKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService,
                        DataModel dataModel, Clock clock, Thesaurus thesaurus, HsmEnergyService hsmEnergyService) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.hsmEnergyService = hsmEnergyService;
    }

    HsmSymmetricKeyImpl init(KeyType keyType, TimeDuration timeDuration, String label) {
        this.keyTypeReference.set(keyType);
        this.setExpirationTime(timeDuration);
        this.label = label;
        return this;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return HsmSymmetricKeyFactory.KEY_ENCRYPTION_METHOD;
    }


    @Override
    public void setKey(byte[] key, String label) {
        this.setKey(key);
        this.setLabel(label);
        this.save();
    }

    private void setKey(byte[] key){
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
        this.encryptedKey = dataVaultService.encrypt(key);
    }

    private void setLabel(String label){
        if (Checks.is(label).emptyOrOnlyWhiteSpace()){
            throw new IllegalArgumentException("Label cannot be empty");
        }
        this.label = label;
    }


    @Override
    public byte[] getKey() {
        if (Checks.is(this.encryptedKey).emptyOrOnlyWhiteSpace()) {
            return new byte[0];
        }
        return dataVaultService.decrypt(this.encryptedKey);
    }

    @Override
    public String getKeyLabel() {
        return label;
    }


    @Override
    public void generateValue(HsmSymmetricKey actualSymmetricKey) {
        try {
            String actualLabel = actualSymmetricKey.getKeyLabel();
            byte[] actualKey = actualSymmetricKey.getKey();
            byte[] hsmGeneratedKey = hsmEnergyService.renewKey(actualKey, actualLabel, this.getKeyLabel()).getEncryptedKey();
            this.setKey(hsmGeneratedKey, label);
        } catch (HsmBaseException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ENCRYPTED_KEY_INVALID, e);
        }
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    private void setExpirationTime(TimeDuration timeDuration) {
        this.expirationTime = ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant();
    }


    @Override
    public void setProperties(Map<String, Object> properties) {
        this.propertySetter = getPropertySetter();
        EnumSet.allOf(HsmProperties.class).forEach(p -> p.copyFromMap(properties, propertySetter));
        Save.UPDATE.validate(dataModel, propertySetter);
        this.setKey(propertySetter.getKey(), propertySetter.getLabel());
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(HsmProperties.class).forEach(p -> p.copyToMap(properties, getPropertySetter()));
        return properties;
    }

    private HsmPropertySetter getPropertySetter(){
        if (this.propertySetter == null){
            this.propertySetter = new HsmPropertySetter(this);
        }
        return this.propertySetter;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return EnumSet.allOf(HsmProperties.class)
                .stream().map(properties -> properties.asPropertySpec(propertySpecService, thesaurus)).collect(toList());
    }

    protected void save() {
        Save.action(id).save(dataModel, this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }
}
