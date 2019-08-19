/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.energyict.mdc.common.device.config.DeviceType;

import javax.inject.Inject;
import java.util.function.Consumer;

public class AttachEMeterInfoCPSPostBuilder implements Consumer<DeviceType> {
    public static final String CPS_ID = "com.energyict.mdc.device.config.cps.DeviceEMeterInfoCustomPropertySet";

    private final CustomPropertySetService customPropertySetService;

    @Inject
    public AttachEMeterInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(DeviceType deviceType) {
        if (deviceType.getCustomPropertySets()
                .stream()
                .filter(rcps -> rcps.getCustomPropertySet() != null)
                .noneMatch(rcps -> CPS_ID.equals(rcps.getCustomPropertySet().getId()))) {
            this.customPropertySetService.findActiveCustomPropertySet(CPS_ID).ifPresent(deviceType::addCustomPropertySet);
        }
    }
}
