package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.rest.util.IdWithNameInfo;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class UsagePointPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && UsagePoint.class.isAssignableFrom(propertySpec.getValueFactory().getValueType());
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.USAGEPOINT;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        UsagePoint usagePoint = (UsagePoint) domainValue;
        return new IdWithNameInfo(usagePoint.getId(), usagePoint.getName());
    }

}
