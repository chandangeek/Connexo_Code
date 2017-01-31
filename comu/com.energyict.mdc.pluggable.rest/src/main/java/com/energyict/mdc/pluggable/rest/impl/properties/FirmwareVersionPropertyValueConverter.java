/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class FirmwareVersionPropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && BaseFirmwareVersion.class.isAssignableFrom(propertySpec.getValueFactory().getValueType());
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.FIRMWAREVERSION;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        FirmwareVersion firmwareVersion = (FirmwareVersion) domainValue;
        return new IdWithNameInfo(firmwareVersion.getId(), firmwareVersion.getFirmwareVersion());
    }

}
