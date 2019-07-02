/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListReadingQualityFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.BpmProcessPropertyFactory;
import com.elster.jupiter.properties.rest.DeviceConfigurationPropertyFactory;
import com.elster.jupiter.properties.rest.DeviceGroupPropertyFactory;
import com.elster.jupiter.properties.rest.DeviceLifeCycleInDeviceTypePropertyFactory;
import com.elster.jupiter.properties.rest.DeviceLifeCycleTransitionPropertyFactory;
import com.elster.jupiter.properties.rest.EndDeviceEventTypePropertyFactory;
import com.elster.jupiter.properties.rest.MetrologyConfigurationPropertyFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.properties.rest.TaskPropertyFacory;
import com.elster.jupiter.util.HasId;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mbarinov on 24.08.2016.
 */
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
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof MetrologyConfigurationPropertyFactory) {
            return SimplePropertyType.METROLOGYCONFIGURATIONLIST;
        }
        if (propertySpec.getValueFactory() instanceof ListReadingQualityFactory) {
            return SimplePropertyType.LISTREADINGQUALITY;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof DeviceLifeCycleInDeviceTypePropertyFactory) {
            return SimplePropertyType.LIFECYCLESTATUSINDEVICETYPE;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof DeviceLifeCycleTransitionPropertyFactory) {
            return SimplePropertyType.LIFECYCLETRANSITION;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof TaskPropertyFacory) {
            return SimplePropertyType.TASK;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof DeviceGroupPropertyFactory) {
            return SimplePropertyType.DEVICEGROUPTYPE;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory() instanceof EndDeviceEventTypePropertyFactory) {
            return SimplePropertyType.ENDDEVICEEVENTTYPE;
        }
        if (((ListValueFactory) propertySpec.getValueFactory()).getActualFactory().isReference()) {
            return SimplePropertyType.IDWITHNAMELIST;
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
            List<?> value = (List<?>) domainValue;
            return value.stream().map(ListPropertyValueConverter::retrieveId).collect(Collectors.toList());
        }
        return null;
    }

    private static Object retrieveId(Object value) {
        return value instanceof HasIdAndName ? ((HasIdAndName) value).getId() : value instanceof HasId ? ((HasId) value).getId() : value;
    }
}
