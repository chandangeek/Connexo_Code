/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.configuration;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.InboundComPortPoolTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

public class InboundTCPConnectionMethodsDevConfPostBuilder implements Consumer<DeviceConfiguration> {

    private final ProtocolPluggableService protocolPluggableService;
    private String name = "Outbound TCP";
    private ConnectionFunction connectionFunction;
    private String protocolDialectName = "tcp";
    private InboundComPortPoolTpl comPortPool = InboundComPortPoolTpl.INBOUND_SERVLET_BEACON_PSK;
    private boolean isDefault = true;

    @Inject
    public InboundTCPConnectionMethodsDevConfPostBuilder(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    public InboundTCPConnectionMethodsDevConfPostBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public InboundTCPConnectionMethodsDevConfPostBuilder withConnectionFunction(ConnectionFunction connectionFunction) {
        this.connectionFunction = connectionFunction;
        return this;
    }

    public InboundTCPConnectionMethodsDevConfPostBuilder withProtocolDialectName(String protocolDialectName) {
        this.protocolDialectName = protocolDialectName;
        return this;
    }

    public InboundTCPConnectionMethodsDevConfPostBuilder withComPortPool(InboundComPortPoolTpl comPortPool) {
        this.comPortPool = comPortPool;
        return this;
    }

    public InboundTCPConnectionMethodsDevConfPostBuilder withDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    @Override
    public void accept(DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByNameTranslationKey("InboundIpConnectionType").get();
        final PartialInboundConnectionTaskBuilder builder = configuration
                .newPartialInboundConnectionTask(name, pluggableClass, getProtocolDialectConfigurationProperties(configuration))
                .comPortPool(Builders.from(comPortPool).get())
                .connectionFunction(connectionFunction)
                .asDefault(isDefault);
        builder.build();
    }

    private ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties(DeviceConfiguration configuration) {
        Optional<ProtocolDialectConfigurationProperties> tcpDialect = configuration.getProtocolDialectConfigurationPropertiesList()
                .stream()
                .filter(protocolDialectConfigurationProperties ->
                        protocolDialectConfigurationProperties.getDeviceProtocolDialectName().toLowerCase().contains(protocolDialectName.toLowerCase()))
                .findFirst();
        return tcpDialect.orElse(configuration.getProtocolDialectConfigurationPropertiesList().get(0));
    }

}
