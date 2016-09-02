package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.dynamic.DateAndTimeFactory;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class ClockPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof DateAndTimeFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.CLOCK;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return domainValue;
    }

}
