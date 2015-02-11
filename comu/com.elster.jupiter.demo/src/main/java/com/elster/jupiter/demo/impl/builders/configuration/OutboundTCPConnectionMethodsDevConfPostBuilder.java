package com.elster.jupiter.demo.impl.builders.configuration;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.templates.OutboundTCPComPortPoolTpl;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.function.Consumer;

public class OutboundTCPConnectionMethodsDevConfPostBuilder implements Consumer<DeviceConfiguration> {

    private final ProtocolPluggableService protocolPluggableService;

    private String host;

    @Inject
    public OutboundTCPConnectionMethodsDevConfPostBuilder(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    public OutboundTCPConnectionMethodsDevConfPostBuilder withHost(String host){
        this.host =  host;
        return this;
    }

    @Override
    public void accept(DeviceConfiguration configuration) {
        if (this.host == null){
            throw new UnableToCreate("You must specify a host for NTA tool");
        }
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName("OutboundTcpIp").get();
        configuration
                .newPartialScheduledConnectionTask("Outbound TCP", pluggableClass, new TimeDuration(60, TimeDuration.TimeUnit.MINUTES), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(Builders.from(OutboundTCPComPortPoolTpl.ORANGE).get())
                .addProperty("host", this.host)
                .addProperty("portNumber", new BigDecimal(4059))
                .addProperty("connectionTimeout", TimeDuration.minutes(1))
                .asDefault(true).build();
    }
}
