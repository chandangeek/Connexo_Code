package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbarinov on 22.08.2016.
 */
public class IdWithNamePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && HasIdAndName.class.isAssignableFrom(propertySpec.getValueFactory().getValueType());
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
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
        return ((HasIdAndName) domainValue).getId();
    }

}
