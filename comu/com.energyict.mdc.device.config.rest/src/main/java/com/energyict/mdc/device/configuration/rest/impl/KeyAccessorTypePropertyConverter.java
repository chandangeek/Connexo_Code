/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 2/2/17.
 */
@Component(name="KeyAccessorTypePropertyConverter", service = PropertyValueConverter.class, immediate = true)
public class KeyAccessorTypePropertyConverter implements PropertyValueConverter {
    private DeviceConfigurationService deviceConfigurationService;
    private PropertyValueInfoService propertyValueInfoService;

    public KeyAccessorTypePropertyConverter() {
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Activate
    public void activate() {
        propertyValueInfoService.addPropertyValueInfoConverter(this);
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return (propertySpec.isReference() && KeyAccessorType.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()));
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.IDWITHNAME;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        long id;
        if (Long.class.isAssignableFrom(infoValue.getClass())) { // I love Jackson
            id = (Long)infoValue;
        } else {
            id = (Integer)infoValue;
        }
        return deviceConfigurationService.findKeyAccessorTypeById(id).orElse(null);
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        KeyAccessorType keyAccessorType = (KeyAccessorType) domainValue;
        return new LongIdWithNameInfo(keyAccessorType.getId(), keyAccessorType.getName());
    }
}
