/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import java.util.LinkedHashMap;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class TimeDurationPropertyValueConverter implements PropertyValueConverter {
    private  Thesaurus thesaurus;

    public TimeDurationPropertyValueConverter(){
    }

    public TimeDurationPropertyValueConverter(Thesaurus thesaurus){
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof TimeDurationValueFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TIMEDURATION;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        Integer count = (Integer) ((LinkedHashMap<String, Object>) infoValue).get("count");
        String timeUnit = (String) ((LinkedHashMap<String, Object>) infoValue).get("timeUnit");
        try {
            return new TimeDuration("" + count + " " + timeUnit);
        }
        catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), propertySpec.getName() + "." + e.getViolatingProperty(), e.getArgs());
        }
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return new TimeDurationInfo((TimeDuration) domainValue, thesaurus);
    }

}
