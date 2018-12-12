/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.demo.impl.templates.LoadProfileTypeTpl;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AttachChannelSAPInfoCPSPostBuilder implements Consumer<DeviceType> {
    public static final String CPS_ID = "com.energyict.mdc.device.config.cps.ChannelSAPInfoCustomPropertySet";

    private final CustomPropertySetService customPropertySetService;
    private List<String> loadProfileTypes = Arrays.asList(LoadProfileTypeTpl._15_MIN_ELECTRICITY.getName(),
            LoadProfileTypeTpl.DAILY_ELECTRICITY.getName(),
            LoadProfileTypeTpl.MONTHLY_ELECTRICITY.getName(),
            LoadProfileTypeTpl._15_MIN_ELECTRICITY_A_PLUS.getName(),
            LoadProfileTypeTpl.DAILY_ELECTRICITY_A_PLUS.getName(),
            LoadProfileTypeTpl.MONTHLY_ELECTRICITY_A_PLUS.getName());

    @Inject
    public AttachChannelSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public void accept(DeviceType deviceType) {
        deviceType.getLoadProfileTypes().stream()
                .filter(lpt -> this.loadProfileTypes.contains(lpt.getName()))
                .filter(lpt -> !deviceType.getLoadProfileTypeCustomPropertySet(lpt).isPresent())
                .forEach(lpt -> this.customPropertySetService.findActiveCustomPropertySet(CPS_ID)
                        .ifPresent(rcps -> deviceType.addLoadProfileTypeCustomPropertySet(lpt, rcps)));
    }
}
