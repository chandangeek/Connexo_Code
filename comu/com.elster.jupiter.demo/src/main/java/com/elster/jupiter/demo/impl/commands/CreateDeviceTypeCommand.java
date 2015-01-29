package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.pp.ConnectionMethodForDeviceConfiguration;
import com.elster.jupiter.demo.impl.pp.ProtocolPropertiesMPP;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.ChannelType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class CreateDeviceTypeCommand {
    private final Provider<ConnectionMethodForDeviceConfiguration> connectionMethodsProvider;

    private String deviceTypeName;
    private String host;

    @Inject
    public CreateDeviceTypeCommand(Provider<ConnectionMethodForDeviceConfiguration> connectionMethodsProvider) {
        this.connectionMethodsProvider = connectionMethodsProvider;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void run(){
        DeviceType deviceType = Builders.from(DeviceTypeTpl.Elster_AS1440).withName(this.deviceTypeName).get();
        DeviceConfiguration configuration = Builders.from(DeviceConfigurationTpl.DEFAULT).withDeviceType(deviceType)
                .withPropertyProviders(Arrays.asList(
                        this.connectionMethodsProvider.get().withHost(this.host),
                        new ProtocolPropertiesMPP()
                )).get();
        addChannelsToDeviceConfiguration(configuration);
        configuration.activate();
    }

    private void addChannelsToDeviceConfiguration(DeviceConfiguration configuration) {
        for (LoadProfileSpec loadProfileSpec : configuration.getLoadProfileSpecs()) {
            List<ChannelType> availableChannelTypes = loadProfileSpec.getLoadProfileType().getChannelTypes();
            for (ChannelType channelType : availableChannelTypes) {
                configuration.createChannelSpec(channelType, channelType.getPhenomenon(), loadProfileSpec).setMultiplier(new BigDecimal(1)).setOverflow(new BigDecimal(9999999999L)).setNbrOfFractionDigits(0).add();
            }
        }
    }
}
