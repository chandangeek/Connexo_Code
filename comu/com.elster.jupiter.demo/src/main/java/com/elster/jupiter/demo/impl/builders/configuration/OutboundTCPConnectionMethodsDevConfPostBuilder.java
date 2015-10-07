package com.elster.jupiter.demo.impl.builders.configuration;

import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class OutboundTCPConnectionMethodsDevConfPostBuilder implements Consumer<DeviceConfiguration> {

    private final ProtocolPluggableService protocolPluggableService;

    private int retryDelayInMinutes = 60;
    private HashMap<String, Object> properties = new HashMap<>();

    @Inject
    public OutboundTCPConnectionMethodsDevConfPostBuilder(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withHost(String host){
        properties.put("host", host);
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withRetryDelay(int retryDelayInMinutes){
        this.retryDelayInMinutes =  retryDelayInMinutes;
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withProperty(String property, Object value){
        this.properties.put(property, value);
        return this;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withDefaultOutboundTcpProperties(){
        this.properties.put("portNumber", new BigDecimal(4059));
        this.properties.put("connectionTimeout", TimeDuration.minutes(1));
        return this;
    }

    @Override
    public void accept(DeviceConfiguration configuration) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        final PartialScheduledConnectionTaskBuilder builder = configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(retryDelayInMinutes, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
                .asDefault(true);
        this.properties.entrySet().stream().forEach(x-> addProperty(builder, x));
        builder.build();
    }

    private void addProperty(PartialScheduledConnectionTaskBuilder builder, Map.Entry<String,Object> entry){
        builder.addProperty(entry.getKey(), entry.getValue());
    }


}
