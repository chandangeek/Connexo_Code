/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.configuration;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class OutboundTCPConnectionMethodsDevConfPostBuilder implements Consumer<DeviceConfiguration> {

    public static final BigDecimal DEFAULT_PORT_NUMBER = new BigDecimal(4059);
    public static final int DEFAULT_RETRY_DELAY_MINUTUES = 60;

    private final ProtocolPluggableService protocolPluggableService;
    private int retryDelayInMinutes = DEFAULT_RETRY_DELAY_MINUTUES;
    private Map<String, Object> properties = new HashMap<>();
    private String name = "Outbound TCP";
    private ConnectionFunction connectionFunction;
    private String protocolDialectName = "tcp";
    private OutboundTCPComPortPoolTpl comPortPool = OutboundTCPComPortPoolTpl.ORANGE;
    private boolean isDefault = true;

    @Inject
    public OutboundTCPConnectionMethodsDevConfPostBuilder(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withHost(String host){
        properties.put("host", host);
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withConnectionFunction(ConnectionFunction connectionFunction) {
        this.connectionFunction = connectionFunction;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withRetryDelay(int retryDelayInMinutes) {
        this.retryDelayInMinutes =  retryDelayInMinutes;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withProtocolDialectName(String protocolDialectName) {
        this.protocolDialectName = protocolDialectName;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withComPortPool(OutboundTCPComPortPoolTpl comPortPool) {
        this.comPortPool = comPortPool;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withDefault(boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withDefaultOutboundTcpProperties(){
        this.properties.put("portNumber", DEFAULT_PORT_NUMBER);
        this.properties.put("connectionTimeout", TimeDuration.minutes(1));
        return this;
    }

    @Override
    public void accept(DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByNameTranslationKey("OutboundTcpIpConnectionType").get();
        final PartialScheduledConnectionTaskBuilder builder = configuration
                .newPartialScheduledConnectionTask(name, pluggableClass, new TimeDuration(retryDelayInMinutes, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE, getProtocolDialectConfigurationProperties(configuration))
                .comPortPool(Builders.from(comPortPool).get())
                .connectionFunction(connectionFunction)
                .setNumberOfSimultaneousConnections(1)
                .asDefault(isDefault);
        this.properties.entrySet().stream().forEach(x-> addProperty(builder, x));
        builder.build();
    }

    private void addProperty(PartialScheduledConnectionTaskBuilder builder, Map.Entry<String,Object> entry){
        builder.addProperty(entry.getKey(), entry.getValue());
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
