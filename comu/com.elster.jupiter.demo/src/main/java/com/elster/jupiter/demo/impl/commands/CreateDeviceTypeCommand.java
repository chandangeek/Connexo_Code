package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.configuration.ChannelsOnDevConfPostBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.OutboundTCPConnectionMethodsDevConfPostBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import javax.inject.Provider;

public class CreateDeviceTypeCommand {
    private final Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider;

    private String deviceTypeName;
    private String host;

    @Inject
    public CreateDeviceTypeCommand(Provider<OutboundTCPConnectionMethodsDevConfPostBuilder> connectionMethodsProvider) {
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
        DeviceConfiguration configuration = Builders.from(DeviceConfigurationTpl.PROSUMERS).withDeviceType(deviceType)
                .withPostBuilder(this.connectionMethodsProvider.get().withHost(this.host).withDefaultOutboundTcpProperties())
                .withPostBuilder(new ChannelsOnDevConfPostBuilder())
                .get();
        configuration.activate();
    }
}
