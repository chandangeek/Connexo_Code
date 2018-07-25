/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmSymmetricKey;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class HsmKeyImpl extends KeyImpl implements HsmSymmetricKey{

    private final DataVaultService dataVaultService;
    private final PropertySpecService propertySpecService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final HsmEnergyService hsmEnergyService;

    @Inject
    HsmKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService,
               DataModel dataModel, Clock clock, Thesaurus thesaurus, HsmEnergyService hsmEnergyService) {
        super(dataModel);
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.hsmEnergyService = hsmEnergyService;
    }

    HsmKeyImpl init(KeyType keyType, TimeDuration timeDuration, String label) {
        super.getKeyTypeReference().set(keyType);
        this.calculateExpirationTime(timeDuration);
        this.setLabel(label);
        return this;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return HsmKeyFactory.KEY_ENCRYPTION_METHOD;
    }


    @Override
    public void setKey(byte[] key, String label) {
        if (key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }
        super.setEncryptedKey(dataVaultService.encrypt(key));
        super.setLabel(label);
        this.save();
    }

    @Override
    public byte[] getKey() {
        String key = super.getEncryptedKey();
        if (key == null){
            return new byte[0];
        }
        return dataVaultService.decrypt(super.getEncryptedKey());
    }

    @Override
    public String getLabel() {
        return super.getLabel();
    }

    @Override
    public void generateValue(HsmSymmetricKey actualSymmetricKey) {
        try {
            String actualLabel = actualSymmetricKey.getLabel();
            byte[] actualKey = actualSymmetricKey.getKey();
            byte[] hsmGeneratedKey = hsmEnergyService.renewKey(new RenewKeyRequest(actualKey, actualLabel, getLabel())).getEncryptedKey();
            this.setKey(hsmGeneratedKey, getLabel());
            this.save();
        } catch (HsmBaseException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ENCRYPTED_KEY_INVALID, e);
        }
    }

    private void calculateExpirationTime(TimeDuration timeDuration) {
        super.setExpirationTime(ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant());
    }


    @Override
    // ToDO: Implement method when requirements are clear
    public void setProperties(Map<String, Object> properties) {
        /*HsmPropertySetter propertySetter = new HsmPropertySetter(this);
        EnumSet.allOf(HsmProperties.class).forEach(p -> p.copyFromMap(properties, propertySetter));
        Save.UPDATE.validate(super.getDataModel(), propertySetter);
        this.setKey(propertySetter.getKey(), propertySetter.getLabel());*/
    }

    @Override
    public Map<String, Object> getProperties() {
        PropertySetter propertySetter = new HsmPropertySetter(this);
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(HsmProperties.class).forEach(p -> p.copyToMap(properties, propertySetter));
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return EnumSet.allOf(HsmProperties.class)
                .stream()
                .map(properties -> properties.asPropertySpec(propertySpecService, thesaurus))
                .collect(toList());
    }


}
