package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListReadingQualityFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.BpmProcessPropertyFactory;
import com.elster.jupiter.properties.rest.DeviceConfigurationPropertyFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.HasName;

import java.util.ArrayList;
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
        if (propertySpec.getValueFactory() instanceof ListReadingQualityFactory) {
            return SimplePropertyType.LISTREADINGQUALITY;
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
            List<Object> nameList = ((List<Object>) domainValue).stream().filter(value -> (value instanceof HasName) && !(value instanceof HasIdAndName)).map(value -> ((HasName) value).getName()).collect(Collectors.toList());
            List<Object> idAndnameList = ((List<Object>) domainValue).stream().filter(value -> value instanceof HasIdAndName).map(value -> ((HasIdAndName) value).getId()).collect(Collectors.toList());
            idAndnameList.addAll(nameList);
            return idAndnameList;
        }
        return null;
    }

}
