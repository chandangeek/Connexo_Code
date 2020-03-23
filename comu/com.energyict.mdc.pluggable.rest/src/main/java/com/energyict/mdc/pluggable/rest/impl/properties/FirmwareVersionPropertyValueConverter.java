/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

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
        return new FirmwareVersionInfo((FirmwareVersion) domainValue);
    }

    private class FirmwareVersionInfo{
        public Object id;
        public String name;
        public String imageIdentifier;

        FirmwareVersionInfo(FirmwareVersion firmwareVersion){
            this.id = firmwareVersion.getId();
            this.name = firmwareVersion.getFirmwareVersion();
            this.imageIdentifier = firmwareVersion.getImageIdentifier();
        }
    }

}
