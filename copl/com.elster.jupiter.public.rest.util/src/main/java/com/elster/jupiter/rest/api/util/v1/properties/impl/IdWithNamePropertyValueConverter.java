/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.properties.AssignPropertyFactory;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyType;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueConverter;
import com.elster.jupiter.rest.api.util.v1.properties.RaiseEventPropertyFactory;
import com.elster.jupiter.rest.api.util.v1.properties.SimplePropertyType;

import java.util.ArrayList;
import java.util.List;

public class IdWithNamePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && HasIdAndName.class.isAssignableFrom(propertySpec.getValueFactory().getValueType());
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        if (propertySpec.getValueFactory() instanceof AssignPropertyFactory) {
            return SimplePropertyType.ASSIGN;
        }
        if (propertySpec.getValueFactory() instanceof RaiseEventPropertyFactory) {
            return SimplePropertyType.RAISEEVENTPROPS;
        }

        return SimplePropertyType.IDWITHNAME;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        List<HasIdAndName> listValue = new ArrayList<>();
        if (infoValue instanceof List) {
            List<?> list = (List<?>) infoValue;
            for (Object listItem : list) {
                listValue.add((HasIdAndName) propertySpec.getValueFactory().fromStringValue((String) listItem));
            }
            return listValue;
        } else if (infoValue != null) {
            return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return domainValue != null ? ((HasIdAndName) domainValue).getId() : null;
    }

}
