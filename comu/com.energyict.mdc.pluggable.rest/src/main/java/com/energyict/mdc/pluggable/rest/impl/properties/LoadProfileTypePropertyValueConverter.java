/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.pluggable.rest.impl.LoadProfileTypeInfo;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class LoadProfileTypePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && LoadProfileType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType());
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.LOADPROFILETYPE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return new LoadProfileTypeInfo((LoadProfileType) domainValue);
    }

}
