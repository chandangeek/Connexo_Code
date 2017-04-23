/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.TemporalAmountValueFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;

import java.util.LinkedHashMap;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class TemporalAmountPropertyValueConverter implements PropertyValueConverter {

    private final Thesaurus thesaurus;

    public TemporalAmountPropertyValueConverter(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof TemporalAmountValueFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TEMPORALAMOUNT;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        Integer count = (Integer) ((LinkedHashMap<String, Object>) infoValue).get("count");
        String timeUnit = (String) ((LinkedHashMap<String, Object>) infoValue).get("timeUnit");
        try {
            return new TimeDuration("" + count + " " + timeUnit);
        } catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), propertySpec.getName() + "." + e.getViolatingProperty(), e.getArgs());
        }
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return new TimeDurationInfo((TimeDuration) domainValue, thesaurus);
    }

}