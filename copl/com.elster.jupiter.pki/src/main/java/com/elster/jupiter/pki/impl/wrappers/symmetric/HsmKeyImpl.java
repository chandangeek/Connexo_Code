/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.HsmRenewKey;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;


import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class HsmKeyImpl extends KeyImpl implements HsmKey {


    private final PropertySpecService propertySpecService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final HsmEnergyService hsmEnergyService;
    protected final HsmEncryptionService hsmEncryptionService;


    private String label;
    private String smartMeterKey;
    private HsmJssKeyType hsmJssKeyType;



    @Inject
    HsmKeyImpl(PropertySpecService propertySpecService,
               DataModel dataModel, Clock clock, Thesaurus thesaurus, HsmEnergyService hsmEnergyService, HsmEncryptionService hsmEncryptionService) {
        super(dataModel);
        this.propertySpecService = propertySpecService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.hsmEnergyService = hsmEnergyService;
        this.hsmEncryptionService = hsmEncryptionService;
    }

    HsmKeyImpl init(KeyType keyType, TimeDuration timeDuration, String label, HsmJssKeyType hsmJssKeyType) {
        super.getKeyTypeReference().set(keyType);
        this.calculateExpirationTime(timeDuration);
        this.setLabel(label);
        this.setHsmJssKeyType(hsmJssKeyType);
        return this;
    }

    @Override
    public String getLabel() {
        return label;
    }

    private void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultSymmetricKeyFactory.KEY_ENCRYPTION_METHOD;
    }


    @Override
    public void setKey(byte[] key, String label) {
        validateSetKey(key, label);
        super.setEncryptedKey(Base64.getEncoder().encodeToString(key));
        this.setLabel(label);
        this.save();
    }

    protected void validateSetKey(byte[] key, String label) {
        checkArgument(key, "Key cannot be null");
        checkArgument(label, "Label cannot be null");
        if (label.isEmpty()) {
            throw new IllegalArgumentException("Empty string label not accepted");
        }
    }

    @Override
    public byte[] getKey() {
        if (super.getEncryptedKey()==null) {
            return null;
        }
        return Base64.getDecoder().decode(super.getEncryptedKey());
    }

    @Override
    public void generateValue(SecurityAccessorType securityAccessorType, HsmKey masterKey) {
        try {
            HsmRenewKey hsmRenewKey = hsmEnergyService.renewKey(new RenewKeyRequest(masterKey.getKey(), masterKey.getLabel(), securityAccessorType.getHsmKeyType()));
            this.setKey(hsmRenewKey.getEncryptedKey(), hsmRenewKey.getKeyLabel());
            this.setSmartMeterKey(hsmRenewKey.getSmartMeterKey());
            this.save();
        } catch (HsmBaseException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ENCRYPTED_KEY_INVALID, e);
        }
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        HsmPropertyValidator hsmPropertyValidator = HsmPropertyValidator.build(properties);
        hsmPropertyValidator.validate(getDataModel());
        this.setKey(hsmPropertyValidator.getKey(), hsmPropertyValidator.getLabel());
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        if (getKey() != null){
            properties.put(HsmProperties.DECRYPTED_KEY.getPropertyName(), DatatypeConverter.printHexBinary(getKey()));
        }
        properties.put(HsmProperties.LABEL.getPropertyName(), getLabel());
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return EnumSet.allOf(HsmProperties.class)
                .stream()
                .map(properties -> properties.asPropertySpec(propertySpecService, thesaurus))
                .collect(toList());
    }

    private void calculateExpirationTime(TimeDuration timeDuration) {
        super.setExpirationTime(ZonedDateTime.now(clock).plus(timeDuration.asTemporalAmount()).toInstant());
    }

    protected void checkArgument(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public byte[] getSmartMeterKey() {
        if (smartMeterKey == null || smartMeterKey.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(smartMeterKey);
    }

    public void setSmartMeterKey(byte[] smartMeterKey) {
        this.smartMeterKey = Base64.getEncoder().encodeToString(smartMeterKey);
        this.save();
    }

    public HsmJssKeyType getHsmJssKeyType() {
        return hsmJssKeyType;
    }

    public void setHsmJssKeyType(HsmJssKeyType hsmJssKeyType) {
        this.hsmJssKeyType = hsmJssKeyType;
    }

}
