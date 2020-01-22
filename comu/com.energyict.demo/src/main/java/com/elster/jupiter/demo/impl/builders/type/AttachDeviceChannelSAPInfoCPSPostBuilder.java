/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.energyict.mdc.common.device.config.DeviceType;

import javax.inject.Inject;
import java.util.function.Consumer;

public class AttachDeviceChannelSAPInfoCPSPostBuilder implements Consumer<DeviceType> {

    public static final String CPS_ID = "com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoCustomPropertySet";
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public AttachDeviceChannelSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(DeviceType deviceType) {
        deviceType.getLoadProfileTypes().stream()
                .filter(lpt -> !deviceType.getLoadProfileTypeCustomPropertySet(lpt).isPresent())
                .forEach(lpt -> this.customPropertySetService.findActiveCustomPropertySet(CPS_ID)
                        .ifPresent(rcps -> deviceType.addLoadProfileTypeCustomPropertySet(lpt, rcps)));
    }
}
