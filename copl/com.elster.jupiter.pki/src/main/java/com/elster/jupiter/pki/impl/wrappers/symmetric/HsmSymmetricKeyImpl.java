/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
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
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class HsmSymmetricKeyImpl extends KeyImpl implements HsmSymmetricKey{

    private final DataVaultService dataVaultService;
    private final PropertySpecService propertySpecService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final HsmEnergyService hsmEnergyService;


    private HsmPropertySetter propertySetter;

    @Inject
    HsmSymmetricKeyImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService,
                        DataModel dataModel, Clock clock, Thesaurus thesaurus, HsmEnergyService hsmEnergyService) {
        super(dataModel);
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.hsmEnergyService = hsmEnergyService;
    }

    HsmSymmetricKeyImpl init(KeyType keyType, TimeDuration timeDuration, String label) {
        super.getKeyTypeReference().set(keyType);
        this.setExpirationTime(timeDuration);
        super.setLabel(label);
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
        super.setEncryptedKey(dataVaultService.encrypt(key));
    }

    @Override
    public void setLabel(String label){
        if (Checks.is(label).emptyOrOnlyWhiteSpace()){
            throw new IllegalArgumentException("Label cannot be empty");
        }
        super.setLabel(label);
    }

    @Override
    public String getKeyLabel() {
        return super.getLabel();
    }

    @Override
    public byte[] getKey() {
        if (Checks.is(super.getEncryptedKey()).emptyOrOnlyWhiteSpace()) {
            return new byte[0];
        }
        return dataVaultService.decrypt(super.getEncryptedKey());
    }

    @Override
    public void generateValue(HsmSymmetricKey actualSymmetricKey) {
        try {
            String actualLabel = actualSymmetricKey.getKeyLabel();
            byte[] actualKey = actualSymmetricKey.getKey();
            byte[] hsmGeneratedKey = hsmEnergyService.renewKey(new RenewKeyRequest(actualKey, actualLabel, getLabel())).getEncryptedKey();
            this.setKey(hsmGeneratedKey, super.getLabel());
            this.save();
        } catch (HsmBaseException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ENCRYPTED_KEY_INVALID, e);
        }
    }



    private void setExpirationTime(TimeDuration timeDuration) {
        super.setExpirationTime(ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant());
    }


    @Override
    public void setProperties(Map<String, Object> properties) {
        this.propertySetter = getPropertySetter();
        EnumSet.allOf(HsmProperties.class).forEach(p -> p.copyFromMap(properties, propertySetter));
        Save.UPDATE.validate(super.getDataModel(), propertySetter);
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


}
