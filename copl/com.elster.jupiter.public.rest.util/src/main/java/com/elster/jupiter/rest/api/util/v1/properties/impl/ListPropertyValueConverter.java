/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListReadingQualityFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.properties.BpmProcessPropertyFactory;
import com.elster.jupiter.rest.api.util.v1.properties.DeviceConfigurationPropertyFactory;
import com.elster.jupiter.rest.api.util.v1.properties.DeviceLifeCycleInDeviceTypePropertyFactory;
import com.elster.jupiter.rest.api.util.v1.properties.EndDeviceEventTypePropertyFactory;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueConverter;
import com.elster.jupiter.rest.api.util.v1.properties.SimplePropertyType;

import java.util.List;
import java.util.stream.Collectors;

public class ListPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && (propertySpec.getValueFactory() instanceof ListValueFactory || propertySpec.getValueFactory() instanceof ListReadingQualityFactory);
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof BpmProcessPropertyFactory) {
            return SimplePropertyType.SELECTIONGRID;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof DeviceConfigurationPropertyFactory) {
            return SimplePropertyType.DEVICECONFIGURATIONLIST;
        }
        if (propertySpec.getValueFactory() instanceof ListReadingQualityFactory) {
            return SimplePropertyType.LISTREADINGQUALITY;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof DeviceLifeCycleInDeviceTypePropertyFactory) {
            return SimplePropertyType.LIFECYCLESTATUSINDEVICETYPE;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof EndDeviceEventTypePropertyFactory) {
            return SimplePropertyType.ENDDEVICEEVENTTYPE;
        }
        return SimplePropertyType.LISTVALUE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (propertySpec.getValueFactory() instanceof ListValueFactory && infoValue instanceof List) {
            List<Object> valueList = (List<Object>) infoValue;
            ListValueFactory listValueFactory = (ListValueFactory) propertySpec.getValueFactory();
            return listValueFactory.fromValues(valueList);
        }
        return propertySpec.getValueFactory().fromStringValue((String) infoValue);
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if (domainValue != null) {
            List<HasIdAndName> value = (List<HasIdAndName>) domainValue;
            return value.stream().map(HasIdAndName::getId).collect(Collectors.toList());
        }
        return null;
    }

}
