/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.NoneOrTimeDurationValue;
import com.elster.jupiter.properties.NoneOrTimeDurationValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.time.TimeDuration;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class NoneOrTimeDurationPropertyValueConverter implements PropertyValueConverter {

    private final TimeDurationPropertyValueConverter timeDurationPropertyValueConverter;

    public NoneOrTimeDurationPropertyValueConverter(Thesaurus thesaurus) {
        this.timeDurationPropertyValueConverter = new TimeDurationPropertyValueConverter(thesaurus);
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof NoneOrTimeDurationValueFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.NONE_OR_TIMEDURATION;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && infoValue instanceof Map) {
            Object isNone = ((Map) infoValue).get("isNone");
            if (isNone != null && (Boolean) isNone) {
                return NoneOrTimeDurationValue.none();
            } else {
                return NoneOrTimeDurationValue.of((TimeDuration) this.timeDurationPropertyValueConverter.convertInfoToValue(propertySpec, infoValue));
            }
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null && domainValue instanceof NoneOrTimeDurationValue) {
            NoneOrTimeDurationValue value = (NoneOrTimeDurationValue) domainValue;
            if (value.isNone()) {
                return ImmutableMap.of("isNone", true);
            } else {
                return timeDurationPropertyValueConverter.convertValueToInfo(propertySpec, value.getValue());
            }
        }
        return null;
    }
}
