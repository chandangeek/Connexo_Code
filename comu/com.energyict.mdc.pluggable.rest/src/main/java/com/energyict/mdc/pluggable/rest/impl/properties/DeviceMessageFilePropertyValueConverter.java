/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.DeviceMessageFileReferenceInfo;
import com.energyict.mdc.protocol.api.DeviceMessageFile;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class DeviceMessageFilePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) { return propertySpec != null && DeviceMessageFile.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()); }


    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.REFERENCE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return new DeviceMessageFileReferenceInfo((DeviceMessageFile) domainValue);
    }

}
