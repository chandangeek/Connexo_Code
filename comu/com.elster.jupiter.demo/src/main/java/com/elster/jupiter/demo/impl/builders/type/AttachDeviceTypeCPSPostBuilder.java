package com.elster.jupiter.demo.impl.builders.type;

import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Consumer;

public class AttachDeviceTypeCPSPostBuilder implements Consumer<DeviceType> {
    private final Provider<AttachEMeterInfoCPSPostBuilder> eMeterCpsInfoProvider;
    private final Provider<AttachDeviceSAPInfoCPSPostBuilder> deviceSapCpsInfoProvider;
    private final Provider<AttachChannelSAPInfoCPSPostBuilder> channelSapCpsInfoProvider;

    @Inject
    public AttachDeviceTypeCPSPostBuilder(Provider<AttachEMeterInfoCPSPostBuilder> eMeterCpsInfoProvider, Provider<AttachDeviceSAPInfoCPSPostBuilder> deviceSapCpsInfoProvider, Provider<AttachChannelSAPInfoCPSPostBuilder> channelSapCpsInfoProvider) {
        this.eMeterCpsInfoProvider = eMeterCpsInfoProvider;
        this.deviceSapCpsInfoProvider = deviceSapCpsInfoProvider;
        this.channelSapCpsInfoProvider = channelSapCpsInfoProvider;
    }

    @Override
    public void accept(DeviceType deviceType) {
        this.eMeterCpsInfoProvider.get().accept(deviceType);
        this.deviceSapCpsInfoProvider.get().accept(deviceType);
        this.channelSapCpsInfoProvider.get().accept(deviceType);
    }
}
