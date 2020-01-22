/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.energyict.mdc.common.device.config.DeviceType;

import javax.inject.Inject;
import java.util.function.Consumer;

public class AttachDeviceRegisterSAPInfoCPSPostBuilder implements Consumer<DeviceType> {

    public static final String CPS_ID = "com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoCustomPropertySet";
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public AttachDeviceRegisterSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(DeviceType deviceType) {
        deviceType.getRegisterTypes().stream()
                .filter(rt -> !deviceType.getRegisterTypeTypeCustomPropertySet(rt).isPresent())
                .forEach(rt -> this.customPropertySetService.findActiveCustomPropertySet(CPS_ID)
                        .ifPresent(rcps -> deviceType.addRegisterTypeCustomPropertySet(rt, rcps)));
    }
}
