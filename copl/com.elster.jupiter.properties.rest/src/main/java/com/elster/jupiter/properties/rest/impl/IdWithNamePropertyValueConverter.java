/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.AssignPropertyFactory;
import com.elster.jupiter.properties.rest.ProcessPropertyFactory;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.RaiseEventPropertyFactory;
import com.elster.jupiter.properties.rest.RelativePeriodWithCountPropertyFactory;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mbarinov on 22.08.2016.
 */
public class IdWithNamePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && (HasIdAndName.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())
                || (HasId.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()) && HasName.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())));
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        if (propertySpec.getValueFactory() instanceof AssignPropertyFactory) {
            return SimplePropertyType.ASSIGN;
        }
        if (propertySpec.getValueFactory() instanceof RaiseEventPropertyFactory) {
            return SimplePropertyType.RAISEEVENTPROPS;
        }
        if (propertySpec.getValueFactory() instanceof RelativePeriodWithCountPropertyFactory) {
            return SimplePropertyType.RELATIVEPERIODWITHCOUNT;
        }
        if (propertySpec.getValueFactory() instanceof ProcessPropertyFactory) {
            return SimplePropertyType.BPM_PROCESS;
        }
        return SimplePropertyType.IDWITHNAME;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        List<HasName> listValue = new ArrayList<>();
        if (infoValue instanceof List) {
            List<?> list = (List<?>) infoValue;
            for (Object listItem : list) {
                listValue.add((HasName) propertySpec.getValueFactory().fromStringValue((String) listItem));
            }
            return listValue;
        } else if (infoValue != null) {
            return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
        }
        return null;
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null) {
            if (domainValue instanceof HasIdAndName){
                return ((HasIdAndName) domainValue).getId();
            } else if (domainValue instanceof HasId && domainValue instanceof HasName) {
                return this.asInfo(((HasId) domainValue).getId(), ((HasName) domainValue).getName());
            } else {
                return ((HasId) domainValue).getId();
            }
        }
        return null;
    }

    private Object asInfo(long id, String name) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", id);
        info.put("name", name);
        return info;
    }

}