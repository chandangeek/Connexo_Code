/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.pluggable.rest.impl.LoadProfileInfo;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class LoadProfilePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
      //  return propertySpec != null && LoadProfile.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()); // not OK
      //  return propertySpec!= null && propertySpec.getValueFactory().getValueType().isAssignableFrom(LoadProfile.class); // Lau - ok 1
        return propertySpec!= null && com.energyict.mdc.upl.meterdata.LoadProfile.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()); // Lau ok 2
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.LOADPROFILE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return new LoadProfileInfo((LoadProfile) domainValue);
    }
}