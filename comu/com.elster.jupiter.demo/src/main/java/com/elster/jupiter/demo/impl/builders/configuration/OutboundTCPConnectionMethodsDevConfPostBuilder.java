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
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

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

    @Inject
    public OutboundTCPConnectionMethodsDevConfPostBuilder(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withHost(String host){
        properties.put(ConnectionTypePropertySpecName.OUTBOUND_IP_HOST.propertySpecName(), host);
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withRetryDelay(int retryDelayInMinutes) {
        this.retryDelayInMinutes =  retryDelayInMinutes;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withDefaultOutboundTcpProperties(){
        this.properties.put(ConnectionTypePropertySpecName.OUTBOUND_IP_PORT_NUMBER.propertySpecName(), DEFAULT_PORT_NUMBER);
        this.properties.put(ConnectionTypePropertySpecName.OUTBOUND_IP_CONNECTION_TIMEOUT.propertySpecName(), TimeDuration.minutes(1));
        return this;
    }

    @Override
    public void accept(DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        final PartialScheduledConnectionTaskBuilder builder = configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(retryDelayInMinutes, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE, getProtocolDialectConfigurationProperties(configuration))
                .comPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
                .setNumberOfSimultaneousConnections(1)
                .asDefault(true);
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
                                protocolDialectConfigurationProperties.getDeviceProtocolDialectName().toLowerCase().contains("tcp"))
                        .findFirst();
        return tcpDialect.orElse(configuration.getProtocolDialectConfigurationPropertiesList().get(0));
    }
}
