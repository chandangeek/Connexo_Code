/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.energyict.mdc.common.device.config.DeviceType;

import java.util.function.Consumer;

public abstract class AbstractAttachDeviceSAPInfoCPSPostBuilder implements Consumer<DeviceType> {

    private final CustomPropertySetService customPropertySetService;

    public AbstractAttachDeviceSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(DeviceType deviceType) {
        if (deviceType.getCustomPropertySets()
                .stream()
                .filter(rcps -> rcps.getCustomPropertySet() != null)
                .noneMatch(rcps -> getCpsId().equals(rcps.getCustomPropertySet().getId()))) {
            this.customPropertySetService.findActiveCustomPropertySet(getCpsId()).ifPresent(deviceType::addCustomPropertySet);
        }
    }

   protected abstract String getCpsId();
}
