/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.type;

import com.energyict.mdc.common.device.config.DeviceType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Consumer;

public class AttachDeviceTypeCPSPostBuilder implements Consumer<DeviceType> {
    private final Provider<AttachEMeterInfoCPSPostBuilder> eMeterCpsInfoProvider;
    private final Provider<AttachDeviceSAPInfoCPSPostBuilder> deviceSapCpsInfoProvider;
    private final Provider<AttachDeviceRegisterSAPInfoCPSPostBuilder> deviceRegisterSAPInfoCPSPostBuilderProvider;
    private final Provider<AttachDeviceChannelSAPInfoCPSPostBuilder> channelSAPInfoCPSPostBuilderProvider;

    @Inject
    public AttachDeviceTypeCPSPostBuilder(Provider<AttachEMeterInfoCPSPostBuilder> eMeterCpsInfoProvider,
                                          Provider<AttachDeviceSAPInfoCPSPostBuilder> deviceSapCpsInfoProvider,
                                          Provider<AttachDeviceRegisterSAPInfoCPSPostBuilder> deviceRegisterSAPInfoCPSPostBuilderProvider,
                                          Provider<AttachDeviceChannelSAPInfoCPSPostBuilder> channelSAPInfoCPSPostBuilderProvider) {
        this.eMeterCpsInfoProvider = eMeterCpsInfoProvider;
        this.deviceSapCpsInfoProvider = deviceSapCpsInfoProvider;
        this.channelSAPInfoCPSPostBuilderProvider = channelSAPInfoCPSPostBuilderProvider;
        this.deviceRegisterSAPInfoCPSPostBuilderProvider = deviceRegisterSAPInfoCPSPostBuilderProvider;
    }

    @Override
    public void accept(DeviceType deviceType) {
        this.eMeterCpsInfoProvider.get().accept(deviceType);
        this.deviceSapCpsInfoProvider.get().accept(deviceType);
        this.channelSAPInfoCPSPostBuilderProvider.get().accept(deviceType);
        this.deviceRegisterSAPInfoCPSPostBuilderProvider.get().accept(deviceType);
    }
}
