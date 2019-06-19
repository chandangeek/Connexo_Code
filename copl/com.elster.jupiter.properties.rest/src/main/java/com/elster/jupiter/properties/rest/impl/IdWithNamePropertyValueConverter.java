/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.MailPropertyFactory;
import com.elster.jupiter.properties.rest.AssignPropertyFactory;
import com.elster.jupiter.properties.rest.ProcessPropertyFactory;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.RaiseEventPropertyFactory;
import com.elster.jupiter.properties.rest.RaiseEventUrgencyFactory;
import com.elster.jupiter.properties.rest.RecurrenceSelectionPropertyFactory;
import com.elster.jupiter.properties.rest.RelativePeriodWithCountPropertyFactory;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.properties.rest.WebServicesEndPointFactory;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.ArrayList;
import java.util.HashMap;
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
        if (propertySpec.getValueFactory() instanceof MailPropertyFactory) {
            return SimplePropertyType.MAILTO;
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
        if (propertySpec.getValueFactory() instanceof WebServicesEndPointFactory) {
            return SimplePropertyType.WEB_SERVICES_ENDPOINT;
        }
        if (propertySpec.getValueFactory() instanceof RaiseEventUrgencyFactory) {
            return SimplePropertyType.RADIO_GROUP;
        }
        if (propertySpec.getValueFactory() instanceof RecurrenceSelectionPropertyFactory) {
            return SimplePropertyType.RECURRENCE_SELECTION_PROPS;
        }
        return SimplePropertyType.IDWITHNAME;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue instanceof Map) {
            return propertySpec.getValueFactory().fromStringValue((((Map)infoValue).get("id").toString()));
        } else if (infoValue instanceof List) {
            List<HasName> listValue = new ArrayList<>();
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
        if(domainValue!=null){
            if (domainValue instanceof HasIdAndName){
                return ((HasIdAndName) domainValue).getId();
            } else if (HasId.class.isAssignableFrom(domainValue.getClass())) {
                if (HasName.class.isAssignableFrom(domainValue.getClass())) {
                    return asIdWithNameMap(domainValue);
                } else {
                    return ((HasId) domainValue).getId();
                }
            }
        }
        return null;
    }

    private Object asIdWithNameMap(Object domainValue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", ((HasId) domainValue).getId());
        map.put("name", ((HasName) domainValue).getName());
        return map;
    }

}
